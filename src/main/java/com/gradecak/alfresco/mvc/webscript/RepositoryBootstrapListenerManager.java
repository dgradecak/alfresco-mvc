package com.gradecak.alfresco.mvc.webscript;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jancalve on 20/04/16.
 */
public class RepositoryBootstrapListenerManager {

  private List<RepositoryBootstrapFinishedListener> listeners = new ArrayList<RepositoryBootstrapFinishedListener>();

  public void addListener(RepositoryBootstrapFinishedListener listener) {
    System.out.println("Adding listener..");
    listeners.add(listener);
  }

  public void notifyListeners() {
    System.out.println("Notifying listeners..");
    for (RepositoryBootstrapFinishedListener listener : listeners) {
      listener.onBootstrapFinishedEvent();
    }
  }

}
