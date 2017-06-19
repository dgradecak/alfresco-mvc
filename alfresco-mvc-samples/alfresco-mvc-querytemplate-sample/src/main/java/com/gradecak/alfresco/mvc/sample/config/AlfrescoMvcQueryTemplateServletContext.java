package com.gradecak.alfresco.mvc.sample.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.gradecak.alfresco.mvc.config.AlfrescoMvcServletConfig;
import com.gradecak.alfresco.mvc.sample.controller.AlfrescoMvcQueryTemplateController;

@Configuration
@ComponentScan(basePackageClasses = { AlfrescoMvcQueryTemplateController.class })
@EnableWebMvc
public class AlfrescoMvcQueryTemplateServletContext extends AlfrescoMvcServletConfig {
  
  @Bean
  public PageableHandlerMethodArgumentResolver pageableResolver() {
    return new PageableHandlerMethodArgumentResolver(sortResolver());
  }

  @Bean
  public SortHandlerMethodArgumentResolver sortResolver() {
    return new SortHandlerMethodArgumentResolver();
  }
}
