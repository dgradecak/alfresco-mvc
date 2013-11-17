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

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;

public class PackageAutoProxyCreator extends AbstractAutoProxyCreator {

	private static final long serialVersionUID = -1219238254256448615L;

	private String basePackage;

	/**
	 * Identify as bean to proxy if the bean name is in the configured list of names.
	 */
	protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, TargetSource targetSource) {
		if (this.basePackage != null) {
			if (beanClass != null && beanClass.getPackage() != null && beanClass.getPackage().getName().equals(basePackage)) { return PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS; }
		}
		return DO_NOT_PROXY;
	}

	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}
}
