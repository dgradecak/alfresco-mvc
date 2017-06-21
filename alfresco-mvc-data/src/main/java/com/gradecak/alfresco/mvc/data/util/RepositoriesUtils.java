package com.gradecak.alfresco.mvc.data.util;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.AnnotationRepositoryMetadata;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.util.StringUtils;

public class RepositoriesUtils {

  /**
   * Resolves the domain type from the given type. Will resolve the repository domain type if the given type is a
   * repository or return the type as is if not.
   * 
   * @param type must not be {@literal null}.
   * @return
   */
  public static Class<?> getDomainType(Class<?> type) {

    if (!isRepositoryInterface(type)) {
      return type;
    }

    return getMetadataFor(type).getDomainType();
  }

  public static boolean isRepositoryInterface(Class<?> type) {
    return Repository.class.isAssignableFrom(type) || AnnotationUtils.findAnnotation(type, RepositoryDefinition.class) != null;
  }

  private static RepositoryMetadata getMetadataFor(Class<?> type) {
    return Repository.class.isAssignableFrom(type) ? new DefaultRepositoryMetadata(type) : new AnnotationRepositoryMetadata(type);
  }
  
  public static String getDefaultPathFor(Class<?> type) {
    return getSimpleTypeName(type);
  }

  private static String getSimpleTypeName(Class<?> type) {
    return StringUtils.uncapitalize(type.getSimpleName());
  }
}
