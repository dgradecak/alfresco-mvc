package com.gradecak.alfresco.mvc.services.workflow;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import com.gradecak.alfresco.mvc.services.service.TenantTaskService;

public class CompleteHandler implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String outcome = (String) execution.getVariable(TenantTaskService.TASK_OUTCOME_FIELD);
    if (!TenantTaskService.COMPLETE_STRING.equals(outcome) && !TenantTaskService.APPROVE_STRING.equals(outcome)) {
      throw new IllegalArgumentException("the task is not completed correctly");
    }
  }
}
