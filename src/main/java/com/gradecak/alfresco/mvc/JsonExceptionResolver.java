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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
import org.springframework.web.util.JavaScriptUtils;

public class JsonExceptionResolver implements HandlerExceptionResolver {

  protected static final Logger LOGGER = LoggerFactory.getLogger(JsonExceptionResolver.class);

  public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

    LOGGER.error("Error occurred for handler [" + handler + "]", ex);

    View view = new MappingJacksonJsonView();
    Map<String, Object> res = new HashMap<String, Object>();
    res.put("success", false);

    Map<String, Object> protocol = new HashMap<String, Object>();
    protocol.put("event", "exception");

    Map<String, Object> data = new HashMap<String, Object>();
    data.put("exception", ex.getClass());
    data.put("message", JavaScriptUtils.javaScriptEscape(ex.getMessage()));
    protocol.put("data", data);

    res.put("protocol", protocol);

    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

    return new ModelAndView(view, res);
  }

}
