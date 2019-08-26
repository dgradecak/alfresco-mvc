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
import java.nio.charset.Charset;

import javax.servlet.http.Cookie;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.common.collect.ImmutableMap;
import com.gradecak.alfresco.mvc.webscript.mock.MockWebscript;
import com.gradecak.alfresco.mvc.webscript.mock.MockWebscriptBuilder;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = { "/web-annotationcontext-test.xml" })
@TestInstance(Lifecycle.PER_CLASS)
public class DispatcherWebscriptSpringAnnotationTest {

	@Autowired
	private DispatcherWebscript webScript;
	
	private MockWebscript mockWebscript;

	@BeforeAll
	public void beforeAll() throws Exception {
		MockitoAnnotations.initMocks(this);

		mockWebscript = MockWebscriptBuilder.singleWebscript(webScript);
	}
	
	@BeforeEach
	public void before() throws Exception {
		mockWebscript.newRequest();
	}

	@Test
	public void requestGet_responseOk() throws Exception {
		MockHttpServletResponse res = mockWebscript.withParameters(ImmutableMap.of("id", "testId"))
				.withControllerMapping("test/get").execute();
		Assertions.assertTrue(res.getStatus() == 200);
		Assertions.assertEquals("{\"data\":\"testId\",\"total\":1,\"success\":true}", res.getContentAsString());
	}

	@Test
	public void requestGet_withHeaders_responseOk() throws Exception {
		MockHttpServletResponse res = mockWebscript.withHeaders(ImmutableMap.of("header-key", (Object) "header-value"))
				.withControllerMapping("test/getHeaders").execute();
		Assertions.assertTrue(res.getStatus() == 200);
		Assertions.assertEquals(
				"{\"data\":{\"Content-Type\":\"application/json\",\"header-key\":\"header-value\"},\"total\":1,\"success\":true}",
				res.getContentAsString());
	}

	@Test
	public void requestGet_withCookies_responseOk() throws Exception {
		MockHttpServletResponse res = mockWebscript.withCookies(new Cookie("cookie-key", "cookie-value"))
				.withControllerMapping("test/getCookies").execute();
		Assertions.assertTrue(res.getStatus() == 200);
		Assertions.assertEquals(
				"{\"data\":[{\"name\":\"cookie-key\",\"value\":\"cookie-value\",\"maxAge\":-1,\"secure\":false,\"version\":0,\"httpOnly\":false}],\"total\":1,\"success\":true}",
				res.getContentAsString());
		
//		BEFORE: nulls were serialized Assertions.assertEquals(
//				"{\"data\":[{\"name\":\"cookie-key\",\"value\":\"cookie-value\",\"comment\":null,\"domain\":null,\"maxAge\":-1,\"path\":null,\"secure\":false,\"version\":0,\"httpOnly\":false}],\"total\":1,\"success\":true}",
//				res.getContentAsString());
	}

	@Test
	public void requestPost_withParams_responseOk() throws Exception {
		MockHttpServletResponse res = mockWebscript.withPostRequest().withParameters(ImmutableMap.of("id", "testId"))
				.withControllerMapping("test/post").execute();
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
		MockHttpServletResponse res = mockWebscript.withPostRequest().withBody(ImmutableMap.of("id", "testId"))
				.withControllerMapping("test/body").execute();
		Assertions.assertTrue(res.getStatus() == 200);
		Assertions.assertEquals("{\"data\":{\"id\":\"testId\"},\"total\":1,\"success\":true}",
				res.getContentAsString());
	}

	@Test
	public void requestGet_wrongControllerMapping_response404() throws Exception {
		MockHttpServletResponse res = mockWebscript.withParameters(ImmutableMap.of("id", "testId"))
				.withControllerMapping("test/wrong").execute();
		Assertions.assertTrue(res.getStatus() == 404);
	}

	@Test
	public void requestPost_toGetMethod() throws Exception {
		MockHttpServletResponse res = mockWebscript.withPostRequest().withParameters(ImmutableMap.of("id", "testId"))
				.withControllerMapping("test/get").execute();
		Assertions.assertTrue(res.getStatus() == 405);
		Assertions.assertEquals("Request method 'POST' not supported", res.getErrorMessage());
	}

	@Test
	public void requestDelete_responseOk() throws Exception {
		MockHttpServletResponse res = mockWebscript.withPostRequest().withMethod(HttpMethod.DELETE)
				.withControllerMapping("test/delete").execute();
		Assertions.assertTrue(res.getStatus() == 200);
		Assertions.assertEquals("{\"success\":true}", res.getContentAsString());
	}

	@Test
	public void requestPut_responseOk() throws Exception {
		MockHttpServletResponse res = mockWebscript.withPostRequest().withMethod(HttpMethod.PUT)
				.withControllerMapping("test/delete").execute();
		Assertions.assertTrue(res.getStatus() == 200);
		Assertions.assertEquals("{\"success\":true}", res.getContentAsString());
	}

	@Test
	public void requestHead_response405() throws Exception {
		MockHttpServletResponse res = mockWebscript.withPostRequest().withMethod(HttpMethod.HEAD)
				.withControllerMapping("test/delete").execute();
		Assertions.assertTrue(res.getStatus() == 405);
	}
	
	@Test
	public void get_fileDownloaded_status200() throws Exception {
		MockHttpServletResponse res = mockWebscript.withControllerMapping("test/download").execute();
		Assertions.assertTrue(res.getStatus() == 200);
		
		String contentAsString = res.getContentAsString();		
		ClassPathResource resource = new ClassPathResource("alfresco/extension/templates/webscripts/alfresco-mvc/mvc.delete.desc.xml");
		Assertions.assertEquals(IOUtils.toString(resource.getInputStream(), Charset.defaultCharset()), contentAsString); 
	}
	
	@Test
	public void get_noderefAlfrescoResponse_status200() throws Exception {
		MockHttpServletResponse res = mockWebscript.withControllerMapping("test/noderefAlfrescoResponse").execute();
		Assertions.assertTrue(res.getStatus() == 200);
		
		String contentAsString = res.getContentAsString();

		// TODO: since a mock is being used for webscriptHelper and if the response i correctly processed by AlfrescoApiResponseInterceptor
		// there will always be an empty string unless we do something more to handle this (so it is expected as is)
		Assertions.assertEquals("", contentAsString); 
	}

	@Test
	public void requestGet_responseException() throws Exception {
		Assertions.assertThrows(IOException.class, () -> {
			mockWebscript.withParameters(ImmutableMap.of("id", "testId")).withControllerMapping("test/exception")
					.execute();
		});
	}

	// TODO add file upload test
}
