package com.gradecak.alfresco.mvc.sample.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.gradecak.alfresco.mvc.data.config.AlfrescoMvcDataServletContextConfig;
import com.gradecak.alfresco.mvc.sample.controller.AlfrescoMvcDataController;

@Configuration
@ComponentScan(basePackageClasses = { AlfrescoMvcDataController.class })
@EnableWebMvc
public class AlfrescoMvcDataServletContext extends AlfrescoMvcDataServletContextConfig {}
