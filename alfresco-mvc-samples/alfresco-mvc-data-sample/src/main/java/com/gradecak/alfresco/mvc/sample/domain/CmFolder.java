package com.gradecak.alfresco.mvc.sample.domain;

import com.gradecak.alfresco.mvc.data.annotation.AlfrescoNode;

@AlfrescoNode
public class CmFolder extends CoreNode {

  private String cmTitle;
  private String cmDescription;

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
}
