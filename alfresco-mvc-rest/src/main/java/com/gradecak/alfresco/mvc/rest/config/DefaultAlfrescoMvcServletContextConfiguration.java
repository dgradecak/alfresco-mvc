/*-
 * #%L
 * Alfresco MVC rest
 * %%
 * Copyright (C) 2007 - 2024 gradecak.com
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.gradecak.alfresco.mvc.rest.config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
import org.springframework.lang.Nullable;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
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
	public DefaultAlfrescoMvcServletContextConfiguration(@Nullable RestJsonModule alfrescoRestJsonModule,
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
	public MultipartResolver multipartResolver() {
		MultipartResolver resolver = createMultipartResolver();
		configureMultipartResolver(resolver);
		return resolver;
	}

	protected MultipartResolver createMultipartResolver() {
		StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
		return resolver;
	}

	protected void configureMultipartResolver(final MultipartResolver resolver) {
	}

	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		return jackson2ObjectMapperBuilder().build();
	}

	@Bean
	@Primary
	public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {

		List<JsonDeserializer<?>> customJsonDeserializers = new ArrayList<>(customJsonDeserializers());
		customJsonDeserializers.add(jackson2NodeRefDeserializer());
		customJsonDeserializers.add(jackson2QnameDeserializer());

		List<JsonSerializer<?>> customJsonSerilizers = new ArrayList<>(customJsonSerilizers());
		customJsonSerilizers.add(jackson2NodeRefSerializer());
		customJsonSerilizers.add(jackson2QnameSerializer());

		Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json().failOnEmptyBeans(false)
				.failOnUnknownProperties(false).dateFormat(dateFormat())
				.serializers(customJsonSerilizers.toArray(new JsonSerializer[0]))
				.deserializers(customJsonDeserializers.toArray(new JsonDeserializer[0]))
				.featuresToEnable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
				.findModulesViaServiceLoader(true);

		if (alfrescoRestJsonModule != null) {
			builder.modulesToInstall(alfrescoRestJsonModule);
		}

		customizeJackson2ObjectMapperBuilder(builder);

		return builder;
	}

	protected DateFormat dateFormat() {
		DateFormat dateFormatIso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		dateFormatIso8601.setTimeZone(TimeZone.getTimeZone("UTC"));
		return dateFormatIso8601;
	}

	protected void customizeJackson2ObjectMapperBuilder(Jackson2ObjectMapperBuilder builder) {
	}

	protected List<JsonDeserializer<?>> customJsonDeserializers() {
		return Collections.emptyList();
	}

	protected List<JsonSerializer<?>> customJsonSerilizers() {
		return Collections.emptyList();
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

	protected Jackson2NodeRefDeserializer jackson2NodeRefDeserializer() {
		return new Jackson2NodeRefDeserializer();
	}

	protected Jackson2QnameDeserializer jackson2QnameDeserializer() {
		return new Jackson2QnameDeserializer(namespaceService);
	}

	protected Jackson2NodeRefSerializer jackson2NodeRefSerializer() {
		return new Jackson2NodeRefSerializer();
	}

	protected Jackson2QnameSerializer jackson2QnameSerializer() {
		return new Jackson2QnameSerializer(namespaceService);
	}

}
