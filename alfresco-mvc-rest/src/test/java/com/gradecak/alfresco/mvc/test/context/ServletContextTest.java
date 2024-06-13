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

package com.gradecak.alfresco.mvc.test.context;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.DispatcherServlet;

import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript;
import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript.DispatcherWebscriptServlet;
import com.gradecak.alfresco.mvc.webscript.mock.MockWebscript;
import com.gradecak.alfresco.mvc.webscript.mock.MockWebscriptBuilder;

@ExtendWith(SpringExtension.class)
@ContextHierarchy({ @ContextConfiguration(locations = { "/mock-alfresco-context.xml", "/test-restjsonmodule.xml" }),
		@ContextConfiguration(classes = AlfrescoMvcCustomServletConfigModuleConfiguration.class) })
@WebAppConfiguration
@TestInstance(Lifecycle.PER_CLASS)
public class ServletContextTest {

	@Autowired @Qualifier("webscript.alfresco-mvc.mvc.get")
	private DispatcherWebscript dispatcherWebscript;
	
	@Autowired
	private DispatcherWebscriptServlet dispatcherWebscriptServlet;

	@Autowired
	ApplicationContext applicationContext;

	MockWebscript mockWebscript;

	@BeforeAll
	public void beforeAll() throws Exception {
		mockWebscript = MockWebscriptBuilder.singleWebscript(dispatcherWebscript);
	}

	@BeforeEach
	public void before() throws Exception {
		mockWebscript.newRequest();
	}
	
	@Test
	public void when_alfrescoMvcDispatcherServletContextConfigured_expect_applicationContextCorrectAndDispatcherServletConfigured() {
		Map<String, DispatcherWebscript> beansOfType = applicationContext.getBeansOfType(DispatcherWebscript.class);		
		Assertions.assertEquals(8, beansOfType.size());
		
		DispatcherServlet dispatcherServletSame = dispatcherWebscript.getDispatcherServlet().getWebApplicationContext()
				.getBean(DispatcherServlet.class);

		Assertions.assertEquals(dispatcherWebscriptServlet, dispatcherServletSame);
	}
}
