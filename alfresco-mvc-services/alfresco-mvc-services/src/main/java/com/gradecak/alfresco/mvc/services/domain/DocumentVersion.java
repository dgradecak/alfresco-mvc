package com.gradecak.alfresco.mvc.services.domain;

import com.gradecak.alfresco.mvc.data.domain.CmDocument;
import com.gradecak.alfresco.mvc.data.domain.CoreVersion;

public class DocumentVersion<T extends CmDocument> extends CoreVersion {
  private T document;

  public T getDocument() {
    return document;
  }

  public void setDocument(T document) {
    this.document = document;
  }
}
