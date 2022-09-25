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

package com.gradecak.alfresco.mvc.test.inheritglobalproperties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.DispatcherServlet;

import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript;

@ExtendWith(SpringExtension.class)
@ContextHierarchy({ @ContextConfiguration(locations = { "/mock-alfresco-context.xml", "/test-restjsonmodule.xml" }),
		@ContextConfiguration(classes = AlfrescoMvcInheritGlobalPropertiesModuleConfiguration.class) })
@TestPropertySource(properties = "test.exists=true")
@WebAppConfiguration
@TestInstance(Lifecycle.PER_CLASS)
public class InheritGlobalPropertiesTest {

	@Autowired
	private Environment env;

	@Autowired
	private DispatcherWebscript dispatcherWebscript;

	@BeforeAll
	public void beforeAll() throws Exception {
	}

	@BeforeEach
	public void before() throws Exception {
	}

	@Test
	public void when_alfrescoMvcInheritGlobalProperties_expect_propertyExists() throws Exception {
		Assertions.assertNull(env.getProperty("myKey"));

		Environment servletEnvironment = dispatcherWebscript.getDispatcherServlet().getEnvironment();
		Assertions.assertEquals("myValue", servletEnvironment.getProperty("myKey"));
		
		Assertions.assertEquals("true", servletEnvironment.getProperty("test.exists"));
	}

	@Test
	public void when_alfrescoMvcInheritGlobalProperties_expect_propertyNotExists() throws Exception {
		Environment servletEnvironment = dispatcherWebscript.getDispatcherServlet().getEnvironment();
		Assertions.assertNull(servletEnvironment.getProperty("myKey1"));
		
		Assertions.assertEquals("true", servletEnvironment.getProperty("test.exists"));
	}

	@Test
	public void when_alfrescoMvcDispatcherServlet_expect_beanExists() throws Exception {
		DispatcherServlet dispatcherServlet = dispatcherWebscript.getDispatcherServlet().getWebApplicationContext()
				.getBean(DispatcherServlet.class);
		Assertions.assertNotNull(dispatcherServlet);
	}

}
