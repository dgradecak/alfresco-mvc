package com.gradecak.alfresco.mvc.data.domain;

import com.gradecak.alfresco.mvc.data.annotation.AlfrescoNode;
import com.gradecak.alfresco.mvc.data.mapper.CmFolderPropertiesMapper;

@AlfrescoNode(entityMapper = CmFolderPropertiesMapper.class, nodeMapper = CmFolderPropertiesMapper.class)
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
