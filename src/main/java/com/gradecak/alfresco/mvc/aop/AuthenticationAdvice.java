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
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import com.gradecak.alfresco.mvc.annotation.AuthenticationType;

public class AuthenticationAdvice implements MethodInterceptor {

  private ServiceRegistry serviceRegistry;

  public Object invoke(final MethodInvocation invocation) throws Throwable {

    Class<?> targetClass = invocation.getThis() != null ? invocation.getThis().getClass() : null;

    Method specificMethod = ClassUtils.getMostSpecificMethod(invocation.getMethod(), targetClass);
    // If we are dealing with method with generic parameters, find the original
    // method.
    specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);

    AlfrescoAuthentication alfrescoAuthentication = parseAnnotation(specificMethod);

    if (alfrescoAuthentication != null) {

      AuthenticationType authenticationType = alfrescoAuthentication.value();

      if (authenticationType != null && !AuthenticationType.NONE.equals(authenticationType)) {
        AuthenticationService authenticationService = serviceRegistry.getAuthenticationService();
        AuthorityService authorityService = serviceRegistry.getAuthorityService();

        String ticket = getTicket();
        if (StringUtils.hasText(ticket)) {
          authenticationService.validate(ticket);
          if (AuthenticationType.USER.equals(authenticationType) && authorityService.hasGuestAuthority()) {
            throw new AuthenticationException("User has guest authority where at least a user authentication is required.");
          } else if (AuthenticationType.ADMIN.equals(authenticationType) && !authorityService.hasAdminAuthority()) {
            throw new AuthenticationException("User does not have admin authority where at least named admin authentication is required .");
          }
        } else if (AuthenticationType.GUEST.equals(authenticationType) && authenticationService.guestUserAuthenticationAllowed()) {
          authenticationService.authenticateAsGuest();
        } else {
          throw new AuthenticationException("\nUnable to authenticate due to one of the following reasons:\n"
              + "Credentials are not provided in HTTP request where at least named user or admin authentication is required.\n"
              + "Guest user authentication is not allowed where at least guest authentication is required.\n");
        }
      }
    }

    return invocation.proceed();
  }

  private String getTicket() {

    String ticket = "";
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (int i = 0; i < cookies.length; i++) {
        Cookie cookie = cookies[i];
        if (cookie != null && "TICKET".equals(cookie.getName().toUpperCase())) {
          ticket = cookie.getValue();
        }
      }
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> parameterMap = request.getParameterMap();

    if (parameterMap != null) {
      for (Object parameter : parameterMap.keySet()) {
        if (parameter != null && "TICKET".equals(((String) parameter).toUpperCase())) {
          ticket = (String) parameterMap.get(parameter);
        }
      }
    }

    // HttpSession session = request.getSession();
    // if (session != null) {
    // // TODO dgradecak: FIX THIS
    // User user = (User)session.getAttribute("_alfAuthTicket");
    // ticket = user.getTicket();
    // }

    return ticket;
  }

  private AlfrescoAuthentication parseAnnotation(AnnotatedElement ae) {
    AlfrescoAuthentication ann = ae.getAnnotation(AlfrescoAuthentication.class);
    if (ann == null) {
      for (Annotation metaAnn : ae.getAnnotations()) {
        ann = metaAnn.annotationType().getAnnotation(AlfrescoAuthentication.class);
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

  private AlfrescoAuthentication parseAnnotation(AlfrescoAuthentication ann) {
    // parse if needed something else
    return ann;
  }

  public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }
}
