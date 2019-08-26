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
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import com.google.common.collect.ImmutableMap;
import com.gradecak.alfresco.mvc.webscript.mock.MockWebscript;

public abstract class AbstractAlfrescoMvcTest {

	MockWebscript mockWebscript;

	@Test
	public void when_getWithRequiredParamId_expect_okWithIdInBody() throws Exception {
		MockHttpServletResponse res = mockWebscript.withParameters(ImmutableMap.of("id", "testId"))
				.withControllerMapping("test/get").execute();
		Assertions.assertEquals(HttpStatus.OK.value(), res.getStatus());
		Assertions.assertEquals("testId", res.getContentAsString());
	}

	@Test
	public void when_getWithoutRequiredParamId_expect_failBadRequest() throws Exception {
		MockHttpServletResponse res = mockWebscript.withControllerMapping("test/get").execute();
		Assertions.assertEquals(res.getStatus(), HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void when_getWithRequiredHeader_expect_okAndHeaderInBodyAndBodyIsSuccess() throws Exception {
		MockHttpServletResponse res = mockWebscript.withHeaders(ImmutableMap.of("header-key", "header-value"))
				.withControllerMapping("test/headers").execute();
		Assertions.assertEquals(HttpStatus.OK.value(), res.getStatus());
		Assertions.assertEquals("header-value", res.getHeaderValue("header-key"));
		Assertions.assertEquals("success", res.getContentAsString());
	}

	@Test
	public void when_getWithCookies_expect_okAndCookieKeyValueInBodyAndBodyIsSuccess() throws Exception {
		MockHttpServletResponse res = mockWebscript.withCookies(new Cookie("cookie-key", "cookie-value"))
				.withControllerMapping("test/cookies").execute();
		Assertions.assertEquals(HttpStatus.OK.value(), res.getStatus());
		Assertions.assertEquals("cookie-value", res.getHeaderValue("cookie-key"));
		Assertions.assertEquals("success", res.getContentAsString());
	}

	@Test
	public void when_postWithRequiredParamId_expect_okAndParamValueInBody() throws Exception {
		MockHttpServletResponse res = mockWebscript.withPostRequest().withParameters(ImmutableMap.of("id", "testId"))
				.withControllerMapping("test/post").execute();
		Assertions.assertEquals(HttpStatus.OK.value(), res.getStatus());
		Assertions.assertEquals("testId", res.getContentAsString());
	}

	@Test
	public void when_deleteWithRequiredParamId_expect_okAndParamValueInBody() throws Exception {
		MockHttpServletResponse res = mockWebscript.withDeleteRequest().withParameters(ImmutableMap.of("id", "testId"))
				.withControllerMapping("test/delete").execute();
		Assertions.assertEquals(HttpStatus.OK.value(), res.getStatus());
		Assertions.assertEquals("testId", res.getContentAsString());
	}

	@Test
	public void when_wrongHttpMethodWithoutRequiredParam_expect_failMethodNotAllowed() throws Exception {
		MockHttpServletResponse res = mockWebscript.withPostRequest().withControllerMapping("test/delete").execute();
		Assertions.assertEquals(res.getStatus(), HttpStatus.METHOD_NOT_ALLOWED.value());
	}

	@Test
	public void when_postWithoutRequiredBody_expect_failBadRequest() throws Exception {
		MockHttpServletResponse res = mockWebscript.withPostRequest().withControllerMapping("test/body").execute();
		Assertions.assertTrue(res.getStatus() == HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void when_postWithRequiredBody_expect_okAndBodyInHeaderAndBodyIsSuccess() throws Exception {
		MockHttpServletResponse res = mockWebscript.withPostRequest().withBody(ImmutableMap.of("id", "testId"))
				.withControllerMapping("test/body").execute();
		Assertions.assertEquals(HttpStatus.OK.value(), res.getStatus());
		Assertions.assertEquals("testId", res.getHeaderValue("id"));
		Assertions.assertEquals("success", res.getContentAsString());
	}

	@Test
	public void when_wrongControllerMapping_expect_notFound() throws Exception {
		MockHttpServletResponse res = mockWebscript.withParameters(ImmutableMap.of("id", "testId"))
				.withControllerMapping("test/nonexistingURI").execute();
		Assertions.assertEquals(res.getStatus(), HttpStatus.NOT_FOUND.value());
	}

	@Test
	public void when_wrongHttpMethodWithRequiredParam_failMethodNotAllowed() throws Exception {
		MockHttpServletResponse res = mockWebscript.withPostRequest().withParameters(ImmutableMap.of("id", "testId"))
				.withControllerMapping("test/get").execute();
		Assertions.assertEquals(res.getStatus(), HttpStatus.METHOD_NOT_ALLOWED.value());
		Assertions.assertEquals("Request method 'POST' not supported", res.getErrorMessage());
	}

	@Test
	public void when_ambigousMethodInvoked_expect_handledIOException() throws Exception {
		Assertions.assertThrows(IOException.class, () -> {
			mockWebscript.withPostRequest().withMethod(HttpMethod.DELETE).withControllerMapping("test/ambigousMethod")
					.execute();
		});
	}

	@Test
	public void when_putMethodInvoked_expect_ok() throws Exception {
		MockHttpServletResponse res = mockWebscript.withPostRequest().withMethod(HttpMethod.PUT)
				.withControllerMapping("test/put").execute();
		Assertions.assertEquals(HttpStatus.OK.value(), res.getStatus());
	}

	@Test
	public void when_downloadInitatedCorrectly_expect_okAndFileInBody() throws Exception {
		MockHttpServletResponse res = mockWebscript.withControllerMapping("test/download").execute();
		Assertions.assertEquals(HttpStatus.OK.value(), res.getStatus());

		String contentAsString = res.getContentAsString();
		ClassPathResource resource = new ClassPathResource(
				"alfresco/extension/templates/webscripts/alfresco-mvc/mvc.delete.desc.xml");
		Assertions.assertEquals(IOUtils.toString(resource.getInputStream(), Charset.defaultCharset()), contentAsString);
	}

	@Test
	public void when_alfrescoMvcSerializationIsUsed_expect_okAndNodrefFullySerialized() throws Exception {
		MockHttpServletResponse res = mockWebscript.withControllerMapping("test/noderef").execute();
		Assertions.assertEquals(HttpStatus.OK.value(), res.getStatus());

		String contentAsString = res.getContentAsString();
		Assertions.assertEquals("{\"storeRef\":{\"protocol\":\"a\",\"identifier\":\"a\"},\"id\":\"a\"}",
				contentAsString);
	}

	@Test
	public void when_alfrescoRestSerializationIsUsedButMocked_expect_okAndNodrefNotSerialized() throws Exception {
		MockHttpServletResponse res = mockWebscript.withControllerMapping("test/noderefAlfrescoResponse").execute();
		Assertions.assertEquals(HttpStatus.OK.value(), res.getStatus());

		String contentAsString = res.getContentAsString();

		// TODO: since a mock is being used for webscriptHelper and if the response i
		// correctly processed by AlfrescoApiResponseInterceptor
		// there will always be an empty string unless we do something more to handle
		// this (so it is expected as is)
		Assertions.assertEquals("", contentAsString);
	}

	@Test
	public void when_expectedExceptionIsThrownByController_expect_handledIOException() throws Exception {
		Assertions.assertThrows(IOException.class, () -> {
			mockWebscript.withParameters(ImmutableMap.of("id", "testId")).withControllerMapping("test/exception")
					.execute();
		});
	}

	@Test
	public void when_handleIllegalArgumentException_expect_failInternalServerError() throws Exception {
		MockHttpServletResponse res = mockWebscript.withControllerMapping("test/exceptionHandler").execute();
		Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), res.getStatus());
	}

	// TODO add file upload test
}
