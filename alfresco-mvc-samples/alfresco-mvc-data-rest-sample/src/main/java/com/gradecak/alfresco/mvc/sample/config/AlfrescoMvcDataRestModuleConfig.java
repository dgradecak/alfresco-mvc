package com.gradecak.alfresco.mvc.sample.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.gradecak.alfresco.mvc.annotation.EnableAlfrescoMvcProxy;
import com.gradecak.alfresco.mvc.data.annotation.EnableAlfrescoRepositories;
import com.gradecak.alfresco.mvc.data.repository.CmDocumentRepository;
import com.gradecak.alfresco.mvc.data.rest.config.AlfrescoMvcDataRestConfig;
import com.gradecak.alfresco.mvc.data.rest.service.AlfrescoMvcCannedQueryService;

@Configuration
@EnableAlfrescoMvcProxy(basePackageClasses = AlfrescoMvcCannedQueryService.class)
@EnableAlfrescoRepositories(basePackageClasses = CmDocumentRepository.class)
public class AlfrescoMvcDataRestModuleConfig extends AlfrescoMvcDataRestConfig {

  @Override
  protected Class<? extends WebMvcConfigurerAdapter> configureWebMvcConfigurerAdapter() {
    return AlfrescoMvcDataRestServletContext.class;
  }

}
