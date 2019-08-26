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

package com.gradecak.alfresco.querytemplate;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.util.Assert;

/**
*
* @deprecated Alfresco 5.2+ include a REST API and we recommend using classes from the package <code>org.alfresco.rest.api</code>
*/
public class BeanPropertiesMapperBuilder<T> {

	private Class<T> mappedClass;
	private BeanPropertiesMapperConfigurer<T> configurer;
	private boolean reportNamespaceException = false;
	private NamespaceService namespaceService;
	private DictionaryService dictionaryService;

	public BeanPropertiesMapperBuilder<T> reportNamespaceException(final boolean reportNamespaceException) {
		this.reportNamespaceException = reportNamespaceException;
		return this;
	}

	public BeanPropertiesMapperBuilder<T> namespaceService(final NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
		return this;
	}

	public BeanPropertiesMapperBuilder<T> dictionaryService(final DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
		return this;
	}

	public BeanPropertiesMapperBuilder<T> mappedClass(Class<T> mappedClass) {
		this.mappedClass = mappedClass;
		return this;
	}

	public BeanPropertiesMapperBuilder<T> configurer(BeanPropertiesMapperConfigurer<T> configurer) {
		this.configurer = configurer;
		return this;
	}

	public BeanPropertiesMapper<T> build() {
		Assert.notNull(namespaceService, "A namespaceService is required.");
		Assert.notNull(dictionaryService, "A dictionaryService is required.");
		Assert.notNull(mappedClass, "A mappedClass is required.");

		BeanPropertiesMapper<T> mapper = new BeanPropertiesMapper<T>(namespaceService, dictionaryService, configurer,
				reportNamespaceException);
		mapper.setMappedClass(mappedClass);
		return mapper;

	}

}
