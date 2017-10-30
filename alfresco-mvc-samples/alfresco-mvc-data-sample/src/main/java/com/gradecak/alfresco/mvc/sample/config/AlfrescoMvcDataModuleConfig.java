package com.gradecak.alfresco.mvc.sample.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.gradecak.alfresco.mvc.annotation.EnableAlfrescoMvcProxy;
import com.gradecak.alfresco.mvc.data.annotation.EnableAlfrescoRepositories;
import com.gradecak.alfresco.mvc.data.config.AlfrescoMvcDataConfig;
import com.gradecak.alfresco.mvc.data.repository.CmDocumentRepository;
import com.gradecak.alfresco.mvc.sample.service.AlfrescoDataService;

@Configuration
@EnableAlfrescoMvcProxy(basePackageClasses = AlfrescoDataService.class)
@EnableAlfrescoRepositories(basePackageClasses = CmDocumentRepository.class)
public class AlfrescoMvcDataModuleConfig extends AlfrescoMvcDataConfig {

  @Override
  protected Class<? extends WebMvcConfigurerAdapter> configureWebMvcConfigurerAdapter() {
    return AlfrescoMvcDataServletContext.class;
  }

  @Bean
  public AlfrescoDataService alfrescoDataService() {
    return new AlfrescoDataService();
  }
}