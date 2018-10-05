package com.gradecak.alfresco.querytemplate;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.util.Assert;

public class BeanPropertiesMapperBuilder<T> {

  private Class<T> mappedClass;
  private BeanPropertiesMapperConfigurer<T> configurer;
  private boolean reportNamespaceException = false;
  private NamespaceService namespaceService;
  private DictionaryService dictionaryService;

  public BeanPropertiesMapperBuilder<T> reportNamespaceException(final boolean reportNamespaceException) {
    this.reportNamespaceException = reportNamespaceException;
    return this;
  }

  public BeanPropertiesMapperBuilder<T> namespaceService(final NamespaceService namespaceService) {
    this.namespaceService = namespaceService;
    return this;
  }

  public BeanPropertiesMapperBuilder<T> dictionaryService(final DictionaryService dictionaryService) {
    this.dictionaryService = dictionaryService;
    return this;
  }

  public BeanPropertiesMapperBuilder<T> mappedClass(Class<T> mappedClass) {
    this.mappedClass = mappedClass;
    return this;
  }

  public BeanPropertiesMapperBuilder<T> configurer(BeanPropertiesMapperConfigurer<T> configurer) {
    this.configurer = configurer;
    return this;
  }

  public BeanPropertiesMapper<T> build() {
    Assert.notNull(namespaceService, "A namespaceService is required.");
    Assert.notNull(dictionaryService, "A dictionaryService is required.");
    Assert.notNull(mappedClass, "A mappedClass is required.");

    BeanPropertiesMapper<T> mapper = new BeanPropertiesMapper<T>(namespaceService, dictionaryService, configurer, reportNamespaceException);
    mapper.setMappedClass(mappedClass);
    return mapper;

  }

}
