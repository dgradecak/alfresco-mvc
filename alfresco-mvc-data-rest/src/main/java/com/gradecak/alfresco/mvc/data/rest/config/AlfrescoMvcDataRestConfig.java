package com.gradecak.alfresco.mvc.data.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gradecak.alfresco.mvc.data.config.AlfrescoMvcDataConfig;
import com.gradecak.alfresco.mvc.data.rest.service.AlfrescoMvcCannedQueryService;

@Configuration
public abstract class AlfrescoMvcDataRestConfig extends AlfrescoMvcDataConfig {

  @Bean
  public AlfrescoMvcCannedQueryService alfrescoMvcCannedQueryService() {
    return new AlfrescoMvcCannedQueryService();
  }
  
}
