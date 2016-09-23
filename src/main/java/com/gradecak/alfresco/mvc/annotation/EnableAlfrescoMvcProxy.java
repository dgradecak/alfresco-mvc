package com.gradecak.alfresco.mvc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;

import com.gradecak.alfresco.mvc.aop.AlfrescoProxyRegistrar;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
//@ImportResource("classpath*:alfresco/module/mvc/context/aop-context.xml")
@Import(AlfrescoProxyRegistrar.class)
public @interface EnableAlfrescoMvcProxy {
  String[]value() default {};

  boolean proxyTargetClass() default true;

  AdviceMode mode() default AdviceMode.PROXY;

  String[]basePackages() default {};

  Class<?>[]basePackageClasses() default {};
}
