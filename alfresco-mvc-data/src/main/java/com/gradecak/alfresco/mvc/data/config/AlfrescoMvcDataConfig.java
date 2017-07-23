package com.gradecak.alfresco.mvc.data.config;

import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.support.Repositories;

import com.gradecak.alfresco.mvc.config.AlfrescoMvcRestConfig;
import com.gradecak.alfresco.mvc.data.mapper.BeanEntityMapper;
import com.gradecak.alfresco.mvc.data.service.AlfrescoDataEntityService;
import com.gradecak.alfresco.mvc.data.support.AlfrescoNodeConfiguration;

@Configuration
public abstract class AlfrescoMvcDataConfig extends AlfrescoMvcRestConfig {

  @Autowired(required = false)
  List<BeanEntityMapper<?>> mappers;

  @Bean
  public AlfrescoNodeConfiguration alfrescoNodeConfiguration(ServiceRegistry serviceRegistry) {
    return new AlfrescoNodeConfiguration(mappers, serviceRegistry);
  }

  @Bean
  public Repositories repositories(ListableBeanFactory beanFactory) {
    return new Repositories(beanFactory);
  }

  @Bean
  public AlfrescoDataEntityService alfrescoEntityService(ServiceRegistry serviceRegistry) {
    AlfrescoDataEntityService alfrescoEntityService = new AlfrescoDataEntityService();
    alfrescoEntityService.setServiceRegistry(serviceRegistry);
    return alfrescoEntityService;
  }
}
