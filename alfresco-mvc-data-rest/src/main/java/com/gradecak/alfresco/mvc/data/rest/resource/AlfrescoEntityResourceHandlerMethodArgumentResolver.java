package com.gradecak.alfresco.mvc.data.rest.resource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Persistable;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartRequest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecak.alfresco.mvc.data.mapper.BeanEntityMapper;
import com.gradecak.alfresco.mvc.data.support.AlfrescoNodeConfiguration;

/**
 * Custom {@link HandlerMethodArgumentResolver} to create {@link PersistentEntityResource} instances.
 * 
 */
public class AlfrescoEntityResourceHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

  private static final String ERROR_MESSAGE = "Could not read an object of type %s from the request! Converter %s returned null!";
  private static final String NO_CONVERTER_FOUND = "No suitable HttpMessageConverter found to read request body into object of type %s from request with content type of %s!";

  private final RootResourceInformationHandlerMethodArgumentResolver repoRequestResolver;
  private final List<HttpMessageConverter<?>> messageConverters;
  private final ServiceRegistry serviceRegistry;
  private final AlfrescoNodeConfiguration alfrescoNodeConfiguration;

  /**
   * Creates a new {@link PersistentEntityResourceHandlerMethodArgumentResolver} for the given
   * {@link HttpMessageConverter}s and {@link RootResourceInformationHandlerMethodArgumentResolver}..
   * 
   * @param messageConverters must not be {@literal null}.
   * @param repositoryRequestResolver must not be {@literal null}.
   */
  public AlfrescoEntityResourceHandlerMethodArgumentResolver(ServiceRegistry serviceRegistry, AlfrescoNodeConfiguration alfrescoNodeConfiguration, List<HttpMessageConverter<?>> messageConverters,
      RootResourceInformationHandlerMethodArgumentResolver repositoryRequestResolver) {

    Assert.notEmpty(messageConverters, "MessageConverters must not be null or empty!");
    Assert.notNull(repositoryRequestResolver, "RootResourceInformationHandlerMethodArgumentResolver must not be empty!");

    this.messageConverters = messageConverters;
    this.repoRequestResolver = repositoryRequestResolver;
    this.serviceRegistry = serviceRegistry;
    this.alfrescoNodeConfiguration = alfrescoNodeConfiguration;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.springframework.web.method.support.HandlerMethodArgumentResolver#supportsParameter(org.springframework.core.
   * MethodParameter)
   */
  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return AlfrescoEntityResource.class.isAssignableFrom(parameter.getParameterType());
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.web.method.support.HandlerMethodArgumentResolver#resolveArgument(org.springframework.core.
   * MethodParameter, org.springframework.web.method.support.ModelAndViewContainer,
   * org.springframework.web.context.request.NativeWebRequest,
   * org.springframework.web.bind.support.WebDataBinderFactory)
   */
  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public AlfrescoEntityResource resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

    RootResourceInformation resourceInformation = repoRequestResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

    HttpServletRequest nativeRequest = webRequest.getNativeRequest(HttpServletRequest.class);
    ServletServerHttpRequest request = new ServletServerHttpRequest(nativeRequest);

    Class<Persistable<NodeRef>> domainType = resourceInformation.getDomainClass();
    MediaType contentType = request.getHeaders().getContentType();
    
    BeanEntityMapper<Persistable<NodeRef>> mapper = null;
    QName createQName = null;
    
    String dataType = nativeRequest.getParameter("dataType");
    if (StringUtils.hasText(dataType)) {
      createQName = QName.createQName(dataType, serviceRegistry.getNamespaceService());
      mapper = alfrescoNodeConfiguration.findBestMatch(createQName);      
    }
    
    if(nativeRequest instanceof MultipartRequest) {
      MultipartRequest req = ((MultipartRequest)nativeRequest);
      String data = nativeRequest.getParameter("data");
      HashMap<String, Object> map = new ObjectMapper().readValue(data, new TypeReference<HashMap<String, Object>>() {});
      Persistable<NodeRef> node = mapper.mapNodeProperties(null, toQNameMap(map));
      
      return AlfrescoEntityResource.build(node, createQName, mapper, req.getFile("filedata").getInputStream()).build();
    }

    for (HttpMessageConverter converter : messageConverters) {

      if (!converter.canRead(AlfrescoEntityResource.class, contentType)) {
        continue;
      }

      Persistable<NodeRef> obj = (Persistable<NodeRef>)converter.read(domainType, request);

      if (obj == null) {
        throw new HttpMessageNotReadableException(String.format(ERROR_MESSAGE, domainType, converter));
      }

      if(mapper == null) {
        mapper = resourceInformation.getInvoker().getRepository().getBeanEntityMapper();
      }
      return AlfrescoEntityResource.build(obj, mapper.supportsNodeType(), mapper).build();
    }

    throw new HttpMessageNotReadableException(String.format(NO_CONVERTER_FOUND, domainType, contentType));
  }

  private Map<QName, Serializable> toQNameMap(HashMap<String, Object> entity) {

    Map<QName, Serializable> properties = new HashMap<>();

    for (Map.Entry<String, Object> entry : entity.entrySet()) {
      String key = entry.getKey();
      String replaceAll = key.replaceFirst("(.)(\\p{Upper})", "$1:$2").toLowerCase();

      QName qName = QName.createQName(replaceAll, serviceRegistry.getNamespaceService());
      PropertyDefinition propertyDefinition = serviceRegistry.getDictionaryService().getProperty(qName);
      if (propertyDefinition == null) {
        continue;
      }
      Serializable value = (Serializable) entry.getValue();
      if (value != null) {
        properties.put(qName, value);
      }
    }
    
    return properties;
  }
}
