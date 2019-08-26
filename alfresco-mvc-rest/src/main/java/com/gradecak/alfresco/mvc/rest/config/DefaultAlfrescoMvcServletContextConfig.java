package com.gradecak.alfresco.mvc.rest.config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import org.alfresco.rest.framework.jacksonextensions.RestJsonModule;
import org.alfresco.rest.framework.webscripts.ResourceWebScriptHelper;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gradecak.alfresco.mvc.rest.AlfrescoApiResponseInterceptor;

@Configuration
public class DefaultAlfrescoMvcServletContextConfig implements WebMvcConfigurer {

	@Autowired
	private RestJsonModule alfrescoRestJsonModule;

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

	private void configureMultipartResolver(final CommonsMultipartResolver resolver) {
	}

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

		converters.stream().filter(c -> c instanceof MappingJackson2HttpMessageConverter).forEach(c -> {
			Jackson2ObjectMapperBuilder objectMapperBuilder = Jackson2ObjectMapperBuilder.json();

			ObjectMapper objectMapper = objectMapperBuilder.failOnEmptyBeans(false).failOnUnknownProperties(false)
					.build();
			objectMapper.registerModule(alfrescoRestJsonModule);
			objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);
			objectMapper.configOverride(java.util.Map.class)
					.setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, null));
			objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			DateFormat DATE_FORMAT_ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			DATE_FORMAT_ISO8601.setTimeZone(TimeZone.getTimeZone("UTC"));
			objectMapper.setDateFormat(DATE_FORMAT_ISO8601);
			objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

			((MappingJackson2HttpMessageConverter) c).setObjectMapper(objectMapper);
		});

		// this is from alfresco config in
		// org.alfresco.rest.framework.jacksonextensions.JacksonHelper.afterPropertiesSet()
	}
}
