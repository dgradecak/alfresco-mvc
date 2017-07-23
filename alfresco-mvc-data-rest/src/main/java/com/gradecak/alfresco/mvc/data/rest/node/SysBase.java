package com.gradecak.alfresco.mvc.data.rest.node;

import java.util.Date;

import com.gradecak.alfresco.querytemplate.AbstractPersistable;

public class SysBase extends AbstractPersistable {

  private Date cmCreated;
  private String cmCreator;
  private Date cmModified;
  private String cmModifier;
  private Date cmAccessed;

  public Date getCmCreated() {
    return cmCreated;
  }

  public void setCmCreated(Date cmCreated) {
    this.cmCreated = cmCreated;
  }

  public String getCmCreator() {
    return cmCreator;
  }

  public void setCmCreator(String cmCreator) {
    this.cmCreator = cmCreator;
  }

  public Date getCmModified() {
    return cmModified;
  }

  public void setCmModified(Date cmModified) {
    this.cmModified = cmModified;
  }

  public String getCmModifier() {
    return cmModifier;
  }

  public void setCmModifier(String cmModifier) {
    this.cmModifier = cmModifier;
  }
  
  public Date getCmAccessed() {
    return cmAccessed;
  }
  
  public void setCmAccessed(Date cmAccessed) {
    this.cmAccessed = cmAccessed;
  }
}
