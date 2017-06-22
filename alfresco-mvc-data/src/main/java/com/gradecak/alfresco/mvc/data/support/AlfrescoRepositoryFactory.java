package com.gradecak.alfresco.mvc.data.support;

import java.io.Serializable;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;

import com.gradecak.alfresco.mvc.data.annotation.AlfrescoNode;
import com.gradecak.alfresco.mvc.data.annotation.AlfrescoNode.NoCreator;
import com.gradecak.alfresco.mvc.data.annotation.AlfrescoNode.UseBeanPropertiesMapper;
import com.gradecak.alfresco.mvc.data.annotation.AlfrescoNodeCreator;
import com.gradecak.alfresco.mvc.data.mapper.BeanEntityMapper;
import com.gradecak.alfresco.mvc.data.mapper.EntityPropertiesMapper;
import com.gradecak.alfresco.mvc.data.service.AlfrescoDataEntityService;
import com.gradecak.alfresco.mvc.data.util.RepositoriesUtils;
import com.gradecak.alfresco.querytemplate.NodePropertiesMapper;

public class AlfrescoRepositoryFactory<T extends Persistable<NodeRef>> extends RepositoryFactorySupport {

  private final ServiceRegistry serviceRegistry;
  private final AlfrescoDataEntityService documentService;
  private final ListableBeanFactory beanFactory;
  private final AlfrescoNodeConfiguration alfrescoNodeConfiguration;

  public AlfrescoRepositoryFactory(AlfrescoDataEntityService documentService, ServiceRegistry serviceRegistry, ListableBeanFactory beanFactory, AlfrescoNodeConfiguration alfrescoNodeConfiguration) {
    this.serviceRegistry = serviceRegistry;
    this.documentService = documentService;
    this.beanFactory = beanFactory;
    this.alfrescoNodeConfiguration = alfrescoNodeConfiguration;
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected Object getTargetRepository(RepositoryMetadata metadata) {

    getEntityInformation((Class<T>)metadata.getDomainType());
    Class<T> domainClass = (Class<T>) metadata.getDomainType();
    AlfrescoNode annotation = domainClass.getAnnotation(AlfrescoNode.class);
    if(annotation == null){
      throw new IllegalStateException("Is the domain class annotated with @AlfrescoNode? class: "+domainClass);
    }

    String mapping = RepositoriesUtils.getDefaultPathFor(domainClass);
    alfrescoNodeConfiguration.addDomainMapper(mapping, domainClass);

    Class<? extends NodePropertiesMapper<? extends Persistable<NodeRef>>> nodeMapperClass = annotation.nodeMapper();
    BeanEntityMapper<T> nodeMapperInstance = null;
    if (UseBeanPropertiesMapper.class.equals(nodeMapperClass)) {
      nodeMapperInstance = new BeanEntityMapper<>(serviceRegistry, domainClass);
    } else {
      nodeMapperInstance = (BeanEntityMapper<T>) beanFactory.getBean(nodeMapperClass);
    }
    if (nodeMapperInstance == null) {
      throw new IllegalArgumentException("NodePropertiesMapper has no instance: " + nodeMapperClass);
    }

    Class<? extends EntityPropertiesMapper<?, ?>> entityMapperClass = annotation.entityMapper();
    EntityPropertiesMapper<?, ?> entityMapperInstance = null;
    if (UseBeanPropertiesMapper.class.equals(entityMapperClass)) {
      entityMapperInstance =  new BeanEntityMapper<>(serviceRegistry, domainClass);
    } else {
      entityMapperInstance = beanFactory.getBean(entityMapperClass);
    }
    if (entityMapperInstance == null) {
      throw new IllegalArgumentException("NodePropertiesMapper has no instance: " + entityMapperClass);
    }

    Class<? extends AlfrescoNodeCreator<?>> creatorClass = annotation.creator();
    AlfrescoNodeCreator<?> creatorInstance = NoCreator.INSTANCE;
    if (!NoCreator.class.equals(creatorClass)) {
      creatorInstance = beanFactory.getBean(creatorClass);
    }

    SimpleAlfrescoNodeRepository repository = new SimpleAlfrescoNodeRepository(documentService, nodeMapperInstance, entityMapperInstance, creatorInstance);
    // repository.setRepositoryMethodMetadata(lockModePostProcessor.getLockMetadataProvider());

    return repository;
  }

  @Override
  protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
    return SimpleAlfrescoNodeRepository.class;
  }

  @Override
  protected QueryLookupStrategy getQueryLookupStrategy(Key key) {

    return null;
  }
  
  @Override
  public <S , ID extends Serializable> EntityInformation<S, ID> getEntityInformation(Class<S> domainClass) {
    return (EntityInformation<S, ID>)AlfrescoEntityInformationSupport.getMetadata(domainClass);
  }

//  @Override
//  @SuppressWarnings("unchecked")
//  public <S> AbstractEntityInformation<S, NodeRef> getEntityInformation(Class<S> domainClass) {
//    return (AlfrescoEntityInformationSupport<S>) AlfrescoEntityInformationSupport.getMetadata(domainClass);
//  }
}
