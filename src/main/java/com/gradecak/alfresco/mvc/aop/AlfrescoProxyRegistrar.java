package com.gradecak.alfresco.mvc.aop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.framework.autoproxy.BeanFactoryAdvisorRetrievalHelper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.gradecak.alfresco.mvc.annotation.EnableAlfrescoMvcProxy;

public class AlfrescoProxyRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware, BeanFactoryAware {

  public static final String PACKAGE_PROXY_CREATOR_BEAN_NAME = "com.gradecak.alfresco.mvc.aop.internalPackageAutoProxyCreator";

  private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

  private ResourceLoader resourceLoader;
  private Environment environment;
  private BeanFactoryAdvisorRetrievalHelper advisorRetrievalHelper;
  private ConfigurableListableBeanFactory beanFactory;

  private AnnotationAttributes attributes;
  private AnnotationMetadata metadata;
  private final Collection<Advisor> advisors = new ArrayList<>();

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
      throw new IllegalStateException("Cannot use AlfrescoProxyRegistrar without a ConfigurableListableBeanFactory");
    }
    initBeanFactory((ConfigurableListableBeanFactory) beanFactory);
  }

  public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {

    Assert.notNull(resourceLoader, "ResourceLoader must not be null!");
    Assert.notNull(annotationMetadata, "AnnotationMetadata must not be null!");
    Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");

    // Guard against calls for sub-classes
    if (annotationMetadata.getAnnotationAttributes(EnableAlfrescoMvcProxy.class.getName()) == null) {
      return;
    }

    this.attributes = new AnnotationAttributes(annotationMetadata.getAnnotationAttributes(EnableAlfrescoMvcProxy.class.getName()));
    this.metadata = annotationMetadata;

    Iterable<String> basePackages = getBasePackages();
    for (String basePackage : basePackages) {
      registerOrEscalateApcAsRequired(PackageAutoProxyCreator.class, registry, null, basePackage);
    }
  }

  public Iterable<String> getBasePackages() {

    String[] value = attributes.getStringArray("value");
    String[] basePackages = attributes.getStringArray("basePackages");
    Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");

    // Default configuration - return package of annotated class
    if (value.length == 0 && basePackages.length == 0 && basePackageClasses.length == 0) {
      String className = metadata.getClassName();
      return Collections.singleton(ClassUtils.getPackageName(className));
    }

    Set<String> packages = new HashSet<>();
    packages.addAll(Arrays.asList(value));
    packages.addAll(Arrays.asList(basePackages));

    for (Class<?> typeName : basePackageClasses) {
      packages.add(ClassUtils.getPackageName(typeName));
    }

    return packages;
  }

  private static BeanDefinition registerOrEscalateApcAsRequired(Class cls, BeanDefinitionRegistry registry, Object source, String basePackage) {
    Assert.notNull(registry, "BeanDefinitionRegistry must not be null");

    String proxyPackageBeanName = PACKAGE_PROXY_CREATOR_BEAN_NAME + "." + basePackage;
    if (registry.containsBeanDefinition(proxyPackageBeanName)) {
      return null;
    }

    RootBeanDefinition beanDefinition = new RootBeanDefinition(cls);
    beanDefinition.setSource(source);
    beanDefinition.getPropertyValues().add("order", Ordered.HIGHEST_PRECEDENCE);
    beanDefinition.getPropertyValues().add("basePackage", basePackage);
    beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
    registry.registerBeanDefinition(proxyPackageBeanName, beanDefinition);
    return beanDefinition;
  }

  protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    this.advisorRetrievalHelper = new BeanFactoryAdvisorRetrievalHelper(beanFactory);
    this.beanFactory = beanFactory;
  }

  // protected void createProxiedBeans(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry) {
  // String[] alfrescoMvcAdvisors = PackageAutoProxyCreator.DEFAULT_INTERCEPTORS;
  //
  // for (String beanName : alfrescoMvcAdvisors) {
  // // Advisor advisorInstance = beanFactory.getBean(advisor, Advisor.class);
  // // if (advisorInstance == null) {
  // // throw new RuntimeException("Alfresco @MVC default advisor could not be found in the bean factory");
  // // }
  //
  //// if (beanFactory == null || !beanFactory.isCurrentlyInCreation(beanName)) {
  //// CoreDocumentService bean = beanFactory.getBean(CoreDocumentService.class);
  //// Object next = beanFactory.getBean(beanName);
  //// advisors.add(this.advisorAdapterRegistry.wrap(next));
  //// }
  // }
  //
  // Collection<BeanDefinition> candidates = getCandidates(resourceLoader, registry);
  // for (BeanDefinition beanDefinition : candidates) {
  // CoreDocumentService bean = beanFactory.getBean(CoreDocumentService.class);
  // System.out.println("REAL bean "+bean);
  // //registry.registerBeanDefinition(beanDefinition.get, beanDefinition);
  // }
  // }

  // private Advisor[] resolveInterceptorNames() {
  // String[] alfrescoMvcAdvisors = PackageAutoProxyCreator.DEFAULT_INTERCEPTORS;
  //
  // ConfigurableBeanFactory cbf = (this.beanFactory instanceof ConfigurableBeanFactory) ?
  // (ConfigurableBeanFactory) this.beanFactory : null;
  // List<Advisor> advisors = new ArrayList<Advisor>();
  // for (String beanName : alfrescoMvcAdvisors) {
  // if (cbf == null || !cbf.isCurrentlyInCreation(beanName)) {
  // Object next = this.beanFactory.getBean(beanName);
  // advisors.add(this.advisorAdapterRegistry.wrap(next));
  // }
  // }
  // return advisors.toArray(new Advisor[advisors.size()]);
  // }

  // public Collection<BeanDefinition> getCandidates(ResourceLoader loader, BeanDefinitionRegistry registry) {
  //
  // ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
  // scanner.setResourceLoader(loader);
  // scanner.setEnvironment(environment);
  //
  // // for (TypeFilter filter : getExcludeFilters()) {
  // // scanner.addExcludeFilter(filter);
  // // }
  //
  // Set<BeanDefinition> result = new HashSet<BeanDefinition>();
  //
  // for (String basePackage : getBasePackages()) {
  // Set<BeanDefinition> candidate = scanner.findCandidateComponents(basePackage);
  // result.addAll(candidate);
  // }
  //
  // return result;
  // }

  //
  // protected Object createProxy(Class<?> beanClass, String beanName, Object[] specificInterceptors, TargetSource
  // targetSource) {
  //
  // ProxyFactory proxyFactory = new ProxyFactory();
  // // Copy our properties (proxyTargetClass etc) inherited from ProxyConfig.
  // // proxyFactory.copyFrom(this);
  //
  // // if (!shouldProxyTargetClass(beanClass, beanName)) {
  // // // Must allow for introductions; can't just set interfaces to
  // // // the target's interfaces only.
  // // Class<?>[] targetInterfaces = ClassUtils.getAllInterfacesForClass(beanClass, this.proxyClassLoader);
  // // for (Class<?> targetInterface : targetInterfaces) {
  // // proxyFactory.addInterface(targetInterface);
  // // }
  // // }
  //
  // proxyFactory.addAdvisors(advisors);
  //
  // proxyFactory.setTargetSource(targetSource);
  // proxyFactory.setProxyTargetClass(true);
  // proxyFactory.setFrozen(true);
  // proxyFactory.setPreFiltered(true);
  //
  // customizeProxyFactory(proxyFactory);
  //
  // return proxyFactory.getProxy(ClassUtils.getDefaultClassLoader());
  // }
  //
  // protected void customizeProxyFactory(ProxyFactory proxyFactory) {
  // // TODO Auto-generated method stub

  // }
}
