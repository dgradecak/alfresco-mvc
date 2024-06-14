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

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.gradecak.alfresco.mvc.rest.annotation.AlfrescoDispatcherWebscript;
import com.gradecak.alfresco.mvc.rest.annotation.EnableAlfrescoMvcRest;
import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript.ServletConfigOptions;

@Configuration
@EnableAlfrescoMvcRest(@AlfrescoDispatcherWebscript(servletContext = AlfrescoMvcServletContext.class, servletConfigOptions = ServletConfigOptions.DISABLED_PARENT_HANDLER_MAPPINGS))
public class AlfrescoMvcCustomServletConfigModuleConfiguration implements WebMvcConfigurer {

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.setUseSuffixPatternMatch(false);
	}
}
