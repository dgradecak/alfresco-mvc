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

package com.gradecak.alfresco.mvc.webscript;

import java.io.IOException;

import javax.servlet.http.Cookie;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import com.google.common.collect.ImmutableMap;
import com.gradecak.alfresco.mvc.webscript.mock.MockWebscript;
import com.gradecak.alfresco.mvc.webscript.mock.MockWebscriptBuilder;

public class DispatcherWebscriptMockitoRunnerTest {

  private @Spy DispatcherWebscript webScript;

  private MockWebscript mockWebscript;

  @BeforeEach
  public void before() throws Exception {
    MockitoAnnotations.initMocks(this);

    webScript.setServletContext(new MockServletContext());
    webScript.setContextConfigLocation("test-webscriptdispatcher-context.xml");

    ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
    applicationContext.setConfigLocation("web-context-test.xml");
    applicationContext.refresh();
    webScript.setApplicationContext(applicationContext);
    webScript.onApplicationEvent(new ContextRefreshedEvent(applicationContext));

    mockWebscript = MockWebscriptBuilder.singleWebscript(webScript);
  }

  @Test
  public void requestGet_responseOk() throws Exception {
    MockHttpServletResponse res = mockWebscript.withParameters(ImmutableMap.of("id", "testId")).withControllerMapping("test/get").execute();
    Assertions.assertTrue(res.getStatus() == 200);
    Assertions.assertEquals("{\"data\":\"testId\",\"total\":1,\"success\":true}", res.getContentAsString());
  }

  @Test
  public void requestGet_withHeaders_responseOk() throws Exception {
    MockHttpServletResponse res = mockWebscript.withHeaders(ImmutableMap.of("header-key", (Object) "header-value")).withControllerMapping("test/getHeaders").execute();
    Assertions.assertTrue(res.getStatus() == 200);
    Assertions.assertEquals("{\"data\":{\"Content-Type\":\"application/json\",\"header-key\":\"header-value\"},\"total\":1,\"success\":true}", res.getContentAsString());
  }

  @Test
  public void requestGet_withCookies_responseOk() throws Exception {
    MockHttpServletResponse res = mockWebscript.withCookies(new Cookie("cookie-key", "cookie-value")).withControllerMapping("test/getCookies").execute();
    Assertions.assertTrue(res.getStatus() == 200);
    Assertions.assertEquals(
        "{\"data\":[{\"name\":\"cookie-key\",\"value\":\"cookie-value\",\"comment\":null,\"domain\":null,\"maxAge\":-1,\"path\":null,\"secure\":false,\"version\":0,\"httpOnly\":false}],\"total\":1,\"success\":true}",
        res.getContentAsString());
  }

  @Test
  public void requestPost_withParams_responseOk() throws Exception {
    MockHttpServletResponse res = mockWebscript.withPostRequest().withParameters(ImmutableMap.of("id", "testId")).withControllerMapping("test/post").execute();
    Assertions.assertTrue(res.getStatus() == 200);
    Assertions.assertEquals("{\"data\":\"testId\",\"total\":1,\"success\":true}", res.getContentAsString());
  }

  @Test
  public void requestPost_responseOk() throws Exception {
    MockHttpServletResponse res = mockWebscript.withPostRequest().withControllerMapping("test/post2").execute();
    Assertions.assertTrue(res.getStatus() == 200);
    Assertions.assertEquals("{\"success\":true}", res.getContentAsString());
  }

  @Test
  public void requestPost_withBody_response400() throws Exception {
    MockHttpServletResponse res = mockWebscript.withPostRequest().withControllerMapping("test/body").execute();
    Assertions.assertTrue(res.getStatus() == 400);
  }

  @Test
  public void requestPost_withBody_responseOk() throws Exception {
    MockHttpServletResponse res = mockWebscript.withPostRequest().withBody(ImmutableMap.of("id", "testId")).withControllerMapping("test/body").execute();
    Assertions.assertTrue(res.getStatus() == 200);
    Assertions.assertEquals("{\"data\":{\"id\":\"testId\"},\"total\":1,\"success\":true}", res.getContentAsString());
  }

  @Test
  public void requestGet_wrongControllerMapping_response404() throws Exception {
    MockHttpServletResponse res = mockWebscript.withParameters(ImmutableMap.of("id", "testId")).withControllerMapping("test/wrong").execute();
    Assertions.assertTrue(res.getStatus() == 404);
  }

  @Test
  public void requestPost_toGetMethod() throws Exception {
    MockHttpServletResponse res = mockWebscript.withPostRequest().withParameters(ImmutableMap.of("id", "testId")).withControllerMapping("test/get").execute();
    Assertions.assertTrue(res.getStatus() == 405);
    Assertions.assertEquals("Request method 'POST' not supported", res.getErrorMessage());
  }

  @Test
  public void requestDelete_responseOk() throws Exception {
    MockHttpServletResponse res = mockWebscript.withPostRequest().withMethod(HttpMethod.DELETE).withControllerMapping("test/delete").execute();
    Assertions.assertTrue(res.getStatus() == 200);
    Assertions.assertEquals("{\"success\":true}", res.getContentAsString());
  }

  @Test
  public void requestPut_responseOk() throws Exception {
    MockHttpServletResponse res = mockWebscript.withPostRequest().withMethod(HttpMethod.PUT).withControllerMapping("test/delete").execute();
    Assertions.assertTrue(res.getStatus() == 200);
    Assertions.assertEquals("{\"success\":true}", res.getContentAsString());
  }

  @Test
  public void requestHead_response405() throws Exception {
    MockHttpServletResponse res = mockWebscript.withPostRequest().withMethod(HttpMethod.HEAD).withControllerMapping("test/delete").execute();
    Assertions.assertTrue(res.getStatus() == 405);
  }

  @Test
  public void requestGet_responseException() throws Exception {
    Assertions.assertThrows(IOException.class, () -> {
      mockWebscript.withParameters(ImmutableMap.of("id", "testId")).withControllerMapping("test/exception").execute();
    });
  }

  // TODO add file upload test
}
