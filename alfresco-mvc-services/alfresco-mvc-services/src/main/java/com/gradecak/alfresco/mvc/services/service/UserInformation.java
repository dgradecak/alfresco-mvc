package com.gradecak.alfresco.mvc.services.service;

import java.io.Serializable;
import java.util.Map;

public interface UserInformation {
  public Map<String, Serializable> getUserInformation(final String username);
}
