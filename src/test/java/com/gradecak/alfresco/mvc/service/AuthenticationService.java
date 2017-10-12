/**
 * Copyright gradecak.com

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradecak.alfresco.mvc.service;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import com.gradecak.alfresco.mvc.annotation.AuthenticationType;

@Service
public class AuthenticationService {

  @Autowired
  private ServiceRegistry serviceRegistry;

  @AlfrescoAuthentication
  public String getNamePropertyAsDefault(final NodeRef nodeRef) {
    return (String) serviceRegistry.getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME);
  }
  
  @AlfrescoAuthentication(AuthenticationType.USER)
  public String getNamePropertyAsUser(final NodeRef nodeRef) {
    return (String) serviceRegistry.getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME);
  }
  
  @AlfrescoAuthentication(AuthenticationType.ADMIN)
  public String getNamePropertyAsAdmin(final NodeRef nodeRef) {
    return (String) serviceRegistry.getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME);
  }
  
  @AlfrescoAuthentication(AuthenticationType.NONE)
  public String getNamePropertyAsNone(final NodeRef nodeRef) {
    return (String) serviceRegistry.getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME);
  }
}
