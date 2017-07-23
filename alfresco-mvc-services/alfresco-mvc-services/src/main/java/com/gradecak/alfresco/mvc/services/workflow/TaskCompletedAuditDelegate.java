package com.gradecak.alfresco.mvc.services.workflow;

import java.io.Serializable;
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
import com.gradecak.alfresco.mvc.services.service.NoteService;
import com.gradecak.alfresco.mvc.services.service.TenantEntityContainerService;
import com.gradecak.alfresco.mvc.services.service.TenantTaskService;
import com.gradecak.alfresco.mvc.services.service.TenantWorkflowService;

public class TaskCompletedAuditDelegate extends BaseJavaDelegate implements TaskListener {
  
  private final TenantEntityContainerService entityContainerService;
  private final NoteService noteService;

  @Autowired
  public TaskCompletedAuditDelegate(TenantEntityContainerService entityContainerService, NoteService noteService) {
    Assert.notNull(entityContainerService);
    Assert.notNull(noteService);

    this.entityContainerService = entityContainerService;
    this.noteService = noteService;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    // NOT used
  }

  @Override
  public void notify(DelegateTask delegateTask) {
    
    Map<String, Object> processVariables = delegateTask.getVariables();

    String entity = (String) processVariables.get(TenantWorkflowService.IBAPP_ENTITY_FIELD);
    String primaryKey = (String) processVariables.get(TenantWorkflowService.IBAPP_PRIMARYKEY_FIELD);

    if (StringUtils.hasText(entity) && StringUtils.hasText(primaryKey)) {
      NodeRef packageRef = (NodeRef) processVariables.get(TenantWorkflowService.RELATED_FIELD);
      if (packageRef != null) {
        String outcome = (String) processVariables.get(TenantTaskService.TASK_OUTCOME_FIELD);
        Assert.hasText(outcome);
        
        String comment = (String) processVariables.get(TenantTaskService.TASK_REASON_FIELD);

        Map<String, Serializable> map = ImmutableMap.of("type", "task", "action", outcome, "id", delegateTask.getId(), "comment", comment != null ? comment : "");
        entityContainerService.createSystemNote(packageRef, map);
        
        NodeRef workflowPackageRef = (NodeRef) processVariables.get(TenantWorkflowService.PACKAGE_FIELD);
        noteService.createSystemNote(workflowPackageRef, map);
        
        delegateTask.setVariableLocal(TenantTaskService.TASK_OUTCOME_FIELD, outcome);
      }
    }       

    // String workflowId =delegateTask.getExecutionId();
    // String taskId = delegateTask.getId();
    //
    // //activitiPropertyConverter.setDefaultTaskProperties(delegateTask);
    // Map<QName, Serializable> pathProperties =
    // activitiPropertyConverter.getPathProperties(delegateTask.getExecutionId());
    //
    // auditLoggerService.audit(new TaskEvent(workflowId, taskId, delegateTask.getName(), delegateTask.getAssignee(),
    // null, null, TaskEvent.TASKCOMPLETED, pathProperties));
    // delegateTask.setVariableLocal("sdwf_audit", Boolean.TRUE);
  }
}
