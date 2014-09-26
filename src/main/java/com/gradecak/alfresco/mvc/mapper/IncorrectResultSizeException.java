package com.gradecak.alfresco.mvc.mapper;

import org.alfresco.error.AlfrescoRuntimeException;

public class IncorrectResultSizeException extends AlfrescoRuntimeException {

  private static final long serialVersionUID = 420411279170854347L;

  public static final String DEFAULT_MESSAGE_ID = "mvc.exception.inccorectResultSetSize";

  public IncorrectResultSizeException() {
    super(DEFAULT_MESSAGE_ID, new Object[] { "more." });
  }

  public IncorrectResultSizeException(int actualFound) {
    super(DEFAULT_MESSAGE_ID, new Object[] { actualFound });
  }

  public IncorrectResultSizeException(String msgId, Throwable cause) {
    super(msgId, cause);
  }

  public IncorrectResultSizeException(String msgId, Object[] msgParams, Throwable cause) {
    super(msgId, msgParams, cause);
  }

  public IncorrectResultSizeException(String msgId, Object[] msgParams) {
    super(msgId, msgParams);
  }

  public IncorrectResultSizeException(String msgId) {
    super(msgId);
  }
}
