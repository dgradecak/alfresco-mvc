/**
 * Copyright gradecak.com

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradecak.alfresco.mvc.rest.config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import org.alfresco.rest.framework.jacksonextensions.RestJsonModule;
import org.alfresco.rest.framework.webscripts.ResourceWebScriptHelper;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecak.alfresco.mvc.rest.AlfrescoApiResponseInterceptor;
import com.gradecak.alfresco.mvc.rest.jackson.Jackson2NodeRefDeserializer;
import com.gradecak.alfresco.mvc.rest.jackson.Jackson2NodeRefSerializer;
import com.gradecak.alfresco.mvc.rest.jackson.Jackson2QnameDeserializer;
import com.gradecak.alfresco.mvc.rest.jackson.Jackson2QnameSerializer;

@Configuration
public class DefaultAlfrescoMvcServletContextConfiguration implements WebMvcConfigurer {

	private final RestJsonModule alfrescoRestJsonModule;
	private final NamespaceService namespaceService;

	@Autowired
	public DefaultAlfrescoMvcServletContextConfiguration(RestJsonModule alfrescoRestJsonModule,
			NamespaceService namespaceService) {
		this.alfrescoRestJsonModule = alfrescoRestJsonModule;
		this.namespaceService = namespaceService;
	}

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

	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		DateFormat DATE_FORMAT_ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		DATE_FORMAT_ISO8601.setTimeZone(TimeZone.getTimeZone("UTC"));

		return Jackson2ObjectMapperBuilder.json().failOnEmptyBeans(false).failOnUnknownProperties(false)
				.dateFormat(DATE_FORMAT_ISO8601).modulesToInstall(alfrescoRestJsonModule)
				.serializers(jackson2NodeRefSerializer(), jackson2QnameSerializer())
				.deserializers(jackson2NodeRefDeserializer(), jackson2QnameDeserializer())
				.featuresToEnable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY).findModulesViaServiceLoader(true)
				.build();
	}

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(jackson2NodeRefDeserializer());
		registry.addConverter(jackson2QnameDeserializer());
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(new ResourceHttpMessageConverter());
		converters.add(new StringHttpMessageConverter());
		converters.add(new MappingJackson2HttpMessageConverter(objectMapper()));
	}

	@Bean
	Jackson2NodeRefDeserializer jackson2NodeRefDeserializer() {
		return new Jackson2NodeRefDeserializer();
	}

	@Bean
	Jackson2QnameDeserializer jackson2QnameDeserializer() {
		return new Jackson2QnameDeserializer(namespaceService);
	}

	@Bean
	Jackson2NodeRefSerializer jackson2NodeRefSerializer() {
		return new Jackson2NodeRefSerializer();
	}

	@Bean
	Jackson2QnameSerializer jackson2QnameSerializer() {
		return new Jackson2QnameSerializer(namespaceService);
	}

}
