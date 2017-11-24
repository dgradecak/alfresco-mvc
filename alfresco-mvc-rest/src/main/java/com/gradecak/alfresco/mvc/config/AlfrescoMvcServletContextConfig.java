package com.gradecak.alfresco.mvc.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.gradecak.alfresco.mvc.converter.NodeRefConverter;
import com.gradecak.alfresco.mvc.jackson.Jackson2NodeRefDeserializer;
import com.gradecak.alfresco.mvc.jackson.Jackson2NodeRefSerializer;
import com.gradecak.alfresco.mvc.jackson.Jackson2QnameDeserializer;
import com.gradecak.alfresco.mvc.jackson.Jackson2QnameSerializer;

@Configuration
@EnableWebMvc
public class AlfrescoMvcServletContextConfig extends WebMvcConfigurerAdapter {

  @Autowired
  ServiceRegistry serviceRegistry;

  @Bean
  public CommonsMultipartResolver multipartResolver() {
    final CommonsMultipartResolver resolver = new CommonsMultipartResolver();
    resolver.setMaxUploadSize(-1);
    resolver.setDefaultEncoding("utf-8");
    configureMultipartResolver(resolver);
    return resolver;
  }

  private void configureMultipartResolver(final CommonsMultipartResolver resolver) {}

  @Bean
  public ObjectMapper objectMapper() {

    final ObjectMapper objectMapper = new ObjectMapper();
    configureJacksonObjectMapper(objectMapper);

    return objectMapper;
  }

  protected void configureJacksonObjectMapper(final ObjectMapper objectMapper) {
    objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    final SimpleModule module = new SimpleModule("Alfresco @MVC Data Module", new Version(1, 0, 0, null, null, null));
    module.addSerializer(QName.class, new Jackson2QnameSerializer(serviceRegistry));
    module.addDeserializer(QName.class, new Jackson2QnameDeserializer(serviceRegistry));

    module.addSerializer(NodeRef.class, new Jackson2NodeRefSerializer());
    module.addDeserializer(NodeRef.class, new Jackson2NodeRefDeserializer());

    objectMapper.registerModule(module);
  }

  @Bean
  public MappingJackson2HttpMessageConverter jacksonHttpMessageConverter() {

    final List<MediaType> mediaTypes = new ArrayList<MediaType>();
    mediaTypes.addAll(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.valueOf("application/schema+json")));

    final MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
    jacksonConverter.setObjectMapper(objectMapper());
    jacksonConverter.setSupportedMediaTypes(mediaTypes);

    return jacksonConverter;
  }

  @Bean
  public NodeRefConverter nodeRefConverter() {
    return new NodeRefConverter();
  }

  @Override
  public void addFormatters(FormatterRegistry formatterRegistry) {
    formatterRegistry.addConverter((Converter<String, NodeRef>) nodeRefConverter());
  }

}
