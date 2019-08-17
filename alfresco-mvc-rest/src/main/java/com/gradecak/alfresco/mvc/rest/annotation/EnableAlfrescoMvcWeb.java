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

package com.gradecak.alfresco.mvc.rest.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.gradecak.alfresco.mvc.rest.config.DefaultAlfrescoMvcServletContextConfig;

/**
 * use this lass in order to import
 * <code>DefaultAlfrescoMvcServletContextConfig</code> and to add @EnableWebMvc.
 * You can omit this annotation and directly use @EnableWebMvc
 * 
 * The default configuration reuse the Alfresco jackson configuration
 * <code>org.alfresco.rest.framework.jacksonextensions.RestJsonModule</code>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EnableWebMvc
@Import(DefaultAlfrescoMvcServletContextConfig.class)
public @interface EnableAlfrescoMvcWeb {
}
