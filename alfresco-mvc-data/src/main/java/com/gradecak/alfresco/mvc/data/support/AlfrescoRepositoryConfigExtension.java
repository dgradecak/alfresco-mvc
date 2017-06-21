package com.gradecak.alfresco.mvc.data.support;

import org.alfresco.service.ServiceRegistry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.config.RepositoryConfigurationSource;

import com.gradecak.alfresco.mvc.aop.AlfrescoProxyRegistrar;
import com.gradecak.alfresco.mvc.aop.PackageAutoProxyCreator;
import com.gradecak.alfresco.mvc.data.service.AlfrescoEntityService;

public class AlfrescoRepositoryConfigExtension extends RepositoryConfigurationExtensionSupport {

  private static final String DEFAULT_TRANSACTION_MANAGER_BEAN_NAME = "transactionManager";

  private final ServiceRegistry serviceRegistry;
  
  public AlfrescoRepositoryConfigExtension(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }
  
  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config14.RepositoryConfigurationExtension#getRepositoryInterface()
   */
  public String getRepositoryFactoryClassName() {
    return AlfrescoRepositoryFactoryBean.class.getName();
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config14.RepositoryConfigurationExtensionSupport#getModulePrefix()
   */
  @Override
  protected String getModulePrefix() {
    return "alfresco";
  }

  /*
   * (non-Javadoc)
   * @see
   * org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#postProcess(org.springframework.
   * beans.factory.support.BeanDefinitionBuilder,
   * org.springframework.data.repository.config.RepositoryConfigurationSource)
   */
  @Override
  public void postProcess(BeanDefinitionBuilder builder, RepositoryConfigurationSource source) {

    String transactionManagerRef = source.getAttribute("transactionManagerRef");
    builder.addPropertyValue("transactionManager", transactionManagerRef == null ? DEFAULT_TRANSACTION_MANAGER_BEAN_NAME : transactionManagerRef);
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#registerBeansForRoot(org.
   * springframework.beans.factory.support.BeanDefinitionRegistry,
   * org.springframework.data.repository.config.RepositoryConfigurationSource)
   */
  // // TODO think about tx management, not sure it is needed. related to AlfrescoEntityService
  @Override
  public void registerBeansForRoot(BeanDefinitionRegistry registry, RepositoryConfigurationSource config) {
    AlfrescoProxyRegistrar.registerOrEscalateApcAsRequired(PackageAutoProxyCreator.class, registry, null, AlfrescoEntityService.class.getPackage().getName());
  }

}
