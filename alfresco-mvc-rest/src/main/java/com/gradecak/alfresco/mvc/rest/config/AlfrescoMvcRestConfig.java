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

package com.gradecak.alfresco.mvc.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript;

/**
 * will be removed in 8.0
 * 
 * @deprecated use
 *             {@code AlfrescoDispatcherWebscript or instantiate DispatcherWebscript}
 */
@Configuration
public abstract class AlfrescoMvcRestConfig {

	@Bean(name = { "webscript.alfresco-mvc.mvc.post", "webscript.alfresco-mvc.mvc.get",
			"webscript.alfresco-mvc.mvc.delete", "webscript.alfresco-mvc.mvc.put" })
	public DispatcherWebscript dispatcherWebscript() {
		DispatcherWebscript dispatcherWebscript = new DispatcherWebscript();
		dispatcherWebscript
				.setContextClass(org.springframework.web.context.support.AnnotationConfigWebApplicationContext.class);
		dispatcherWebscript.setContextConfigLocation(servletContext().getName());
		return dispatcherWebscript;
	}

	abstract protected Class<?> servletContext();
}
