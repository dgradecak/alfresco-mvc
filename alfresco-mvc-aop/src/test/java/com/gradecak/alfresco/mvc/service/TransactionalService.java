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

import javax.transaction.SystemException;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;

@Service
public class TransactionalService {

  @Autowired
  private ServiceRegistry serviceRegistry;

  @AlfrescoTransaction
  public String transactionWriteWithoutPropagation() {
    return (String) serviceRegistry.getNodeService().getProperty(null, ContentModel.PROP_NAME);
  }
  
  @AlfrescoTransaction(readOnly=true)
  public String transactioReadOnlyWithPropagationRequired() throws SystemException {
    return (String) serviceRegistry.getNodeService().getProperty(null, ContentModel.PROP_NAME);
  }
  
  @AlfrescoTransaction(readOnly=true, propagation=Propagation.REQUIRES_NEW)
  public String transactioReadOnlyWithPropagationRequiresNew() throws SystemException {
    return (String) serviceRegistry.getNodeService().getProperty(null, ContentModel.PROP_NAME);
  }
}
