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

package com.gradecak.alfresco.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.transaction.TransactionService;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.Mockito;

public class AlfrescoRule implements TestRule {
  protected ServiceRegistry serviceRegistry;
  protected TransactionService transactionService;
  protected PermissionService permissionService;
  protected NodeService nodeService;

  public Statement apply(final Statement base, final Description description) {
    transactionService = mockTransactionService();
    permissionService = mockPermissionService();
    nodeService = mockNodeService();   
    serviceRegistry = mockServiceRegistry();
    return base;
  }

  public ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }

  public TransactionService getTransactionService() {
    return transactionService;
  }

  public PermissionService getPermissionService() {
    return permissionService;
  }

  public NodeService getNodeService() {
    return nodeService;
  }

  protected PermissionService mockPermissionService() {
    final PermissionService permissionService = mock(PermissionService.class);
    when(permissionService.hasPermission(Mockito.any(NodeRef.class), Mockito.eq(PermissionService.READ_PROPERTIES))).thenReturn(AccessStatus.ALLOWED);
    return permissionService;
  }
  
  protected TransactionService mockTransactionService(){
    TransactionService transactionService = mock(TransactionService.class);
    when(transactionService.getRetryingTransactionHelper()).thenReturn(mock(RetryingTransactionHelper.class));
    return transactionService;
  }
  
  protected NodeService mockNodeService() {
    NodeService nodeService = mock(NodeService.class);
    when(nodeService.exists(Mockito.any(NodeRef.class))).thenReturn(true);
    return nodeService;
  }

  protected ServiceRegistry mockServiceRegistry() {
    ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
    when(serviceRegistry.getNodeService()).thenReturn(nodeService);
    when(serviceRegistry.getTransactionService()).thenReturn(transactionService);
    when(serviceRegistry.getPermissionService()).thenReturn(permissionService);
    return serviceRegistry;
  }
  
  protected ActivitiScriptNode createdActivitiScriptNode(final NodeRef nodeRef) {
    return mock(ActivitiScriptNode.class, withSettings().serializable());
  }
}
