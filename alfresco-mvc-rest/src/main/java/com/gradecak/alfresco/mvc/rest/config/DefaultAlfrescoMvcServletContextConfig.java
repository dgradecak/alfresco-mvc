package com.gradecak.alfresco.mvc.rest.config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import org.alfresco.rest.api.model.Target;
import org.alfresco.rest.framework.jacksonextensions.NodeRefDeserializer;
import org.alfresco.rest.framework.jacksonextensions.NodeRefSerializer;
import org.alfresco.rest.framework.jacksonextensions.RestApiStringDeserializer;
import org.alfresco.rest.framework.jacksonextensions.SerializerOfCollectionWithPaging;
import org.alfresco.rest.framework.jacksonextensions.SerializerOfExecutionResult;
import org.alfresco.rest.framework.jacksonextensions.TargetDeserializer;
import org.alfresco.rest.framework.webscripts.ResourceWebScriptHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableList;
import com.gradecak.alfresco.mvc.rest.AlfrescoApiResponseInterceptor;
import com.gradecak.alfresco.mvc.rest.jackson.Jackson2QnameSerializer;

@Configuration
public class DefaultAlfrescoMvcServletContextConfig implements WebMvcConfigurer {
	
	@Autowired
	  protected ServiceRegistry serviceRegistry;

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(new ParamsHandlerMethodArgumentResolver());
  }

  @Bean
  public AlfrescoApiResponseInterceptor alfrescoResponseInterceptor(ResourceWebScriptHelper webscriptHelper) {
    return new AlfrescoApiResponseInterceptor(webscriptHelper);
  }

  @Bean
  public CommonsMultipartResolver multipartResolver() {
    final CommonsMultipartResolver resolver = new CommonsMultipartResolver();
    resolver.setMaxUploadSize(-1);
    resolver.setDefaultEncoding("utf-8");
    configureMultipartResolver(resolver);
    return resolver;
  }

  private void configureMultipartResolver(final CommonsMultipartResolver resolver) {}
  
  
  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
	  
	  converters.stream().filter(c -> c instanceof MappingJackson2HttpMessageConverter).forEach(c -> {
		  Jackson2ObjectMapperBuilder objectMapperBuilder = Jackson2ObjectMapperBuilder.json();
	      List<JsonSerializer<?>> jacksonSerializers = jacksonSerializers();
	      if (jacksonSerializers != null) {
	        objectMapperBuilder.serializers(jacksonSerializers.toArray(new JsonSerializer<?>[] {}));
	      }
	      List<JsonDeserializer<?>> jacksonDeserializers = jacksonDeserializers();
	      if (jacksonDeserializers != null) {
	        objectMapperBuilder.deserializers(jacksonDeserializers.toArray(new JsonDeserializer<?>[] {}));
	      }
	      
	      objectMapperBuilder.failOnEmptyBeans(false).failOnUnknownProperties(false);
          ((MappingJackson2HttpMessageConverter) c).setObjectMapper(objectMapperBuilder.build());
      });
	  
	  // this is alfresco config in org.alfresco.rest.framework.jacksonextensions.JacksonHelper.afterPropertiesSet()
//      objectMapper = new ObjectMapper();
//      objectMapper.registerModule(module);
//      objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);
//      objectMapper.configOverride(java.util.Map.class)
//                      .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, null));
//      objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
//      DateFormat DATE_FORMAT_ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//      DATE_FORMAT_ISO8601.setTimeZone(TimeZone.getTimeZone("UTC"));
//      objectMapper.setDateFormat(DATE_FORMAT_ISO8601);
//      objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
  }
  
  @SuppressWarnings("serial")
  protected List<JsonSerializer<?>> jacksonSerializers() {
    return ImmutableList.of(new SerializerOfCollectionWithPaging() {

    }, new SerializerOfExecutionResult() {}, new NodeRefSerializer() {}, new Jackson2QnameSerializer(serviceRegistry));
  }

  @SuppressWarnings("serial")
  protected List<JsonDeserializer<?>> jacksonDeserializers() {
    return ImmutableList.of(new NodeRefDeserializer() {
      @Override
      public Class<?> handledType() {
        return NodeRef.class;
      }
    }, new TargetDeserializer() {
      @Override
      public Class<?> handledType() {
        return Target.class;
      }
    }, new RestApiStringDeserializer() {
      @Override
      public Class<?> handledType() {
        return String.class;
      }
    });
  }
}
