package com.gradecak.alfresco.mvc.sample.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.gradecak.alfresco.mvc.annotation.EnableAlfrescoMvcProxy;
import com.gradecak.alfresco.mvc.config.AlfrescoMvcRestConfig;
import com.gradecak.alfresco.mvc.sample.service.QueryTemplateService;

@Configuration
@EnableAlfrescoMvcProxy(basePackageClasses = QueryTemplateService.class)
public class AlfrescoMvcQueryTemplateModuleConfig extends AlfrescoMvcRestConfig {

  @Override
  protected Class<? extends WebMvcConfigurerAdapter> configureWebMvcConfigurerAdapter() {
    return AlfrescoMvcQueryTemplateServletContext.class;
  }

  @Bean
  public QueryTemplateService queryTemplateService() {
    return new QueryTemplateService();
  }
}
