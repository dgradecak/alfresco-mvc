package com.gradecak.alfresco.mvc.webscript;

import org.alfresco.repo.admin.RepositoryEndBootstrapBean;
import org.springframework.context.ApplicationEvent;

/**
 * Created by jancalve on 20/04/16.
 */
public class RepositoryFinishedBootstrapBean extends RepositoryEndBootstrapBean {

  private RepositoryBootstrapListenerManager repositoryBootstrapListenerManager;

  @Override
  protected void onBootstrap(ApplicationEvent event) {
    repositoryBootstrapListenerManager.notifyListeners();
  }

  public void setRepositoryBootstrapListenerManager(RepositoryBootstrapListenerManager repositoryBootstrapListenerManager) {
    this.repositoryBootstrapListenerManager = repositoryBootstrapListenerManager;
  }



}
