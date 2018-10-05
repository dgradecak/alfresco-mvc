package com.gradecak.alfresco.querytemplate;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.util.Assert;

public class BeanPropertiesMapperRegistry {

  private final NamespaceService namespaceService;
  private final DictionaryService dictionaryService;

  private Map<Class<?>, BeanPropertiesMapper<?>> mappers = new HashMap<>();

  public BeanPropertiesMapperRegistry(final NamespaceService namespaceService, final DictionaryService dictionaryService) {
    Assert.notNull(namespaceService, "[Assertion failed] - the namespaceService argument must be null");
    Assert.notNull(dictionaryService, "[Assertion failed] - the dictionaryService argument must be null");

    this.namespaceService = namespaceService;
    this.dictionaryService = dictionaryService;
  }

  @SuppressWarnings("unchecked")
  public <T> BeanPropertiesMapper<T> getForClass(Class<T> clazz) {
    BeanPropertiesMapper<T> mapper = (BeanPropertiesMapper<T>) mappers.get(clazz);
    if (mapper == null) {
      mapper = new BeanPropertiesMapperBuilder<T>().mappedClass(clazz).namespaceService(namespaceService).dictionaryService(dictionaryService).build();

      addBeanPropertiesMapper(mapper);
    }
    return mapper;
  }

  public BeanPropertiesMapperRegistry addBeanPropertiesMappers(BeanPropertiesMapper<?>... mappers) {
    for (BeanPropertiesMapper<?> mapper : mappers) {
      addBeanPropertiesMapper(mapper);
    }
    return this;
  }

  public BeanPropertiesMapperRegistry addBeanPropertiesMapper(BeanPropertiesMapper<?> mapper) {
    Assert.notNull(mapper, "[Assertion failed] - the mapper argument must be null");
    Class<?> clazz = mapper.getMappedClass();
    BeanPropertiesMapper<?> existingMapper = mappers.get(clazz);
    if (existingMapper != null) {
      throw new RuntimeException("A mapper is alread configured for the class: " + clazz.getName());
    }

    mappers.put(clazz, mapper);

    return this;
  }
}
