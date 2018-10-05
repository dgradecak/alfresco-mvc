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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;

import com.gradecak.alfresco.mvc.rest.annotation.EnableAlfrescoMvcRest;
import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript;

public class AlfrescoRestRegistrar implements ImportBeanDefinitionRegistrar {

  private AnnotationAttributes attributes;

  public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {

    Assert.notNull(annotationMetadata, "AnnotationMetadata must not be null!");
    Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");

    this.attributes = new AnnotationAttributes(annotationMetadata.getAnnotationAttributes(EnableAlfrescoMvcRest.class.getName()));

    AnnotationAttributes[] dispatcherWebscripts = (AnnotationAttributes[]) attributes.get("value");

    for (AnnotationAttributes dispatcherWebscript : dispatcherWebscripts) {
      processDispatcherWebscript(dispatcherWebscript, registry);
    }

  }

  private void processDispatcherWebscript(AnnotationAttributes webscriptAttributes, BeanDefinitionRegistry registry) {
    String webscript = webscriptAttributes.getString("name");
    Assert.notNull(webscript, "Webscript name cannot be empty!");

    Class<?> servletContext = webscriptAttributes.getClass("servletContext");
    HttpMethod[] htpMethods = (HttpMethod[]) webscriptAttributes.get("htpMethods");

    // DispatcherWebscript dispatcherWebscript = dispatcherWebscript(servletContext);

    RootBeanDefinition beanDefinition = new RootBeanDefinition(DispatcherWebscript.class);
    beanDefinition.setSource(null);
    beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(webscript);
    beanDefinition.getPropertyValues().add("contextClass", org.springframework.web.context.support.AnnotationConfigWebApplicationContext.class);
    beanDefinition.getPropertyValues().add("contextConfigLocation", servletContext.getName());
    beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

    List<HttpMethod> methodsIteable = new ArrayList<>(Arrays.asList(htpMethods));
    String beanName = getWebscriptName(webscript, methodsIteable.remove(0));
    registry.registerBeanDefinition(beanName, beanDefinition);

    for (HttpMethod httpMethod : htpMethods) {
      registry.registerAlias(beanName, getWebscriptName(webscript, httpMethod));
    }
  }

  private String getWebscriptName(String webscript, HttpMethod httpMethod) {
    String beanName = "webscript." + webscript + "." + httpMethod.name();
    return beanName.toLowerCase();
  }
}
