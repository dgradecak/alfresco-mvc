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

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.gradecak.alfresco.mvc.rest.annotation.AlfrescoRestResponse;

@Controller
@RequestMapping("/test")
public class TestController {

	@RequestMapping(value = "/get", method = { RequestMethod.GET })
	public ResponseEntity<?> get(@RequestParam String id) {
		return ResponseEntity.ok(id);
	}

	@RequestMapping(value = "/headers", method = { RequestMethod.GET })
	public ResponseEntity<?> headers(@RequestHeader MultiValueMap<String, String> headers) {
		return ResponseEntity.ok().headers(new HttpHeaders(headers)).body("success");
	}

	@RequestMapping(value = "/cookies", method = { RequestMethod.GET })
	public ResponseEntity<?> cookies(HttpServletRequest req) {

		HttpHeaders headers = new HttpHeaders();
		Cookie[] cookies = req.getCookies();
		for (Cookie cookie : cookies) {
			headers.add(cookie.getName(), cookie.getValue());
		}

		return ResponseEntity.ok().headers(new HttpHeaders(headers)).body("success");

	}

	@RequestMapping(value = "/post", method = { RequestMethod.POST })
	public ResponseEntity<?> post(@RequestParam String id) {
		return ResponseEntity.ok(id);
	}

	@RequestMapping(value = "/delete", method = { RequestMethod.DELETE })
	public ResponseEntity<?> delete(@RequestParam String id) {
		return ResponseEntity.ok(id);
	}

	@RequestMapping(value = "/exception", method = { RequestMethod.GET })
	public ResponseEntity<?> exception(@RequestParam String id) {
		throw new RuntimeException("test exception");
	}

	@RequestMapping(value = "/body", method = { RequestMethod.POST })
	public ResponseEntity<?> post(@RequestBody Map<String, String> body) {
		return ResponseEntity.ok().header("id", body.get("id")).body("success");
	}

	@RequestMapping(value = "/ambigousMethod", method = { RequestMethod.DELETE, RequestMethod.PUT })
	public ResponseEntity<?> ambigousMethod() {
		return ResponseEntity.ok().build();
	}

	@RequestMapping(value = "/ambigousMethod", method = { RequestMethod.DELETE })
	public ResponseEntity<?> ambigousMethod2() {
		return ResponseEntity.ok().build();
	}

	@RequestMapping(value = "/put", method = { RequestMethod.PUT })
	public ResponseEntity<?> put() {
		return ResponseEntity.ok().build();
	}

	@RequestMapping(value = "/download", method = { RequestMethod.GET })
	public ResponseEntity<?> download() throws IOException {
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"mvc.delete.desc.xml\"")
				.body(new ClassPathResource(
						"alfresco/extension/templates/webscripts/alfresco-mvc/mvc.delete.desc.xml"));
	}

	@GetMapping(value = "noderef")
	public ResponseEntity<?> noderef() throws IOException {
		return ResponseEntity.ok(new NodeRef("a://a/a"));
	}

	@GetMapping(value = "noderefAlfrescoResponse")
	@AlfrescoRestResponse
	public ResponseEntity<?> noderefAlfrescoResponse() throws IOException {
		return ResponseEntity.ok(new NodeRef("a://a/a"));
	}

	@RequestMapping(value = "/exceptionHandler", method = { RequestMethod.GET })
	public ResponseEntity<?> exceptionHandler() {
		throw new IllegalArgumentException("test exception");
	}

	@GetMapping(value = "regexp/{regexpchars:.+}")
	public ResponseEntity<?> regexpchars(@PathVariable String regexpchars) throws IOException {
		return ResponseEntity.ok(regexpchars);
	}

	@ExceptionHandler({ IllegalArgumentException.class })
	public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException exc) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("error", "internal server error").build();
	}
}
