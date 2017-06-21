package com.gradecak.alfresco.mvc.data.support;

import java.lang.annotation.Annotation;

import org.alfresco.service.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import com.gradecak.alfresco.mvc.data.annotation.EnableAlfrescoRepositories;

public class AlfrescoRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

  @Autowired
  private ServiceRegistry serviceRegistry;

  @Override
  protected Class<? extends Annotation> getAnnotation() {
    return EnableAlfrescoRepositories.class;
  }

  @Override
  protected RepositoryConfigurationExtension getExtension() {
    return new AlfrescoRepositoryConfigExtension(serviceRegistry);
  }
}
