package com.gradecak.alfresco.querytemplate.domain;

import java.util.Date;

import com.gradecak.alfresco.querytemplate.AbstractPersistable;


public class CmFolder extends AbstractPersistable{

  private String cmTitle;
  
  private String cmDescription;
  private String cmName;
  private Date cmModified;
  private String cmModifier;
  private Date cmCreated;
  private String cmCreator;
  private String appIcon;
  private String sysLocale;

  public String getCmTitle() {
    return cmTitle;
  }

  public void setCmTitle(String cmTitle) {
    this.cmTitle = cmTitle;
  }

  public String getCmDescription() {
    return cmDescription;
  }

  public void setCmDescription(String cmDescription) {
    this.cmDescription = cmDescription;
  }

  public String getCmName() {
    return cmName;
  }

  public void setCmName(String cmName) {
    this.cmName = cmName;
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

  public String getAppIcon() {
    return appIcon;
  }

  public void setAppIcon(String appIcon) {
    this.appIcon = appIcon;
  }

  public String getSysLocale() {
    return sysLocale;
  }

  public void setSysLocale(String sysLocale) {
    this.sysLocale = sysLocale;
  }
}