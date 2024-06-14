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

import java.util.ArrayList;
import java.util.List;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ObjectUtils;

public class PackageAutoProxyCreator extends AbstractAutoProxyCreator implements InitializingBean {

	private static final long serialVersionUID = -1219238254256448615L;

	public static final String[] DEFAULT_INTERCEPTORS = { "mvc.aop.alfrescoAuthenticationAdvisor",
			"mvc.aop.alfrescoRunAsAdvisor", "mvc.aop.alfrescoTransactionAdvisor" };

	private String basePackage;
	private boolean skipDefaultInterceptos = false;
	private boolean defaultInterceptorsSet = false;

	public void afterPropertiesSet() throws Exception {
		if (!defaultInterceptorsSet) {
			super.setInterceptorNames(withDefaultInterceptorNames(null));
		}
	}

	/**
	 * Identify as bean to proxy if the bean name is in the configured base package.
	 */
	protected Object[] getAdvicesAndAdvisorsForBean(final Class<?> beanClass, final String beanName,
			final TargetSource targetSource) {
		if (this.basePackage != null) {
			if (beanClass != null && beanClass.getPackage() != null
					&& beanClass.getPackage().getName().equals(basePackage)) {
				return PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS;
			}
		}
		return DO_NOT_PROXY;
	}

	@Override
	public void setInterceptorNames(final String... interceptorNames) {
		super.setInterceptorNames(withDefaultInterceptorNames(interceptorNames));
	}

	private String[] withDefaultInterceptorNames(final String[] interceptorNames) {
		List<String> interceptors = new ArrayList<String>();

		if (!skipDefaultInterceptos) {
			for (String interceptorName : DEFAULT_INTERCEPTORS) {
				interceptors.add(interceptorName);
			}
			defaultInterceptorsSet = true;
		}

		if (!ObjectUtils.isEmpty(interceptorNames)) {
			for (String interceptorName : interceptorNames) {
				interceptors.add(interceptorName);
			}
		}

		return interceptors.toArray(new String[interceptors.size()]);
	}

	public void setBasePackage(final String basePackage) {
		this.basePackage = basePackage;
	}

	public void setSkipDefaultInterceptos(final boolean skipDefaultInterceptos) {
		this.skipDefaultInterceptos = skipDefaultInterceptos;
	}
}
