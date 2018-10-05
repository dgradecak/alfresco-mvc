package com.gradecak.alfresco.querytemplate;

import java.util.Date;

public class CmFolder {

  private String cmTitle;
  private String cmDescription;
  private Date customNamespaceData;
  private String ref;

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

  public String getRef() {
    return ref;
  }

  public void setRef(String ref) {
    this.ref = ref;
  }

  public Date getCustomNamespaceData() {
    return customNamespaceData;
  }

  public void setCustomNamespaceData(Date customNamespaceData) {
    this.customNamespaceData = customNamespaceData;
  }
}