package com.gradecak.alfresco.mvc.mapper;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.namespace.QName;

public interface NodeMapper<T> {

	public T mapNode(Map<QName, Serializable> properties);
}
