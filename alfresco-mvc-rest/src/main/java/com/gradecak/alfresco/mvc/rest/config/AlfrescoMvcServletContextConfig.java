package com.gradecak.alfresco.mvc.rest.config;

import java.util.List;

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
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.google.common.collect.ImmutableList;
import com.gradecak.alfresco.mvc.rest.AlfrescoApiResponseInterceptor;
import com.gradecak.alfresco.mvc.rest.converter.NodeRefConverter;
import com.gradecak.alfresco.mvc.rest.jackson.Jackson2QnameSerializer;

@Configuration
public class AlfrescoMvcServletContextConfig implements WebMvcConfigurer {

	@Autowired
	protected ServiceRegistry serviceRegistry;

	// @Bean
	// public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
	// final PropertySourcesPlaceholderConfigurer configurer = new
	// PropertySourcesPlaceholderConfigurer();
	// configurer.setIgnoreResourceNotFound(true);
	// return configurer;
	// }

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new ParamsHandlerMethodArgumentResolver());
	}

	@Bean
	public AlfrescoApiResponseInterceptor alfrescoResponseInterceptor(ResourceWebScriptHelper webscriptHelper) {
		return new AlfrescoApiResponseInterceptor(webscriptHelper);
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		Jackson2ObjectMapperBuilder objectMapperBuilder = Jackson2ObjectMapperBuilder.json();
		List<JsonSerializer<?>> jacksonSerializers = jacksonSerializers();
		if (jacksonSerializers != null) {
			objectMapperBuilder.serializers(jacksonSerializers.toArray(new JsonSerializer<?>[] {}));
		}
		List<JsonDeserializer<?>> jacksonDeserializers = jacksonDeserializers();
		if (jacksonDeserializers != null) {
			objectMapperBuilder.deserializers(jacksonDeserializers.toArray(new JsonDeserializer<?>[] {}));
		}
		converters.add(new MappingJackson2HttpMessageConverter(objectMapperBuilder.build()));
	}

	@SuppressWarnings("serial")
	protected List<JsonSerializer<?>> jacksonSerializers() {
		return ImmutableList.of(new SerializerOfCollectionWithPaging() {

		}, new SerializerOfExecutionResult() {
		}, new NodeRefSerializer() {
		}, new Jackson2QnameSerializer(serviceRegistry));
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

	@Bean
	public CommonsMultipartResolver multipartResolver() {
		final CommonsMultipartResolver resolver = new CommonsMultipartResolver();
		resolver.setMaxUploadSize(-1);
		resolver.setDefaultEncoding("utf-8");
		configureMultipartResolver(resolver);
		return resolver;
	}

	private void configureMultipartResolver(final CommonsMultipartResolver resolver) {
	}

	@Override
	public void addFormatters(FormatterRegistry formatterRegistry) {
		formatterRegistry.addConverter((Converter<String, NodeRef>) new NodeRefConverter());
	}

}
