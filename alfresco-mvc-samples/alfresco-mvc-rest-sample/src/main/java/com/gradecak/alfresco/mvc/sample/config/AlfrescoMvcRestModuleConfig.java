package com.gradecak.alfresco.mvc.sample.config;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript;

@Configuration
public class AlfrescoMvcRestModuleConfig {

  @Autowired
  ListableBeanFactory beanFactory;

  @Bean(name = { "webscript.alfresco-mvc.mvc.post", "webscript.alfresco-mvc.mvc.get", "webscript.alfresco-mvc.mvc.delete", "webscript.alfresco-mvc.mvc.put" })
  public DispatcherWebscript dispatcherWebscript() {
    DispatcherWebscript dispatcherWebscript = new DispatcherWebscript();
    dispatcherWebscript.setContextClass(org.springframework.web.context.support.AnnotationConfigWebApplicationContext.class);
    dispatcherWebscript.setContextConfigLocation(AlfrescoMvcRestServletContext.class.getName());
    return dispatcherWebscript;
  }
}
