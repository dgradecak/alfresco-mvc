package com.gradecak.alfresco.mvc.data.rest.config;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.data.repository.support.Repositories;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.AnnotationRelProvider;
import org.springframework.hateoas.core.DefaultRelProvider;
import org.springframework.hateoas.core.DelegatingRelProvider;
import org.springframework.hateoas.core.EvoInflectorRelProvider;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.hateoas.hal.Jackson2HalModule.HalHandlerInstantiator;
import org.springframework.hateoas.mvc.TypeConstrainedMappingJackson2HttpMessageConverter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecak.alfresco.mvc.data.config.AlfrescoMvcDataServletContextConfig;
import com.gradecak.alfresco.mvc.data.rest.controller.NodeController;
import com.gradecak.alfresco.mvc.data.rest.resource.AlfrescoEntityResourceHandlerMethodArgumentResolver;
import com.gradecak.alfresco.mvc.data.rest.resource.DomainResourceHandlerAdapter;
import com.gradecak.alfresco.mvc.data.rest.resource.ResourceMetadataHandlerMethodArgumentResolver;
import com.gradecak.alfresco.mvc.data.rest.resource.RootResourceInformationHandlerMethodArgumentResolver;
import com.gradecak.alfresco.mvc.data.rest.service.AlfrescoMvcCannedQueryService;
import com.gradecak.alfresco.mvc.data.support.AlfrescoNodeConfiguration;

@Configuration
@EnableWebMvc
public class AlfrescoMvcDataRestHateoasServletContext extends AlfrescoMvcDataServletContextConfig {

  @Autowired(required = false)
  CurieProvider curieProvider;

  @Bean
  public MappingJackson2HttpMessageConverter halJacksonHttpMessageConverter() {

    ArrayList<MediaType> mediaTypes = new ArrayList<MediaType>();
    mediaTypes.add(MediaTypes.HAL_JSON);

    // if (config().useHalAsDefaultJsonMediaType()) {
    // mediaTypes.add(MediaType.APPLICATION_JSON);
    // }

    MappingJackson2HttpMessageConverter converter = new TypeConstrainedMappingJackson2HttpMessageConverter(ResourceSupport.class);
    converter.setObjectMapper(halObjectMapper());
    converter.setSupportedMediaTypes(mediaTypes);

    return converter;
  }

  @Bean
  public ObjectMapper halObjectMapper() {

    HalHandlerInstantiator instantiator = new HalHandlerInstantiator(getDefaultedRelProvider(), curieProvider);

    ObjectMapper mapper = objectMapper();
    //mapper.registerModule(new Jackson2HalModule()); 
    // hal module cannot be used for now with older spring versions https://github.com/spring-projects/spring-boot/issues/6168 https://github.com/spring-projects/spring-boot/issues/5758
    mapper.setHandlerInstantiator(instantiator);

    configureJacksonObjectMapper(mapper);

    return mapper;
  }

  @Bean
  public List<HttpMessageConverter<?>> defaultMessageConverters() {

    List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();

    // if (config().getDefaultMediaType().equals(MediaTypes.HAL_JSON)) {
    // messageConverters.add(halJacksonHttpMessageConverter());
    // messageConverters.add(jacksonHttpMessageConverter());
    // } else {
    messageConverters.add(jacksonHttpMessageConverter());
    messageConverters.add(halJacksonHttpMessageConverter());

    MappingJackson2HttpMessageConverter fallbackJsonConverter = new MappingJackson2HttpMessageConverter();
    fallbackJsonConverter.setObjectMapper(objectMapper());

    messageConverters.add(fallbackJsonConverter);
    // messageConverters.add(uriListHttpMessageConverter());

    return messageConverters;
  }

  @Bean
  public DefaultRelProvider defaultRelProvider() {
    return new EvoInflectorRelProvider();
  }

  @Bean
  public AnnotationRelProvider annotationRelProvider() {
    return new AnnotationRelProvider();
  }

  private RelProvider getDefaultedRelProvider() {
    OrderAwarePluginRegistry<RelProvider, Class<?>> relProviderPluginRegistry = OrderAwarePluginRegistry.create(Arrays.asList(defaultRelProvider(), annotationRelProvider()));

    return new DelegatingRelProvider(relProviderPluginRegistry);
  }

  @Autowired
  ListableBeanFactory beanFactory;

  @Bean
  public RootResourceInformationHandlerMethodArgumentResolver domainRequestArgumentResolver(ServiceRegistry serviceRegistry, AlfrescoNodeConfiguration alfrescoNodeConfiguration,
      Repositories repositories, AlfrescoMvcCannedQueryService cannedQueryService) {
    return new RootResourceInformationHandlerMethodArgumentResolver(resourceMetadataHandlerMethodArgumentResolver(), repositories, alfrescoNodeConfiguration, cannedQueryService, serviceRegistry);
  }

  @Bean
  public AlfrescoEntityResourceHandlerMethodArgumentResolver alfrescoDomainResourceHandlerMethodArgumentResolver(ServiceRegistry serviceRegistry, AlfrescoNodeConfiguration alfrescoNodeConfiguration,
      RootResourceInformationHandlerMethodArgumentResolver domainRequestArgumentResolver) {
    return new AlfrescoEntityResourceHandlerMethodArgumentResolver(serviceRegistry, alfrescoNodeConfiguration, defaultMessageConverters(), domainRequestArgumentResolver);
  }

  @Bean
  public ResourceMetadataHandlerMethodArgumentResolver resourceMetadataHandlerMethodArgumentResolver() {
    return new ResourceMetadataHandlerMethodArgumentResolver(URI.create(NodeController.BASE_REQUEST_MAPPING));
  }
  
  @Bean
  @SuppressWarnings("rawtypes")
  public RequestMappingHandlerAdapter repositoryExporterHandlerAdapter(ServiceRegistry serviceRegistry, AlfrescoNodeConfiguration alfrescoNodeConfiguration, RootResourceInformationHandlerMethodArgumentResolver domainRequestArgumentResolver) {

    List<HttpMessageConverter<?>> messageConverters = defaultMessageConverters();
    configureHttpMessageConverters(messageConverters);

    Collection<ResourceProcessor> beans = beanFactory.getBeansOfType(ResourceProcessor.class, false, false).values();
    List<ResourceProcessor<?>> processors = new ArrayList<ResourceProcessor<?>>(beans.size());

    for (ResourceProcessor<?> bean : beans) {
      processors.add(bean);
    }

    AnnotationAwareOrderComparator.sort(processors);

    List<HandlerMethodArgumentResolver> defaultMethodArgumentResolvers = Arrays.asList(pageableResolver(), sortResolver(), domainRequestArgumentResolver,
        alfrescoDomainResourceHandlerMethodArgumentResolver(serviceRegistry, alfrescoNodeConfiguration, domainRequestArgumentResolver), resourceMetadataHandlerMethodArgumentResolver());

    DomainResourceHandlerAdapter handlerAdapter = new DomainResourceHandlerAdapter(defaultMethodArgumentResolvers, processors);
    handlerAdapter.setMessageConverters(messageConverters);

    return handlerAdapter;
  }

  
  protected void configureHttpMessageConverters(List<HttpMessageConverter<?>> messageConverters) {}
}
