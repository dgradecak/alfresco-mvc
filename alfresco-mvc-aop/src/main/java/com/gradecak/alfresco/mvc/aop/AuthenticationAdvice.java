/*-
 * #%L
 * Alfresco MVC aop
 * %%
 * Copyright (C) 2007 - 2024 gradecak.com
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.gradecak.alfresco.mvc.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import com.gradecak.alfresco.mvc.annotation.AuthenticationType;

public class AuthenticationAdvice implements MethodInterceptor {

	private final ServiceRegistry serviceRegistry;

	public AuthenticationAdvice(final ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

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

				String ticket = authenticationService.getCurrentTicket();
				if (StringUtils.hasText(ticket)) {
					if (AuthenticationType.USER.equals(authenticationType) && authorityService.hasGuestAuthority()) {
						throw new AuthenticationException(
								"User has guest authority where at least a user authentication is required.");
					} else if (AuthenticationType.ADMIN.equals(authenticationType)
							&& !authorityService.hasAdminAuthority()) {
						throw new AuthenticationException(
								"User does not have admin authority where at least named admin authentication is required .");
					}
				} else if (AuthenticationType.GUEST.equals(authenticationType)
						&& authenticationService.guestUserAuthenticationAllowed()) {
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
}
