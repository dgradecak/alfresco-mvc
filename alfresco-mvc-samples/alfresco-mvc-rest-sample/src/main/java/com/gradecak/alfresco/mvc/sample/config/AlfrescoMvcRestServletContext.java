package com.gradecak.alfresco.mvc.sample.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.gradecak.alfresco.mvc.config.AlfrescoMvcServletContextConfig;
import com.gradecak.alfresco.mvc.sample.controller.AlfrescoMvcRestController;

@Configuration
@ComponentScan(basePackageClasses = { AlfrescoMvcRestController.class})
@EnableWebMvc
public class AlfrescoMvcRestServletContext extends AlfrescoMvcServletContextConfig{

}
