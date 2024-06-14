/*-
 * #%L
 * Alfresco MVC rest
 * %%
 * Copyright (C) 2007 - 2024 gradecak.com
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.gradecak.alfresco.mvc.test.inheritservletconfig;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript;
import com.gradecak.alfresco.mvc.webscript.mock.MockWebscript;
import com.gradecak.alfresco.mvc.webscript.mock.MockWebscriptBuilder;

@ExtendWith(SpringExtension.class)
@ContextHierarchy({ @ContextConfiguration(locations = { "/mock-alfresco-context.xml", "/test-restjsonmodule.xml" }),
		@ContextConfiguration(classes = AlfrescoMvcCustomServletConfigModuleConfiguration.class) })
@WebAppConfiguration
@TestInstance(Lifecycle.PER_CLASS)
public class CustomServletConfigTest {

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

}
