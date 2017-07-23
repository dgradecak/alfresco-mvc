package com.gradecak.alfresco.mvc.data.rest.resource;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.support.Repositories;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.gradecak.alfresco.mvc.data.mapper.BeanEntityMapper;
import com.gradecak.alfresco.mvc.data.rest.service.AlfrescoMvcCannedQueryService;
import com.gradecak.alfresco.mvc.data.support.AlfrescoNodeConfiguration;
import com.gradecak.alfresco.mvc.data.support.AlfrescoNodeRepository;
import com.gradecak.alfresco.querytemplate.QueryTemplate;

/**
 * {@link HandlerMethodArgumentResolver} to create {@link RootResourceInformation} for injection into Spring MVC
 * controller methods.
 * 
 */
public class RootResourceInformationHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

  private final ResourceMetadataHandlerMethodArgumentResolver resourceMetadataResolver;
  private final Repositories repositories;
  private final AlfrescoNodeConfiguration alfrescoNodeConfiguration;
  private final AlfrescoMvcCannedQueryService cannedQueryService;
  private final ServiceRegistry serviceRegistry;

  private final Map<Class<?>, AlfrescoEntityInvoker> invokers;

  public RootResourceInformationHandlerMethodArgumentResolver(ResourceMetadataHandlerMethodArgumentResolver resourceMetadataResolver, Repositories repositories,
      AlfrescoNodeConfiguration alfrescoNodeConfiguration, AlfrescoMvcCannedQueryService cannedQueryService, ServiceRegistry serviceRegistry) {
    this.resourceMetadataResolver = resourceMetadataResolver;
    this.repositories = repositories;
    this.alfrescoNodeConfiguration = alfrescoNodeConfiguration;
    this.cannedQueryService = cannedQueryService;
    this.serviceRegistry = serviceRegistry;

    this.invokers = new HashMap<Class<?>, AlfrescoEntityInvoker>();
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return RootResourceInformation.class.isAssignableFrom(parameter.getParameterType());
  }

  @Override
  public RootResourceInformation resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

    String repositoryMapping = resourceMetadataResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

    //
    // Class<?> domainType = resourceMetadata.getDomainType();
    // RepositoryInvoker repositoryInvoker = invokerFactory.getInvokerFor(domainType);
    // PersistentEntity<?, ?> persistentEntity = repositories.getPersistentEntity(domainType);

    // TODO reject if ResourceMetadata cannot be resolved
    // Class<?> domainClass = domainConfiguration.getDomainClass(domain);
    // NodePropertiesMapper<?> nodeMapper = domainConfiguration.getNodeMapper(domain);
    // EntityPropertiesMapper<?> entityMapper = domainConfiguration.getEntityMapper(domain);
    // AlfrescoEntityInvoker invoker = domainConfiguration.getInvoker(domainClass);
    //
    // if (domainClass == null || nodeMapper == null || entityMapper == null || invoker == null) {
    // throw new IllegalArgumentException("the domain is not correctly configured: " + domain);
    // }

    // DefaultRepositoryMetadata repositoryMetadata = new DefaultRepositoryMetadata(AlfrescoNodeRepository.class);

    // new DefaultRepositoryInformation(repositoryMetadata, getRepositoryBaseClass(metadata),
    // customImplementationClass);

    HttpServletRequest nativeRequest = webRequest.getNativeRequest(HttpServletRequest.class);
    String dataType = nativeRequest.getParameter("dataType");
    if (StringUtils.hasText(dataType)) {
      BeanEntityMapper<Persistable<NodeRef>> mapper = alfrescoNodeConfiguration.findBestMatch(QName.createQName(dataType, serviceRegistry.getNamespaceService()));
      Class<Persistable<NodeRef>> mappedClass = mapper.getMappedClass();
      if (mappedClass != null) {               
        return new RootResourceInformation(mappedClass, getInvoker(mappedClass));
      }
    }

    for (Class<?> domainType : repositories) {              
      String mapping = alfrescoNodeConfiguration.getMapping(domainType);
      if (repositoryMapping.equalsIgnoreCase(mapping)) {
        return new RootResourceInformation((Class<Persistable<NodeRef>>)domainType, getInvoker(domainType));
      }
    }

    throw new IllegalArgumentException(String.format("Could not resolve repository metadata for %s.", repositoryMapping));
  }

  public AlfrescoEntityInvoker getInvoker(Class<?> domainType) {
    AlfrescoEntityInvoker invoker = invokers.get(domainType);

    if (invoker != null) {
      return invoker;
    }

    invoker = prepareInvokers(domainType);
    invokers.put(domainType, invoker);

    return invoker;
  }

  @SuppressWarnings("unchecked")
  private AlfrescoEntityInvoker prepareInvokers(Class<?> domainType) {

    Object repository = repositories.getRepositoryFor(domainType);
    RepositoryInformation information = repositories.getRepositoryInformationFor(domainType);

    // if (repository instanceof PagingAndSortingRepository) {
    // return new PagingAndSortingRepositoryInvoker((PagingAndSortingRepository<Object, Serializable>) repository,
    // information, conversionService);
    // } else if (repository instanceof CrudRepository) {
    // return new CrudRepositoryInvoker((CrudRepository<Object, Serializable>) repository, information,
    // conversionService);
    // } else {
    // return new ReflectionRepositoryInvoker(repository, information, conversionService);
    // }

    return new AlfrescoEntityInvoker((AlfrescoNodeRepository<Persistable<NodeRef>>) repository, cannedQueryService, serviceRegistry);
  }
}
