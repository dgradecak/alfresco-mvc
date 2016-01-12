package com.gradecak.alfresco.mvc.mapper;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface NodePropertiesMapper<T> {

  public T mapNodeProperties(NodeRef nodeRef, Map<QName, Serializable> properties);
}
