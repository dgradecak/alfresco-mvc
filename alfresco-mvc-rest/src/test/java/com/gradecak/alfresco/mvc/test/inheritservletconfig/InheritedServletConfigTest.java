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

package com.gradecak.alfresco.mvc.test.inheritservletconfig;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript;
import com.gradecak.alfresco.mvc.webscript.mock.MockWebscript;
import com.gradecak.alfresco.mvc.webscript.mock.MockWebscriptBuilder;

@ExtendWith(SpringExtension.class)
@ContextHierarchy({ @ContextConfiguration(locations = { "/mock-alfresco-context.xml", "/test-restjsonmodule.xml" }),
		@ContextConfiguration(classes = AlfrescoMvcServletConfigModuleConfiguration.class) })
@WebAppConfiguration
@TestInstance(Lifecycle.PER_CLASS)
public class InheritedServletConfigTest {

	@Autowired
	private DispatcherWebscript dispatcherWebscript;

	MockWebscript mockWebscript;

	@BeforeAll
	public void beforeAll() throws Exception {
		mockWebscript = MockWebscriptBuilder.singleWebscript(dispatcherWebscript);
	}

	@BeforeEach
	public void before() throws Exception {
		mockWebscript.newRequest();
	}

	/**
	 * @deprecated as of Spring 5.2.4. See class-level note in
	 *             {@link RequestMappingHandlerMapping} on the deprecation of path
	 *             extension config options. As there is no replacement for this
	 *             method, in Spring 5.2.x it is necessary to set it to
	 *             {@code false}. In Spring 5.3 the default changes to {@code false}
	 *             and use of this property becomes unnecessary.
	 */
	@Deprecated
	@Test
	public void when_alfrescoMvcDispatcherServletConfigOptionsWithSuffix_expect_suffixHandledAndOk() throws Exception {
		DispatcherServlet dispatcherServlet = dispatcherWebscript.getDispatcherServlet().getWebApplicationContext()
				.getBean(DispatcherServlet.class);
		Assertions.assertNotNull(dispatcherServlet);

		MockHttpServletResponse res = mockWebscript.withControllerMapping("/test/withsufix.test").execute();
		Assertions.assertEquals(HttpStatus.OK.value(), res.getStatus());

		String contentAsString = res.getContentAsString();
		Assertions.assertEquals("withsufix", contentAsString);
	}

	@Test
	public void when_alfrescoMvcDispatcherServletConfigOptionsWithoutSuffix_expect_ok() throws Exception {
		DispatcherServlet dispatcherServlet = dispatcherWebscript.getDispatcherServlet().getWebApplicationContext()
				.getBean(DispatcherServlet.class);
		Assertions.assertNotNull(dispatcherServlet);

		MockHttpServletResponse res = mockWebscript.withControllerMapping("/test/withoutsufix").execute();
		Assertions.assertEquals(HttpStatus.OK.value(), res.getStatus());

		String contentAsString = res.getContentAsString();
		Assertions.assertEquals("withoutsufix", contentAsString);
	}
}
