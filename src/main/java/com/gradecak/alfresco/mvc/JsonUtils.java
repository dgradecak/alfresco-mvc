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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtils {

  static public Map<String, Object> createSuccessResponseMap() {
    return createResponseMap(null, true);
  }

  static public Map<String, Object> createFailResponseMap() {
    return createResponseMap(null, false);
  }

  static public Map<String, Object> createResponseMap(final Object obj, final boolean success) {
    Map<String, Object> response = new HashMap<String, Object>();

    if (obj != null) {
      if (obj instanceof List<?>) {
        response.put("total", ((List<?>) obj).size());
      }
      else {
        response.put("total", 1);
      }

      response.put("data", obj);
    }

    response.put("success", success);
    return response;
  }

}
