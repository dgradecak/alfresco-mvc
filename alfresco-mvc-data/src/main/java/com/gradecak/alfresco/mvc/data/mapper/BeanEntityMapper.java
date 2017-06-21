package com.gradecak.alfresco.mvc.data.mapper;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.data.domain.Persistable;
import org.springframework.util.Assert;

import com.gradecak.alfresco.querytemplate.BeanPropertiesMapper;

public class BeanEntityMapper<T extends Persistable<NodeRef>> extends BeanPropertiesMapper<T> implements EntityPropertiesMapper<T, NodeRef> {

  public BeanEntityMapper(final ServiceRegistry serviceRegistry) {
    super(serviceRegistry);
  }
  
  public Class<T> getMappedClass() {
    return this.mappedClass;
  }

  public QName supportsNodeType() {
    return ContentModel.TYPE_BASE;
  }

  @Override
  final public Map<QName, Serializable> mapEntity(NodeRef nodeRef, T entity) {
    Assert.state(this.mappedClass != null, "Mapped class was not specified");
    BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(entity);

    Map<QName, Serializable> properties = new HashMap<>();
    for (Map.Entry<QName, PropertyDescriptor> entry : mappedQNames.entrySet()) {
      QName qName = entry.getKey();
      PropertyDefinition propertyDefinition = serviceRegistry.getDictionaryService().getProperty(qName);
      if (propertyDefinition == null) {
        continue;
      }
      PropertyDescriptor pd = entry.getValue();
      if (pd != null) {
        Serializable value = (Serializable) bw.getPropertyValue(pd.getName());
        if (value != null) {
          properties.put(qName, value);
        }
      }
    }

    configureMappedProperties(entity, properties);
    return properties;
  }

  final public T mapNodeProperties(final NodeRef nodeRef, final Map<QName, Serializable> properties) {
    return super.mapNodeProperties(nodeRef, properties);
  }

  public void configureMappedProperties(T entity, Map<QName, Serializable> properties) {}

  public static <T extends Persistable<NodeRef>> BeanEntityMapper<T> newInstance(Class<T> mappedClass, ServiceRegistry serviceRegistry) {
    BeanEntityMapper<T> newInstance = new BeanEntityMapper<T>(serviceRegistry);
    return newInstance;
  }

}
