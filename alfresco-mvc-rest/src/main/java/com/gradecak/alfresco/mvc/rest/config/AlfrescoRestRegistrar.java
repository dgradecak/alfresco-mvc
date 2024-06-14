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

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;

import com.gradecak.alfresco.mvc.rest.annotation.AlfrescoDispatcherWebscript;
import com.gradecak.alfresco.mvc.rest.annotation.EnableAlfrescoMvcRest;
import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript;
import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript.DispatcherWebscriptServlet;
import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript.ServletConfigOptions;

import jakarta.servlet.ServletContext;

public class AlfrescoRestRegistrar implements ImportBeanDefinitionRegistrar {

	public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {

		Assert.notNull(annotationMetadata, "AnnotationMetadata must not be null!");
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");

		Map<String, Object> annotationAttributes = annotationMetadata
				.getAnnotationAttributes(EnableAlfrescoMvcRest.class.getName());
		if (annotationAttributes == null) {
			Map<String, Object> annotationAttributes2 = annotationMetadata
					.getAnnotationAttributes(AlfrescoDispatcherWebscript.class.getName());

			if (annotationAttributes2 != null) {
				annotationAttributes = new AnnotationAttributes();
				annotationAttributes.put("value", Collections.singleton(new AnnotationAttributes(annotationAttributes2))
						.toArray(new AnnotationAttributes[0]));
			}

		}

		AnnotationAttributes attributes = new AnnotationAttributes(annotationAttributes);
		AnnotationAttributes[] dispatcherWebscripts = (AnnotationAttributes[]) attributes.get("value");

		for (AnnotationAttributes dispatcherWebscript : dispatcherWebscripts) {
			processDispatcherWebscript(dispatcherWebscript, registry);
		}

	}

	private void processDispatcherWebscript(AnnotationAttributes webscriptAttributes, BeanDefinitionRegistry registry) {
		String webscript = webscriptAttributes.getString("name");
		Assert.hasText(webscript, "Webscript name cannot be empty!");

		Class<?> servletContext = webscriptAttributes.getClass("servletContext");

		ServletConfigOptions[] servletConfigOptions = (ServletConfigOptions[]) webscriptAttributes
				.get("servletConfigOptions");
		Class<? extends WebApplicationContext> servletContextClass = webscriptAttributes
				.getClass("servletContextClass");
		RequestMethod[] httpRequestMethods = (RequestMethod[]) webscriptAttributes.get("httpMethods");
		boolean inheritGlobalProperties = (Boolean) webscriptAttributes.get("inheritGlobalProperties");

		GenericBeanDefinition dispatcherWebscriptServletDefinition = new GenericBeanDefinition();
		dispatcherWebscriptServletDefinition.setBeanClass(DispatcherWebscriptServlet.class);
		ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
		// constructorArgumentValues.addIndexedArgumentValue(0, new
		// RuntimeBeanReference(WebApplicationContext.class));
		constructorArgumentValues.addIndexedArgumentValue(1, new RuntimeBeanReference(ServletContext.class));
		constructorArgumentValues.addIndexedArgumentValue(2, webscript);
		constructorArgumentValues.addIndexedArgumentValue(3, servletContextClass);
		constructorArgumentValues.addIndexedArgumentValue(4, servletContext);
		constructorArgumentValues.addIndexedArgumentValue(5, inheritGlobalProperties);
		dispatcherWebscriptServletDefinition.setConstructorArgumentValues(constructorArgumentValues);
		dispatcherWebscriptServletDefinition.setRole(BeanDefinition.ROLE_APPLICATION);

		EnumSet<ServletConfigOptions> servletConfigOptionsList = EnumSet.noneOf(ServletConfigOptions.class);
		servletConfigOptionsList.addAll(Arrays.asList(servletConfigOptions));
		if (!servletConfigOptionsList.isEmpty()) {
			MutablePropertyValues mutablePropertyValues = new MutablePropertyValues(List.of(
					new PropertyValue("detectAllHandlerMappings",
							!servletConfigOptionsList.contains(ServletConfigOptions.DISABLED_PARENT_HANDLER_MAPPINGS)),
					new PropertyValue("detectAllHandlerAdapters",
							!servletConfigOptionsList.contains(ServletConfigOptions.DISABLED_PARENT_HANDLER_ADAPTERS)),
					new PropertyValue("detectAllViewResolvers",
							!servletConfigOptionsList.contains(ServletConfigOptions.DISABLED_PARENT_VIEW_RESOLVERS)),
					new PropertyValue("detectAllHandlerExceptionResolvers", !servletConfigOptionsList
							.contains(ServletConfigOptions.DISABLED_PARENT_HANDLER_EXCEPTION_RESOLVERS))));
			dispatcherWebscriptServletDefinition.setPropertyValues(mutablePropertyValues);
		}

		registry.registerBeanDefinition("dispatcherWebscriptServlet", dispatcherWebscriptServletDefinition);

		GenericBeanDefinition disaptcherWebscriptDefinition = new GenericBeanDefinition();
		disaptcherWebscriptDefinition.setBeanClass(DispatcherWebscript.class);

		ConstructorArgumentValues constructorArgumentValues2 = new ConstructorArgumentValues();
		constructorArgumentValues2.addIndexedArgumentValue(0,
				new RuntimeBeanReference(DispatcherWebscriptServlet.class));
		disaptcherWebscriptDefinition.setConstructorArgumentValues(constructorArgumentValues2);
		disaptcherWebscriptDefinition.setRole(BeanDefinition.ROLE_APPLICATION);

		for (RequestMethod httpRequestMethod : httpRequestMethods) {
			registry.registerBeanDefinition(getWebscriptName(webscript, httpRequestMethod.asHttpMethod()),
					disaptcherWebscriptDefinition);
		}
	}

	private String getWebscriptName(String webscript, HttpMethod httpMethod) {
		String beanName = "webscript." + webscript + "." + httpMethod.name();
		return beanName.toLowerCase();
	}
}
