package com.gradecak.alfresco.mvc.data.annotation;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.data.domain.Persistable;

import com.gradecak.alfresco.mvc.data.mapper.BeanEntityMapper;
import com.gradecak.alfresco.mvc.data.mapper.EntityPropertiesMapper;
import com.gradecak.alfresco.querytemplate.AbstractPersistable;
import com.gradecak.alfresco.querytemplate.NodePropertiesMapper;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AlfrescoNode {

  Class<? extends NodePropertiesMapper<? extends Persistable<NodeRef>>> nodeMapper() default UseBeanPropertiesMapper.class;

  Class<? extends EntityPropertiesMapper<? extends Persistable<NodeRef>, NodeRef>> entityMapper() default UseBeanPropertiesMapper.class;

  Class<? extends AlfrescoNodeCreator<?>> creator() default NoCreator.class;

  public class UseBeanPropertiesMapper extends BeanEntityMapper<AbstractPersistable> {
    public UseBeanPropertiesMapper() {
      super(null);
    }
  }

  public class NoCreator implements AlfrescoNodeCreator<Object> {
    
    public static final NoCreator INSTANCE = new NoCreator();
    
    @Override
    public NodeRef create(Object entity, Map<QName, Serializable> properties) {
      return null;
    }
  }
}
