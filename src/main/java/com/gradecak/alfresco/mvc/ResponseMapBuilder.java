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

package com.gradecak.alfresco.mvc;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class ResponseMapBuilder {

  private final Builder<String, Object> variables = ImmutableMap.builder();

  public ResponseMapBuilder withData(final Object data) {

    int size = 1;
    if (data != null) {
      if (data instanceof List<?>) {
        size = ((List<?>) data).size();
      }
    }

    return withEntry("data", data).withEntry("total", size);
  }

  public ResponseMapBuilder withSuccess(final boolean success) {
    return withEntry("success", success);
  }

  public ResponseMapBuilder withEntry(final String key, final Object value) {
    if (value != null) {
      variables.put(key, value);
    }
    return this;
  }

  public Map<String, Object> build() {
    return variables.build();
  }

  static public ResponseMapBuilder createSuccessResponseMap() {
    return new ResponseMapBuilder().withSuccess(true);
  }

  static public ResponseMapBuilder createFailResponseMap() {
    return new ResponseMapBuilder().withSuccess(false);
  }

  static public ResponseMapBuilder createResponseMap(final Object data, final boolean success) {
    return new ResponseMapBuilder().withData(data).withSuccess(success);
  }
}
