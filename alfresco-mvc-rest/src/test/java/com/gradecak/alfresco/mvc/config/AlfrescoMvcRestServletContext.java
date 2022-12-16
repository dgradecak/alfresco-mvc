package com.gradecak.alfresco.mvc.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.gradecak.alfresco.mvc.controller.TestController;
import com.gradecak.alfresco.mvc.rest.config.DefaultAlfrescoMvcServletContextConfiguration;

@Configuration
@EnableWebMvc
@ComponentScan(basePackageClasses = { TestController.class })
@Import(DefaultAlfrescoMvcServletContextConfiguration.class)
public class AlfrescoMvcRestServletContext implements WebMvcConfigurer {

}
