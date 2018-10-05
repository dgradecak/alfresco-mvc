package com.gradecak.alfresco.mvc.rest.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.gradecak.alfresco.mvc.rest.config.AlfrescoMvcServletContextConfig;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EnableWebMvc
@Import(AlfrescoMvcServletContextConfig.class)
public @interface EnableAlfrescoMvcServlet {

}
