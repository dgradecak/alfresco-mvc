package com.gradecak.alfresco.mvc.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.gradecak.alfresco.mvc.controller.TestController;
import com.gradecak.alfresco.mvc.rest.annotation.EnableWebAlfrescoMvc;

@Configuration
@EnableWebAlfrescoMvc
@ComponentScan(basePackageClasses = { TestController.class })
public class AlfrescoMvcRestServletContext implements WebMvcConfigurer {

}
