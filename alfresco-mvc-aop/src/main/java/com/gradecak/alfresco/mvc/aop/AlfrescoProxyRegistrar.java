/*-
 * #%L
 * Alfresco MVC aop
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
package com.gradecak.alfresco.mvc.aop;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.gradecak.alfresco.mvc.annotation.EnableAlfrescoMvcAop;

public class AlfrescoProxyRegistrar implements ImportBeanDefinitionRegistrar {

	public static final String PACKAGE_PROXY_CREATOR_BEAN_NAME = "com.gradecak.alfresco.mvc.aop.alfrescoMvcPackageAutoProxyCreator";
	// public static final String AUTOWIRED_PROCESSOR_BEAN_NAME =
	// "org.springframework.beans.factory.annotation.alfrescoMvcAutowiredAnnotationBeanPostProcessor";

	private AnnotationAttributes attributes;
	private AnnotationMetadata metadata;

	public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {

		Assert.notNull(annotationMetadata, "AnnotationMetadata must not be null!");
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");

		// Guard against calls for sub-classes
		if (annotationMetadata.getAnnotationAttributes(EnableAlfrescoMvcAop.class.getName()) == null) {
			return;
		}

		boolean proxyBeanRegistered = false;
		for (String beanName : PackageAutoProxyCreator.DEFAULT_INTERCEPTORS) {
			if (registry.containsBeanDefinition(beanName)) {
				proxyBeanRegistered = true;
				break;
			}
		}

		if (!proxyBeanRegistered) {
			XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(registry);
			xmlReader.loadBeanDefinitions("classpath:com/gradecak/alfresco-mvc/alfresco-mvc-aop.xml");
		}

		this.attributes = new AnnotationAttributes(
				annotationMetadata.getAnnotationAttributes(EnableAlfrescoMvcAop.class.getName()));
		this.metadata = annotationMetadata;

		boolean defaultPropertiesSupport = attributes.getBoolean("defaultPropertiesSupport");
		if (defaultPropertiesSupport) {
			RootBeanDefinition beanDefinition = new RootBeanDefinition(PropertySourcesPlaceholderConfigurer.class);
			registry.registerBeanDefinition("propertySourcesPlaceholderConfigurer", beanDefinition);
		}

		Iterable<String> basePackages = getBasePackages();
		for (String basePackage : basePackages) {
			registerOrEscalateApcAsRequired(PackageAutoProxyCreator.class, registry, null, basePackage);
		}

		// if (!registry.containsBeanDefinition(AUTOWIRED_PROCESSOR_BEAN_NAME)) {
		// RootBeanDefinition beanDefinition = new
		// RootBeanDefinition(AutowiredAnnotationBeanPostProcessor.class);
		// beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		// registry.registerBeanDefinition(AUTOWIRED_PROCESSOR_BEAN_NAME,
		// beanDefinition);
		// }

		// if (!registry.containsBeanDefinition(CONFIGURATION_PROCESSOR_BEAN_NAME)) {
		// RootBeanDefinition beanDefinition = new
		// RootBeanDefinition(ConfigurationClassPostProcessor.class);
		// beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		// registry.registerBeanDefinition(CONFIGURATION_PROCESSOR_BEAN_NAME,
		// beanDefinition);
		// }
	}

	public Iterable<String> getBasePackages() {

		String[] value = attributes.getStringArray("value");
		String[] basePackages = attributes.getStringArray("basePackages");
		Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");

		// Default configuration - return package of annotated class
		if (value.length == 0 && basePackages.length == 0 && basePackageClasses.length == 0) {
			String className = metadata.getClassName();
			return Collections.singleton(ClassUtils.getPackageName(className));
		}

		Set<String> packages = new HashSet<>();
		packages.addAll(Arrays.asList(value));
		packages.addAll(Arrays.asList(basePackages));

		for (Class<?> typeName : basePackageClasses) {
			packages.add(ClassUtils.getPackageName(typeName));
		}

		return packages;
	}

	public static void registerOrEscalateApcAsRequired(Class<PackageAutoProxyCreator> cls,
			BeanDefinitionRegistry registry, Object source, String basePackage) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");

		String proxyPackageBeanName = PACKAGE_PROXY_CREATOR_BEAN_NAME + "." + basePackage;
		if (registry.containsBeanDefinition(proxyPackageBeanName)) {
			return;
		}

		RootBeanDefinition beanDefinition = new RootBeanDefinition(cls);
		beanDefinition.setSource(source);
		beanDefinition.getPropertyValues().add("order", Ordered.HIGHEST_PRECEDENCE);
		beanDefinition.getPropertyValues().add("basePackage", basePackage);
		beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		registry.registerBeanDefinition(proxyPackageBeanName, beanDefinition);
	}
}
