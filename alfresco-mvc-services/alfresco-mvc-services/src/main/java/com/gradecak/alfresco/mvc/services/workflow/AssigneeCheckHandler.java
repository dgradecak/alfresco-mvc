package com.gradecak.alfresco.mvc.services.workflow;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.identity.Authentication;
import org.springframework.util.StringUtils;

import com.gradecak.alfresco.mvc.services.service.TenantTaskService;

public class AssigneeCheckHandler implements TaskListener {

  public void notify(DelegateTask delegateTask) {

    String assignee = delegateTask.getAssignee();
    if (!StringUtils.hasText(assignee)) {
      throw new IllegalArgumentException("The task is not assigned. You have to claim it before actioning upon it.");
    }

    String authenticatedUser = Authentication.getAuthenticatedUserId();
    if(!assignee.equals(authenticatedUser)) {
      throw new IllegalArgumentException("The task is claimed by someone else. You have to claim it before actioning upon it.");
    }
    
    
    String initiator = (String) delegateTask.getExecution().getVariable(TenantTaskService.INITIATOR_AUTHORITY);
    if(initiator.equals(authenticatedUser)) {
      delegateTask.getExecution().setVariable(TenantTaskService.REVIEW_STEP, Boolean.FALSE);
    }
    
  }
}
