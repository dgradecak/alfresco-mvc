package com.gradecak.alfresco.mvc.data.domain;

import com.gradecak.alfresco.mvc.data.annotation.AlfrescoNode;
import com.gradecak.alfresco.mvc.data.mapper.CmDocumentPropertiesMapper;

@AlfrescoNode(entityMapper = CmDocumentPropertiesMapper.class, nodeMapper = CmDocumentPropertiesMapper.class)
public class CmDocument extends CoreNode {

  private String cmVersionLabel;
  private String cmTitle;
  private String cmDescription;

  private Long size;
  private String preview;
  private String mimetype;
  private String download;
  private String thumbnail;

  public String getCmVersionLabel() {
    return cmVersionLabel;
  }

  public void setCmVersionLabel(String cmVersionLabel) {
    this.cmVersionLabel = cmVersionLabel;
  }

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

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public String getPreview() {
    return preview;
  }

  public void setPreview(String preview) {
    this.preview = preview;
  }

  public String getMimetype() {
    return mimetype;
  }

  public void setMimetype(String mimetype) {
    this.mimetype = mimetype;
  }

  public String getDownload() {
    return download;
  }

  public void setDownload(String download) {
    this.download = download;
  }
  
  public String getThumbnail() {
    return thumbnail;
  }
  
  public void setThumbnail(String thumbnail) {
    this.thumbnail = thumbnail;
  }
}
