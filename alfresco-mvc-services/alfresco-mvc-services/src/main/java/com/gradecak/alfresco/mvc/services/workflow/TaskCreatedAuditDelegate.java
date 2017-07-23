package com.gradecak.alfresco.mvc.services.workflow;

import java.util.Date;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.gradecak.alfresco.mvc.services.service.TenantEntityContainerService;
import com.gradecak.alfresco.mvc.services.service.TenantTaskService;
import com.gradecak.alfresco.mvc.services.service.TenantWorkflowService;

public class TaskCreatedAuditDelegate extends BaseJavaDelegate implements TaskListener {

  private final TenantEntityContainerService entityContainerService;

  @Autowired
  public TaskCreatedAuditDelegate(TenantEntityContainerService entityContainerService) {
    Assert.notNull(entityContainerService);

    this.entityContainerService = entityContainerService;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    // NOT used
  }

  @Override
  public void notify(DelegateTask delegateTask) {
    // TODO move in a separate class
    if(delegateTask.getDueDate() == null) { 
      delegateTask.setDueDate((Date) delegateTask.getExecution().getVariable(TenantWorkflowService.IBAPP_WORKFLOW_DUEDATE));
    }
    
    delegateTask.getExecution().setVariable(TenantTaskService.TASK_OUTCOME_FIELD, "");
    
    Map<String, Object> processVariables = delegateTask.getVariables();

    String entity = (String) processVariables.get(TenantWorkflowService.IBAPP_ENTITY_FIELD);
    String primaryKey = (String) processVariables.get(TenantWorkflowService.IBAPP_PRIMARYKEY_FIELD);

    if (StringUtils.hasText(entity) && StringUtils.hasText(primaryKey)) {
      NodeRef packageRef = (NodeRef) processVariables.get(TenantWorkflowService.RELATED_FIELD);
      if (packageRef != null) {
        entityContainerService.createSystemNote(packageRef, ImmutableMap.of("type", "task", "action", "created", "id", delegateTask.getId()));
      }
    }
    
    // String workflowId = delegateTask.getExecutionId();
    // String taskId = delegateTask.getId();
    //
    // //activitiPropertyConverter.setDefaultTaskProperties(delegateTask);
    // Map<QName, Serializable> pathProperties =
    // activitiPropertyConverter.getPathProperties(delegateTask.getExecutionId());
    //
    // auditLoggerService.audit(new TaskEvent(workflowId, taskId, delegateTask.getName(), delegateTask.getAssignee(),
    // null, null, TaskEvent.TASKCREATED, pathProperties));
  }
}
