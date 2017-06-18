package com.gradecak.alfresco.mvc.sample.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.gradecak.alfresco.mvc.config.AlfrescoMvcServletConfig;
import com.gradecak.alfresco.mvc.sample.controller.AlfrescoMvcAopController;

@Configuration
@ComponentScan(basePackageClasses = { AlfrescoMvcAopController.class })
@EnableWebMvc
public class AlfrescoMvcAopServletContext extends AlfrescoMvcServletConfig {

}
