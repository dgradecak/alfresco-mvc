package com.gradecak.alfresco.mvc.data.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.google.common.collect.ImmutableMap;
import com.gradecak.alfresco.querytemplate.AbstractPersistable;

public class CoreNode extends AbstractPersistable {

  private Date cmCreated;
  private String cmCreator;
  private Date cmModified;
  private String cmModifier;
  private String cmName;

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

  public String getCmName() {
    return cmName;
  }

  public void setCmName(String cmName) {
    this.cmName = cmName;
  }
}
