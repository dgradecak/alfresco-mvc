package com.gradecak.alfresco.mvc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript;

@Configuration
public abstract class AlfrescoMvcRestConfig extends WebMvcConfigurerAdapter {

  @Bean(name = { "webscript.alfresco-mvc.mvc.post", "webscript.alfresco-mvc.mvc.get", "webscript.alfresco-mvc.mvc.delete", "webscript.alfresco-mvc.mvc.put" })
  public DispatcherWebscript dispatcherWebscript() {
    DispatcherWebscript dispatcherWebscript = new DispatcherWebscript();
    dispatcherWebscript.setContextClass(org.springframework.web.context.support.AnnotationConfigWebApplicationContext.class);
    dispatcherWebscript.setContextConfigLocation(configureWebMvcConfigurerAdapter().getName());
    return dispatcherWebscript;
  }

  abstract protected Class<? extends WebMvcConfigurerAdapter> configureWebMvcConfigurerAdapter();
}
