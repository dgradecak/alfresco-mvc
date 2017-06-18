/**
 * Copyright gradecak.com

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradecak.alfresco.mvc.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.gradecak.alfresco.mvc.annotation.AlfrescoRunAs;

public class RunAsAdvice implements MethodInterceptor {

  public Object invoke(final MethodInvocation invocation) throws Throwable {

    Class<?> targetClass = invocation.getThis() != null ? invocation.getThis().getClass() : null;

    Method specificMethod = ClassUtils.getMostSpecificMethod(invocation.getMethod(), targetClass);
    // If we are dealing with method with generic parameters, find the original
    // method.
    specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
    AlfrescoRunAs alfrescounRunAs = parseRunAsAnnotation(specificMethod);
    if (alfrescounRunAs != null) {
      String runAs = alfrescounRunAs.value();
      if (StringUtils.hasText(runAs)) {
        RunAsWork<Object> getUserNameRunAsWork = new RunAsWork<Object>() {
          public Object doWork() throws Exception {
            try {
              return invocation.proceed();
            } catch (Throwable e) {
              throw new Exception(e.getMessage(), e);
            }
          }
        };
        return AuthenticationUtil.runAs(getUserNameRunAsWork, runAs);
      }
    }

    return invocation.proceed();
  }

  private AlfrescoRunAs parseRunAsAnnotation(AnnotatedElement ae) {
    AlfrescoRunAs ann = ae.getAnnotation(AlfrescoRunAs.class);
    if (ann == null) {
      for (Annotation metaAnn : ae.getAnnotations()) {
        ann = metaAnn.annotationType().getAnnotation(AlfrescoRunAs.class);
        if (ann != null) {
          break;
        }
      }
    }
    if (ann != null) {
      return parseAnnotation(ann);
    } else {
      return null;
    }
  }

  private AlfrescoRunAs parseAnnotation(AlfrescoRunAs ann) {
    // parse if needed something else
    return ann;
  }

}
