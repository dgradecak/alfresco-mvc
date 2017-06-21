package com.gradecak.alfresco.mvc.data.mapper;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.data.domain.Persistable;

public interface EntityPropertiesMapper<T extends Persistable<NodeRef>, ID extends Serializable> {

  public Map<QName, Serializable> mapEntity(ID id, T entity);
}
