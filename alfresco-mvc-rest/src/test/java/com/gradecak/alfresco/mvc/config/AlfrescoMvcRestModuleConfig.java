package com.gradecak.alfresco.mvc.config;

import org.springframework.context.annotation.Configuration;

import com.gradecak.alfresco.mvc.rest.annotation.AlfrescoDispatcherWebscript;
import com.gradecak.alfresco.mvc.rest.annotation.EnableAlfrescoMvcRest;

@Configuration
@EnableAlfrescoMvcRest(@AlfrescoDispatcherWebscript(servletContext = AlfrescoMvcRestServletContext.class))
public class AlfrescoMvcRestModuleConfig {
}
