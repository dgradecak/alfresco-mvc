package com.gradecak.alfresco.mvc.config;

import org.springframework.context.annotation.Configuration;

import com.gradecak.alfresco.mvc.rest.annotation.AlfrescoDispatcherWebscript;

@Configuration
@AlfrescoDispatcherWebscript(servletContext = AlfrescoMvcRestServletContext.class)
@AlfrescoDispatcherWebscript(servletContext = AlfrescoMvcRestServletContext.class)
public class AlfrescoMvcRestModuleConfigurationWithRepeatable {
}
