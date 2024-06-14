/*-
 * #%L
 * Alfresco MVC rest
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
package com.gradecak.alfresco.mvc.rest.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;

import com.gradecak.alfresco.mvc.rest.config.AlfrescoRestRegistrar;
import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript.ServletConfigOptions;

@Repeatable(EnableAlfrescoMvcRest.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(AlfrescoRestRegistrar.class)
public @interface AlfrescoDispatcherWebscript {
	String name() default "alfresco-mvc.mvc";

	RequestMethod[] httpMethods() default { RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE,
			RequestMethod.PUT, RequestMethod.HEAD, RequestMethod.PATCH, RequestMethod.OPTIONS, RequestMethod.TRACE };

	Class<?> servletContext();

	Class<? extends WebApplicationContext> servletContextClass() default org.springframework.web.context.support.AnnotationConfigWebApplicationContext.class;

	boolean inheritGlobalProperties() default false;

	ServletConfigOptions[] servletConfigOptions() default {};
}
