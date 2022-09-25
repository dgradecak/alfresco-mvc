package com.gradecak.alfresco.mvc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.gradecak.alfresco.mvc.rest.annotation.AlfrescoDispatcherWebscript;
import com.gradecak.alfresco.mvc.rest.config.AlfrescoRestRegistrar;

@Configuration
@AlfrescoDispatcherWebscript(servletContext = AlfrescoMvcRestServletContext.class)
public class AlfrescoMvcRestModuleConfigurationSingle {
}
