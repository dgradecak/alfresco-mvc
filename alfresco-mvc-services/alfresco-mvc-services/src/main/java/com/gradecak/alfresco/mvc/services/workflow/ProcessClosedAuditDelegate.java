package com.gradecak.alfresco.mvc.services.workflow;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.gradecak.alfresco.mvc.services.service.TenantEntityContainerService;
import com.gradecak.alfresco.mvc.services.service.TenantWorkflowService;

public class ProcessClosedAuditDelegate extends BaseJavaDelegate implements ExecutionListener {
  
  private final TenantEntityContainerService entityContainerService;

  @Autowired
  public ProcessClosedAuditDelegate(TenantEntityContainerService entityContainerService) {
    Assert.notNull(entityContainerService);

    this.entityContainerService = entityContainerService;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    // NOT used
  }

  @Override
  public void notify(DelegateExecution execution) {
    //ExecutionEntity ee = (ExecutionEntity) execution;
    
    Map<String, Object> processVariables = execution.getVariables();

    String entity = (String) processVariables.get(TenantWorkflowService.IBAPP_ENTITY_FIELD);
    String primaryKey = (String) processVariables.get(TenantWorkflowService.IBAPP_PRIMARYKEY_FIELD);

    if (StringUtils.hasText(entity) && StringUtils.hasText(primaryKey)) {
      NodeRef packageRef = (NodeRef) processVariables.get(TenantWorkflowService.RELATED_FIELD);
      if (packageRef != null) {
        String description = (String) processVariables.get(TenantWorkflowService.IBAPP_WORKFLOW_DESCRIPTION);
        Assert.hasText(description);

        entityContainerService.createSystemNote(packageRef, ImmutableMap.of("type", "workflow", "action", "closed", "id", execution.getProcessInstanceId(), "description", description));
      }
    }

    // if ("cancelled".equals(ee.getDeleteReason())) {
    // auditLoggerService.audit(new WorkflowEvent(execution.getId(), null, /* pckg */ null, pathProperties, null,
    // WorkflowEvent.WORKFLOWCLOSED));
    // } else {
    // auditLoggerService.audit(new WorkflowEvent(execution.getId(), null, /* pckg */ null, pathProperties, null,
    // WorkflowEvent.WORKFLOWCANCELED));
    // }
  }

  // public void setActivitiPropertyConverter(ActivitiPropertyConverter activitiPropertyConverter) {
  // this.activitiPropertyConverter = activitiPropertyConverter;
  // }
}
