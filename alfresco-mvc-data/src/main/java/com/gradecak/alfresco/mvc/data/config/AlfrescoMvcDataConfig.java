package com.gradecak.alfresco.mvc.data.config;

import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.support.Repositories;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.gradecak.alfresco.mvc.data.annotation.EnableAlfrescoRepositories;
import com.gradecak.alfresco.mvc.data.mapper.BeanEntityMapper;
import com.gradecak.alfresco.mvc.data.mapper.CmDocumentPropertiesMapper;
import com.gradecak.alfresco.mvc.data.mapper.CmFolderPropertiesMapper;
import com.gradecak.alfresco.mvc.data.repository.CmDocumentRepository;
import com.gradecak.alfresco.mvc.data.service.AlfrescoEntityService;
import com.gradecak.alfresco.mvc.data.support.AlfrescoNodeConfiguration;

@Configuration
@EnableAlfrescoRepositories(basePackageClasses = CmDocumentRepository.class)
public class AlfrescoMvcDataConfig extends WebMvcConfigurerAdapter {
    
  @Bean
  public AlfrescoNodeConfiguration alfrescoNodeConfiguration(ServiceRegistry serviceRegistry, List<BeanEntityMapper<?>> mappers) {
    return new AlfrescoNodeConfiguration(mappers, serviceRegistry);
  }

  @Bean
  public Repositories repositories(ListableBeanFactory beanFactory) {
    return new Repositories(beanFactory);
  }
  
  @Bean
  public CmDocumentPropertiesMapper cmDocumentPropertiesMapper(ServiceRegistry serviceRegistry) {
    return new CmDocumentPropertiesMapper(serviceRegistry);
  }
  
  @Bean
  public CmFolderPropertiesMapper smFolderPropertiesMapper(ServiceRegistry serviceRegistry) {
    return new CmFolderPropertiesMapper(serviceRegistry);
  }
  
  @Bean
  public AlfrescoEntityService alfrescoEntityService(ServiceRegistry serviceRegistry) {
    AlfrescoEntityService alfrescoEntityService = new AlfrescoEntityService();
    alfrescoEntityService.setServiceRegistry(serviceRegistry);
    return alfrescoEntityService;
  }
}
