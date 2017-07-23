package com.gradecak.alfresco.mvc.services.workflow;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNameCache;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

public class WorkflowQNameConverter implements InitializingBean {

  private static final int MAX_QNAME_CACHE_SIZE = 5000;
  private QNameCache cache = new QNameCache(MAX_QNAME_CACHE_SIZE);  

  private final ServiceRegistry serviceRegistry;
  
  public WorkflowQNameConverter(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    //this.prefixResolver = serviceRegistry.getNamespaceService();
  }

  public String mapQNameToName(QName qName) {
    String name = cache.getName(qName);
    if (name == null) {
      name = convertQNameToName(qName);
      cache.putQNameToName(qName, name);
      cache.putNameToQName(name, qName);
    }
    return name;
  }

  public QName mapNameToQName(String name) {
    QName qName = cache.getQName(name);
    if (qName == null) {
      qName = convertNameToQName(name);
      cache.putNameToQName(name, qName);
      cache.putQNameToName(qName, name);
    }
    return qName;
  }

  public void clearCache() {
    cache.clear();
  }

  private QName convertNameToQName(String name) {
    if (name.indexOf(QName.NAMESPACE_BEGIN) == 0) {
      return QName.createQName(name);
    }
    String qName = name;
    if (name.indexOf(QName.NAMESPACE_PREFIX) == -1) {
      if (name.indexOf('_') == -1) {
        return QName.createQName(NamespaceService.DEFAULT_URI, name);
      }
      qName = name.replaceFirst("_", ":");
    }
    try {
      return QName.createQName(qName, serviceRegistry.getNamespaceService());
    } catch (NamespaceException ne) {
      return QName.createQName(NamespaceService.DEFAULT_URI, name);
    }
  }

  private String convertQNameToName(QName name) {
    // NOTE: Map names using old conversion scheme (i.e. : -> _) as well as new scheme (i.e. } -> _)
    String nameStr = name.toPrefixString(serviceRegistry.getNamespaceService());
    if (nameStr.indexOf('_') != -1 && nameStr.indexOf('_') < nameStr.indexOf(':')) {
      return name.toString();
    }
    return nameStr.replace(':', '_');
  }
}
