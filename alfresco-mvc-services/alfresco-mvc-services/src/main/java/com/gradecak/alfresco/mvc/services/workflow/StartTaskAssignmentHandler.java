package com.gradecak.alfresco.mvc.services.workflow;

import java.util.Date;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.identity.Authentication;
import org.alfresco.service.cmr.security.PermissionService;
import org.springframework.util.StringUtils;

import com.gradecak.alfresco.mvc.services.service.TenantTaskService;
import com.gradecak.alfresco.mvc.services.service.TenantWorkflowService;

public class StartTaskAssignmentHandler implements TaskListener {

  public void notify(DelegateTask delegateTask) {
    delegateTask.setDueDate((Date) delegateTask.getExecution().getVariable(TenantWorkflowService.IBAPP_WORKFLOW_DUEDATE));
    
    // delegateTask.setPriority((Integer) delegateTask.getExecution().getVariable("bpm_workflowPriority"));
    //
    // delegateTask.getExecution().setVariable("bpm_comment", null);

    String initiator = (String) delegateTask.getExecution().getVariable(TenantTaskService.INITIATOR_AUTHORITY);
    if (!StringUtils.hasText(initiator)) {
      delegateTask.getExecution().setVariable(TenantTaskService.INITIATOR_AUTHORITY, Authentication.getAuthenticatedUserId());
    }
    
    delegateTask.getExecution().setVariable(TenantTaskService.REVIEW_STEP, Boolean.TRUE);
    
    String initialAssignee = (String) delegateTask.getExecution().getVariable(TenantTaskService.INITIAL_AUTHORITY);
    if (StringUtils.hasText(initialAssignee)) {
      if (initialAssignee.startsWith(PermissionService.GROUP_PREFIX)) {
        delegateTask.addCandidateGroup(initialAssignee);
      } else {
        delegateTask.setAssignee(initialAssignee);
      }

      return;
    }

    // String lastActor = (String) delegateTask.getExecution().getVariable("sdwf_lastActor");
    // if (StringUtils.hasText(lastActor)) {
    // delegateTask.setAssignee(lastActor);
    // return;
    // }

    throw new RuntimeException("Cannot assign the task, no group or user provided.");
  }
}
