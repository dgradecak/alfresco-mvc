/**
 * Copyright gradecak.com

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
