package com.gradecak.alfresco.mvc.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript;

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
