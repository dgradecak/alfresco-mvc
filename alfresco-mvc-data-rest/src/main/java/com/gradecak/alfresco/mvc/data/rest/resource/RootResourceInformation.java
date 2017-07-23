package com.gradecak.alfresco.mvc.data.rest.resource;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.data.domain.Persistable;

/**
 * Meta-information about the root repository resource.
 * 
 */
public class RootResourceInformation {

  private final Class<Persistable<NodeRef>> domainClass;
  // private final NodePropertiesMapper<?> nodeMapper;
  // private final EntityPropertiesMapper entityMapper;
  private final AlfrescoEntityInvoker invoker;

  public RootResourceInformation(Class<Persistable<NodeRef>> domainClass, AlfrescoEntityInvoker invoker) {
    this.domainClass = domainClass;
    // this.nodeMapper = nodeMapper;
    // this.entityMapper = entityMapper;
    this.invoker = invoker;
  }

  public Class<Persistable<NodeRef>> getDomainClass() {
    return domainClass;
  }

  // public NodePropertiesMapper<?> getNodeMapper() {
  // return nodeMapper;
  // }
  //
  // public EntityPropertiesMapper getEntityMapper() {
  // return entityMapper;
  // }

  public AlfrescoEntityInvoker getInvoker() {
    return invoker;
  }
}
