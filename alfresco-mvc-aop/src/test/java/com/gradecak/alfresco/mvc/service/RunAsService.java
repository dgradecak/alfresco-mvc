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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gradecak.alfresco.mvc.annotation.AlfrescoRunAs;

@Service
public class RunAsService {

  @Autowired
  private ServiceRegistry serviceRegistry;

  @AlfrescoRunAs("user")
  public String getNamePropertyAsUser(final NodeRef nodeRef) {
    Assert.assertEquals("user", AuthenticationUtil.getRunAsUser());
    return (String) serviceRegistry.getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME);
  }
  
  @AlfrescoRunAs(AuthenticationUtil.SYSTEM_USER_NAME)
  public String getNamePropertyAsSystem(final NodeRef nodeRef) {
    Assert.assertEquals(AuthenticationUtil.SYSTEM_USER_NAME, AuthenticationUtil.getRunAsUser());
    return (String) serviceRegistry.getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME);
  }
}
