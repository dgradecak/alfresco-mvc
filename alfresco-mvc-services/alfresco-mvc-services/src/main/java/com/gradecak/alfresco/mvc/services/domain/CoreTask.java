package com.gradecak.alfresco.mvc.services.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.hateoas.core.Relation;

@Relation(value="task", collectionRelation="tasks")
public class CoreTask {

  private String id;
  private String name;
  private String description;
  private String status;
  private String priority;
  private Date startDate;
  private Date completionDate;
  private String assignee;
  private Date dueDate;
  private String outcome;
  private String processId;
  private Map<String, Boolean> actions = new HashMap<>();
  private CoreWorkflow workflow;
  private Map<String, String> related;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getCompletionDate() {
    return completionDate;
  }

  public void setCompletionDate(Date completionDate) {
    this.completionDate = completionDate;
  }

  public String getOutcome() {
    return outcome;
  }

  public void setOutcome(String outcome) {
    this.outcome = outcome;
  }

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getDueDate() {
    return dueDate;
  }

  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  public String getProcessId() {
    return processId;
  }

  public void setProcessId(String processId) {
    this.processId = processId;
  }
  
  public CoreWorkflow getWorkflow() {
    return workflow;
  }
  
  public void setWorkflow(CoreWorkflow workflow) {
    this.workflow = workflow;
  }
  
  public Map<String, Boolean> getActions() {
    return actions;
  }
  
  public void setActions(Map<String, Boolean> actions) {
    this.actions.clear();
    this.actions.putAll(actions);
  }
  
  public Map<String, String> getRelated() {
    return related;
  }
  
  public void setRelated(Map<String, String> related) {
    this.related = related;
  }
}
