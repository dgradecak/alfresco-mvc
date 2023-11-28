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

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
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
import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript.ServletConfigOptions;

public class AlfrescoRestRegistrar implements ImportBeanDefinitionRegistrar {

	private AnnotationAttributes attributes;

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

			this.attributes = new AnnotationAttributes(annotationAttributes);

		} else {
			this.attributes = new AnnotationAttributes(annotationAttributes);
		}

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

		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setBeanClass(DispatcherWebscript.class);

		DispatcherWebscript ws = new DispatcherWebscript(webscript, inheritGlobalProperties);
		ws.setContextClass(servletContextClass);
		ws.setContextConfigLocation(servletContext.getName());
		ws.addServletConfigOptions(servletConfigOptions);
		beanDefinition.setInstanceSupplier(() -> ws);
		beanDefinition.setRole(BeanDefinition.ROLE_APPLICATION);

		registry.registerBeanDefinition(webscript, beanDefinition);

		for (RequestMethod httpRequestMethod : httpRequestMethods) {
			registry.registerAlias(webscript, getWebscriptName(webscript, httpRequestMethod.asHttpMethod()));
		}
	}

	private String getWebscriptName(String webscript, HttpMethod httpMethod) {
		String beanName = "webscript." + webscript + "." + httpMethod.name();
		return beanName.toLowerCase();
	}
}
