package com.gradecak.alfresco.mvc.data.annotation;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface AlfrescoNodeCreator<T> {

  public NodeRef create(T entity, Map<QName, Serializable> properties);
}
