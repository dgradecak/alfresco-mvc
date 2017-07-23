package com.gradecak.alfresco.mvc.services.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.hateoas.core.Relation;

@Relation(value="workflow", collectionRelation="workflows")
public class CoreWorkflow {

  private String id;
  private String name;
  private String description;
  private String definition;
  private String status;
  private Date startDate;
  private String creator;
  private Date dueDate;
  private Date completionDate;
  private String deleteReason;
  private Map<String, String> related;
  private Map<String, Boolean> permissions = new HashMap<>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDefinition() {
    return definition;
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public Date getDueDate() {
    return dueDate;
  }

  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  public Date getCompletionDate() {
    return completionDate;
  }

  public void setCompletionDate(Date completionDate) {
    this.completionDate = completionDate;
  }
  
  public String getDeleteReason() {
    return deleteReason;
  }
  
  public void setDeleteReason(String deleteReason) {
    this.deleteReason = deleteReason;
  }
  
  public Map<String, String> getRelated() {
    return related;
  }
  
  public void setRelated(Map<String, String> related) {
    this.related = related;
  }
  
  public Map<String, Boolean> getPermissions() {
    return permissions;
  }

  public void setPermissions(Map<String, Boolean> permissions) {
    this.permissions.clear();
    this.permissions.putAll(permissions);
  }
}
