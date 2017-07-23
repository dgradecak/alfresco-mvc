package com.gradecak.alfresco.mvc.services.workflow;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.util.StringUtils;

import com.gradecak.alfresco.mvc.services.service.TenantTaskService;

public class ByPassHandler implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String outcome = (String) execution.getVariable(TenantTaskService.TASK_OUTCOME_FIELD);
    if (!TenantTaskService.BY_PASS_STRING.equals(outcome) && !TenantTaskService.REJECT_STRING.equals(outcome)) {
      throw new IllegalArgumentException("the task is not bypassed correctly");
    }
    
    String reason = (String) execution.getVariable(TenantTaskService.TASK_REASON_FIELD);
    if (!StringUtils.hasText(reason)) {
      throw new IllegalArgumentException("comment must be provided");
    }
  }
}
