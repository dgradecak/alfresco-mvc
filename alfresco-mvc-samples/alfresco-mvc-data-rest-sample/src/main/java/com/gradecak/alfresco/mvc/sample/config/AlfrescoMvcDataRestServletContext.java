package com.gradecak.alfresco.mvc.sample.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.gradecak.alfresco.mvc.data.rest.config.AlfrescoMvcDataRestHateoasServletContext;
import com.gradecak.alfresco.mvc.data.rest.controller.NodeController;

@Configuration
@ComponentScan(basePackageClasses = { NodeController.class })
public class AlfrescoMvcDataRestServletContext extends AlfrescoMvcDataRestHateoasServletContext {}
