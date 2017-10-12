package com.gradecak.alfresco.mvc.bootstrap;


import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.admin.RepositoryEndBootstrapBean;
import org.alfresco.repo.admin.RepositoryState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Created by mauro1855 on 11/10/2017.
 */
public class MVCBootstrapper extends RepositoryEndBootstrapBean implements ServletContextAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherWebscript.class);

  private DispatcherWebscript dispatcherWebscript;
  private ServletContext servletContext;
  private String contextConfigLocation;

  @Override
  public void onBootstrap(ApplicationEvent event) {

    LOGGER.info("Starting Alfresco MVC...");

    RepositoryState repositoryState = getRepositoryState();
    if(repositoryState.isBootstrapping())
      throw new AlfrescoRuntimeException("Alfresco MVC cannot auto proxy while the repository is bootstrapping");

    dispatcherWebscript = (DispatcherWebscript) getApplicationContext().getBean("mvc.dispatcherWebscript");
    try {
      dispatcherWebscript.initialize(getApplicationContext(), servletContext, contextConfigLocation);
    } catch(ServletException e) {
      LOGGER.info("Error initializing Alfresco MVC dispatcher webscript");
    }
  }

  @Override
  public void setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  public void setContextConfigLocation(String contextConfigLocation) {
    this.contextConfigLocation = contextConfigLocation;
  }
}
