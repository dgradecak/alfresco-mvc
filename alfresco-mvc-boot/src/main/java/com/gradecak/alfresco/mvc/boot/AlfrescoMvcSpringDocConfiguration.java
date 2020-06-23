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

package com.gradecak.alfresco.mvc.boot;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.SpringDocConfiguration;
import org.springdoc.webmvc.core.SpringDocWebMvcConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.util.StringUtils;

import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript;
import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript.DispatcherWebscriptServlet;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
@ConditionalOnClass(SpringDocWebMvcConfiguration.class)
@Import({ SpringDocWebMvcConfiguration.class, SpringDocConfigProperties.class, SpringDocConfiguration.class })
public class AlfrescoMvcSpringDocConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public GroupedOpenApi springDocGroupedOpenApi(DispatcherWebscriptServlet s) {
		return GroupedOpenApi.builder().group(getFirstUri(s.getDispatcherWebscript())).pathsToMatch("/**").build();
	}

	@Bean
	@ConditionalOnMissingBean
	public OpenAPI springDocOpenApi(DispatcherWebscriptServlet s) {
		Server server = new Server();
		server.setUrl("/alfresco/s" + getFirstUri(s.getDispatcherWebscript()));
		server.setDescription(getFirstUri(s.getDispatcherWebscript()));
		return new OpenAPI().servers(Collections.singletonList(server))
				.info(new Info().title(getShortname(s.getDispatcherWebscript()) + " - API (" + getFamily(s.getDispatcherWebscript()) + ")").description(getDescription(s.getDispatcherWebscript()))
						.version("version NOT_CONFIGURED")
						.license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
				.externalDocs(new ExternalDocumentation().description("Spring MVC for Alfresco")
						.url("https://github.com/dgradecak/alfresco-mvc"));
	}

	private String getFamily(DispatcherWebscript dw) {
		Set<String> familys = ((AbstractWebScript) dw).getDescription().getFamilys();
		if (familys == null) {
			return "";
		}
		String firstUri = familys.stream().collect(Collectors.joining(","));
		return firstUri;
	}

	private String getFirstUri(DispatcherWebscript dw) {
		String firstUri = ((AbstractWebScript) dw).getDescription().getURIs()[0];
		if (firstUri != null && firstUri.startsWith("/")) {
			return firstUri;
		}
		return "/" + firstUri;
	}

	private String getShortname(DispatcherWebscript dw) {
		String shortname = ((AbstractWebScript) dw).getDescription().getShortName();
		if (StringUtils.isEmpty(shortname)) {
			shortname = getFirstUri(dw);
		}
		return shortname;
	}

	private String getDescription(DispatcherWebscript dw) {
		String description = ((AbstractWebScript) dw).getDescription().getDescription();
		if (StringUtils.isEmpty(description)) {
			description = getShortname(dw);
		}

		return description;
	}
}
