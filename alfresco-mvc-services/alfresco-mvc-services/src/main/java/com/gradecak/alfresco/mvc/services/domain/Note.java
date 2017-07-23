package com.gradecak.alfresco.mvc.services.domain;

import org.springframework.hateoas.core.Relation;

import com.gradecak.alfresco.mvc.data.annotation.AlfrescoNode;
import com.gradecak.alfresco.mvc.data.domain.CoreNode;

@Relation(value = "note", collectionRelation = "notes")
@AlfrescoNode
public class Note extends CoreNode {
  private String htmlContent;
  private NoteType type = NoteType.user;

  public void setHtmlContent(String htmlContent) {
    this.htmlContent = htmlContent;
  }

  public String getHtmlContent() {
    return htmlContent;
  }

  public void setType(NoteType type) {
    this.type = type;
  }

  public NoteType getType() {
    return type;
  }

  static public enum NoteType {
    system, user
  }
}
