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

package com.gradecak.alfresco.mvc.webscript.mock;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Container;
import org.springframework.extensions.webscripts.Description;
import org.springframework.extensions.webscripts.Description.RequiredCache;
import org.springframework.extensions.webscripts.ScriptProcessorRegistry;
import org.springframework.extensions.webscripts.SearchPath;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletResponse;

import com.google.common.base.Throwables;

public class MockWebscript {

  private AbstractWebScript webScript;
  private HttpMethod method = HttpMethod.GET;
  private Map<String, String> parameters;
  private Map<String, String> body;
  private String webscriptUrl = "/service/mvc/";
  private String controllerMapping;
  private Container container = getMockedContainer();
  private Description description = getMockedDescription();
  private Cookie[] cookies;
  private Map<String, Object> headers;

  public MockWebscript(final AbstractWebScript webScript) {
    this.webScript = webScript;
  }

  public MockWebscript withGetRequest() {
    return withMethod(HttpMethod.GET);
  }

  public MockWebscript withPostRequest() {
    return withMethod(HttpMethod.POST);
  }

  public MockWebscript withMethod(final HttpMethod method) {
    this.method = method;
    return this;
  }

  public MockWebscript withParameters(final Map<String, String> parameters) {
    this.parameters = parameters;
    return this;
  }

  public MockWebscript withBody(final Map<String, String> body) {
    this.body = body;
    return this;
  }

  public MockWebscript withControllerMapping(final String controllerMapping) {
    this.controllerMapping = controllerMapping;
    return this;
  }

  public MockWebscript withUrl(final String webscriptUrl) {
    this.webscriptUrl = webscriptUrl;
    return this;
  }

  public MockWebscript withContainer(final Container container) {
    this.container = container;
    return this;
  }

  public MockWebscript withDescription(final Description description) {
    this.description = description;
    return this;
  }

  // public MockWebscript withContent(final String content) {
  // this.content = content;
  // return this;
  // }

  public MockWebscript withCookies(Cookie... cookies) {
    this.cookies = cookies;
    return this;
  }

  public MockWebscript withHeaders(Map<String, Object> headers) {
    this.headers = headers;
    return this;
  }

  public MockHttpServletResponse execute() {
    return doRequest(webScript, container, description, method.name(), parameters, body, webscriptUrl, controllerMapping, cookies, headers);
  }

  private MockHttpServletResponse doRequest(AbstractWebScript webScript, Container container, Description description, String method, Map<String, String> parameters, Map<String, String> body,
      String webscriptUrl, String controllerMapping, Cookie[] cookies, Map<String, Object> headers) {
    webScript.init(container, description);

    MockWebScriptResponse mockedResponse = MockWebScriptResponse.createMockWebScriptResponse();
    try {
      webScript.execute(MockWebscriptServletRequest.createMockWebscriptServletRequest(webScript, method, webscriptUrl, controllerMapping, parameters, body, cookies, headers), mockedResponse);
    } catch (IOException e) {
      Throwables.propagate(e);
    }

    return mockedResponse.getMockHttpServletResponse();
  }

  static private Container getMockedContainer() {
    ScriptProcessorRegistry mockedScriptProcessorRegistry = mock(ScriptProcessorRegistry.class);
    doReturn(null).when(mockedScriptProcessorRegistry).findValidScriptPath(anyString());

    Container mockedContainer = mock(Container.class);
    SearchPath mockedSearchPath = mock(SearchPath.class);
    try {
      doReturn(false).when(mockedSearchPath).hasDocument(anyString());
    } catch (IOException e) {
      Throwables.propagate(e);
    }
    doReturn(mockedSearchPath).when(mockedContainer).getSearchPath();

    return mockedContainer;
  }

  static private Description getMockedDescription() {
    Description mockedDescription = mock(Description.class);
    doReturn(mock(RequiredCache.class)).when(mockedDescription).getRequiredCache();
    return mockedDescription;
  }
}
