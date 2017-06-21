package com.gradecak.alfresco.mvc.data.support;

import java.io.Serializable;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.AnnotationRepositoryMetadata;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;

import com.gradecak.alfresco.mvc.data.service.AlfrescoEntityService;

public class AlfrescoRepositoryFactoryBean<T extends AlfrescoNodeRepository<S>, S extends Persistable<NodeRef>, ID extends Serializable> extends TransactionalRepositoryFactoryBeanSupport<T, S, NodeRef> {

  private final ServiceRegistry serviceRegistry;
  private final AlfrescoEntityService documentService;
  private final ListableBeanFactory beanFactory;
  private final AlfrescoNodeConfiguration alfrescoNodeConfiguration;

  @Autowired
  public AlfrescoRepositoryFactoryBean(AlfrescoEntityService documentService, ServiceRegistry serviceRegistry, ListableBeanFactory beanFactory, AlfrescoNodeConfiguration alfrescoNodeConfiguration) {
    this.serviceRegistry = serviceRegistry;
    this.documentService = documentService;
    this.beanFactory = beanFactory;
    this.alfrescoNodeConfiguration = alfrescoNodeConfiguration;
  }

  RepositoryMetadata getRepositoryMetadata(Class<?> repositoryInterface) {
    return Repository.class.isAssignableFrom(repositoryInterface) ? new DefaultRepositoryMetadata(repositoryInterface) : new AnnotationRepositoryMetadata(repositoryInterface);
  }

  @Override
  protected RepositoryFactorySupport doCreateRepositoryFactory() {
    // TODO Auto-generated method stub
    return new AlfrescoRepositoryFactory<S>(documentService, serviceRegistry, beanFactory, alfrescoNodeConfiguration);
  }
}
