package com.gradecak.alfresco.mvc.data.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.transaction.PlatformTransactionManager;

import com.gradecak.alfresco.mvc.data.support.AlfrescoRepositoriesRegistrar;
import com.gradecak.alfresco.mvc.data.support.AlfrescoRepositoryFactoryBean;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(AlfrescoRepositoriesRegistrar.class)
public @interface EnableAlfrescoRepositories {

  String[] value() default {};
  
  String[]basePackages() default {};

  Class<?>repositoryFactoryBeanClass() default AlfrescoRepositoryFactoryBean.class;

  Class<?>[] basePackageClasses() default {};
  
  /**
   * Specifies which types are eligible for component scanning. Further narrows the set of candidate components from
   * everything in {@link #basePackages()} to everything in the base packages that matches the given filter or filters.
   */
  Filter[] includeFilters() default {};

  /**
   * Specifies which types are not eligible for component scanning.
   */
  Filter[] excludeFilters() default {};

  /**
   * Returns the postfix to be used when looking up custom repository implementations. Defaults to {@literal Impl}. So
   * for a repository named {@code PersonRepository} the corresponding implementation class will be looked up scanning
   * for {@code PersonRepositoryImpl}.
   * 
   * @return
   */
  String repositoryImplementationPostfix() default "Impl";

  /**
   * Configures the location of where to find the Spring Data named queries properties file. Will default to
   * {@code META-INFO/jpa-named-queries.properties}.
   * 
   * @return
   */
  String namedQueriesLocation() default "";

  /**
   * Returns the key of the {@link QueryLookupStrategy} to be used for lookup queries for query methods. Defaults to
   * {@link Key#CREATE_IF_NOT_FOUND}.
   * 
   * @return
   */
  Key queryLookupStrategy() default Key.CREATE_IF_NOT_FOUND;

  // JPA specific configuration
  /**
   * Configures the name of the {@link EntityManagerFactory} bean definition to be used to create repositories
   * discovered through this annotation. Defaults to {@code entityManagerFactory}.
   * 
   * @return
   */
  String entityManagerFactoryRef() default "entityManagerFactory";

  /**
   * Configures the name of the {@link PlatformTransactionManager} bean definition to be used to create repositories
   * discovered through this annotation. Defaults to {@code transactionManager}.
   * 
   * @return
   */
  String transactionManagerRef() default "transactionManager";

  /**
   * Configures whether nested repository-interfaces (e.g. defined as inner classes) should be discovered by the
   * repositories infrastructure.
   */
  boolean considerNestedRepositories() default false;
}
