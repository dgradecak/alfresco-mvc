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

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.gradecak.alfresco.mvc.rest.ResponseMapBuilder;

@Controller
@RequestMapping("/test")
public class TestController {

	@RequestMapping(value = "/get", method = { RequestMethod.GET })
	public ResponseEntity<?> get(@RequestParam String id) {
		return new ResponseEntity<>(ResponseMapBuilder.createResponseMap(id, true).build(), HttpStatus.OK);
	}

	@RequestMapping(value = "/getHeaders", method = { RequestMethod.GET })
	public ResponseEntity<?> getHeaders(@RequestHeader() Map<String, String> headers) {
		return new ResponseEntity<>(ResponseMapBuilder.createResponseMap(headers, true).build(), HttpStatus.OK);
	}

	@RequestMapping(value = "/getCookies", method = { RequestMethod.GET })
	public ResponseEntity<?> getCookies(HttpServletRequest req) {
		return new ResponseEntity<>(ResponseMapBuilder.createResponseMap(req.getCookies(), true).build(),
				HttpStatus.OK);
	}

	@RequestMapping(value = "/post", method = { RequestMethod.POST })
	public ResponseEntity<?> post(@RequestParam String id) {
		return new ResponseEntity<>(ResponseMapBuilder.createResponseMap(id, true).build(), HttpStatus.OK);
	}

	@RequestMapping(value = "/post2", method = { RequestMethod.POST })
	public ResponseEntity<?> post() {
		return new ResponseEntity<>(ResponseMapBuilder.createSuccessResponseMap().build(), HttpStatus.OK);
	}

	@RequestMapping(value = "/exception", method = { RequestMethod.GET })
	public ResponseEntity<?> exception(@RequestParam String id) {
		throw new RuntimeException("test exception");
	}

	@RequestMapping(value = "/body", method = { RequestMethod.POST })
	public ResponseEntity<?> post(@RequestBody Map<String, Object> body) {
		return new ResponseEntity<>(ResponseMapBuilder.createResponseMap(body, true).build(), HttpStatus.OK);
	}

	@RequestMapping(value = "/delete", method = { RequestMethod.DELETE, RequestMethod.PUT })
	public ResponseEntity<?> delete() {
		return new ResponseEntity<>(ResponseMapBuilder.createSuccessResponseMap().build(), HttpStatus.OK);
	}
}
