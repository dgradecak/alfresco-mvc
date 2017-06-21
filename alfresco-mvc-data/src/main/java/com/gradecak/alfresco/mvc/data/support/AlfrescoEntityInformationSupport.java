package com.gradecak.alfresco.mvc.data.support;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.support.AbstractEntityInformation;
import org.springframework.util.Assert;

public class AlfrescoEntityInformationSupport<T extends Persistable<NodeRef>> extends AbstractEntityInformation<T, NodeRef>implements EntityInformation<T, NodeRef> {
  
  public AlfrescoEntityInformationSupport(Class<T> domainClass) {
    super(domainClass);
  }

  @Override
  public NodeRef getId(T entity) {
    return entity.getId();
  }

  @Override
  public Class<NodeRef> getIdType() {
    return NodeRef.class;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static <T> EntityInformation<T, NodeRef> getMetadata(Class<T> domainClass) {

      Assert.notNull(domainClass);

      if (Persistable.class.isAssignableFrom(domainClass)) {
          return new AlfrescoEntityInformationSupport(domainClass);
      } else {
          //return new JpaMetamodelEntityInformation(domainClass);
        throw new RuntimeException("not implemented");
      }
  }
  
}