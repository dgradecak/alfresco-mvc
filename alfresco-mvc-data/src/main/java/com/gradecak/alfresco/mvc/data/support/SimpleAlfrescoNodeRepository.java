package com.gradecak.alfresco.mvc.data.support;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;

import com.gradecak.alfresco.mvc.data.annotation.AlfrescoNode.NoCreator;
import com.gradecak.alfresco.mvc.data.annotation.AlfrescoNodeCreator;
import com.gradecak.alfresco.mvc.data.mapper.BeanEntityMapper;
import com.gradecak.alfresco.mvc.data.mapper.EntityPropertiesMapper;
import com.gradecak.alfresco.mvc.data.service.AlfrescoEntityService;

public class SimpleAlfrescoNodeRepository<T extends Persistable<NodeRef>> implements AlfrescoNodeRepository<T> {

  private final AlfrescoEntityService nodeService;
  private final BeanEntityMapper<T> nodeMapper;
  private final EntityPropertiesMapper<T, NodeRef> entityMapper;
  private final AlfrescoNodeCreator<T> nodeCreator;

  public SimpleAlfrescoNodeRepository(AlfrescoEntityService nodeService, BeanEntityMapper<T> nodeMapper, EntityPropertiesMapper<T, NodeRef> entityMapper, AlfrescoNodeCreator<T> nodeCreator) {
    this.nodeService = nodeService;
    this.nodeMapper = nodeMapper;
    this.entityMapper = entityMapper;
    this.nodeCreator = nodeCreator;
  }

  @Override
  public <S extends T> S save(NodeRef parentRef, S entity, QName type, InputStream is) {
    NodeRef nodeRef = entity.getId();
    Map<QName, Serializable> properties = entityMapper.mapEntity(nodeRef, entity);
    if (nodeRef != null) {
      nodeRef = nodeService.update(nodeMapper, nodeRef, properties);      
    } else {
      if(!NoCreator.class.equals(nodeCreator.getClass())) {
        //NodeRef created = nodeCreator.create(entity, properties);
        nodeRef = nodeService.create(nodeCreator, entity, properties);        
      } else {
        nodeRef = nodeService.create(parentRef, properties, type, is);
      }
    }
    return (S) nodeService.get(nodeMapper, nodeRef);
  }
  
  @Override
  public <S extends T> S save(S entity) {
    return save(null, entity, null, null);
  }
  
  @Override
  public <S extends T> Iterable<S> save(Iterable<S> entities) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public T findOne(NodeRef nodeRef) {
    return nodeService.get(nodeMapper, nodeRef);
  }

  @Override
  public boolean exists(NodeRef id) {
    return nodeService.exists(id);
  }

  @Override
  public Iterable<T> findAll() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Iterable<T> findAll(Iterable<NodeRef> nodeRefs) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public long count() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void delete(NodeRef nodeRef) {
    nodeService.delete(nodeRef);
  }

  @Override
  public void delete(T entity) {
    // TODO Auto-generated method stub

  }

  @Override
  public void delete(Iterable<? extends T> entities) {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteAll() {
    // TODO Auto-generated method stub

  }

  @Override
  public Iterable<T> findAll(Sort sort) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Page<T> findAll(Pageable pageable) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BeanEntityMapper<T> getBeanEntityMapper() {
    return nodeMapper;
  }
}
