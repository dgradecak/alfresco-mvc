package com.gradecak.alfresco.mvc.mapper;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.namespace.QName;

public interface NodePropertiesMapper<T> {

  public T mapNodeProperties(Map<QName, Serializable> properties);
}
