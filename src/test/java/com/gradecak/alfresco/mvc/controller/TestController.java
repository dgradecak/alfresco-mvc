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

package com.gradecak.alfresco.mvc.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gradecak.alfresco.mvc.ResponseMapBuilder;

@Controller
@RequestMapping("/test")
public class TestController {


  @RequestMapping(value = "/get", method = { RequestMethod.GET })
  @ResponseBody
  public Map<String, Object> get(@RequestParam String id) {
    return ResponseMapBuilder.createResponseMap(id, true).build();
  }
  
  @RequestMapping(value = "/post", method = { RequestMethod.POST })
  @ResponseBody
  public Map<String, Object> post(@RequestParam String id) {
    return ResponseMapBuilder.createResponseMap(id, true).build();
  }
  
  @RequestMapping(value = "/exception", method = { RequestMethod.GET })
  @ResponseBody
  public Map<String, Object> exception(@RequestParam String id) {
    throw new RuntimeException("test exception");
  }
  
  @RequestMapping(value = "/body", method = { RequestMethod.POST })
  @ResponseBody
  public Map<String, Object> post(@RequestBody Map<String, Object> body ) {
    return ResponseMapBuilder.createResponseMap(body, true).build();
  }

}
