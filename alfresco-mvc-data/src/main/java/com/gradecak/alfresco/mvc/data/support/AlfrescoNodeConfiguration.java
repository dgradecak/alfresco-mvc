package com.gradecak.alfresco.mvc.data.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.data.domain.Persistable;
import org.springframework.util.Assert;

import com.gradecak.alfresco.mvc.data.mapper.BeanEntityMapper;

public class AlfrescoNodeConfiguration {
  private Map<String, Class<?>> domains = new HashMap<>();
  private Map<Class<?>, String> classes = new HashMap<>();

  private final List<BeanEntityMapper<?>> mappers;
  private final ServiceRegistry serviceRegistry;

  public AlfrescoNodeConfiguration(List<BeanEntityMapper<?>> mappers, ServiceRegistry serviceRegistry) {
    this.mappers = mappers;
    this.serviceRegistry = serviceRegistry;
  }

  public void addDomainMapper(String mapping, Class<?> domainClass) {
    domains.put(mapping, domainClass);
    classes.put(domainClass, mapping);
  }

  public Class<?> getDomainClass(String mapping) {
    return domains.get(mapping);
  }

  public String getMapping(Class<?> clazz) {
    return classes.get(clazz);
  }

  public <T extends Persistable<NodeRef>> BeanEntityMapper<T> findBestMatch(final QName type) {
    Assert.notNull(type);

    BeanEntityMapper<T> bestMatchMapper = null;
    for (BeanEntityMapper<?> mapper : mappers) {
      QName supportsNodeType = mapper.supportsNodeType();
      if (type.equals(supportsNodeType)) {
        bestMatchMapper = (BeanEntityMapper<T>) mapper;
        break;
      }
    }

    if (bestMatchMapper == null) {
      for (BeanEntityMapper<?> mapper : mappers) {
        QName supportsNodeType = mapper.supportsNodeType();
        if (serviceRegistry.getDictionaryService().isSubClass(type, supportsNodeType)) {
          bestMatchMapper = (BeanEntityMapper<T>) mapper;
          break;
        }
      }
    }

    return bestMatchMapper;
  }
}
