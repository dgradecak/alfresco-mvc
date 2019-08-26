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

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.util.Assert;

/**
 *
 * @deprecated Alfresco 5.2+ include a REST API and we recommend using classes
 *             from the package <code>org.alfresco.rest.api</code>
 */
public class BeanPropertiesMapperRegistry {

	private final NamespaceService namespaceService;
	private final DictionaryService dictionaryService;

	private Map<Class<?>, BeanPropertiesMapper<?>> mappers = new HashMap<>();

	public BeanPropertiesMapperRegistry(final NamespaceService namespaceService,
			final DictionaryService dictionaryService) {
		Assert.notNull(namespaceService, "[Assertion failed] - the namespaceService argument must be null");
		Assert.notNull(dictionaryService, "[Assertion failed] - the dictionaryService argument must be null");

		this.namespaceService = namespaceService;
		this.dictionaryService = dictionaryService;
	}

	@SuppressWarnings("unchecked")
	public <T> BeanPropertiesMapper<T> getForClass(Class<T> clazz) {
		BeanPropertiesMapper<T> mapper = (BeanPropertiesMapper<T>) mappers.get(clazz);
		if (mapper == null) {
			mapper = new BeanPropertiesMapperBuilder<T>().mappedClass(clazz).namespaceService(namespaceService)
					.dictionaryService(dictionaryService).build();

			addBeanPropertiesMapper(mapper);
		}
		return mapper;
	}

	public BeanPropertiesMapperRegistry addBeanPropertiesMappers(BeanPropertiesMapper<?>... mappers) {
		for (BeanPropertiesMapper<?> mapper : mappers) {
			addBeanPropertiesMapper(mapper);
		}
		return this;
	}

	public BeanPropertiesMapperRegistry addBeanPropertiesMapper(BeanPropertiesMapper<?> mapper) {
		Assert.notNull(mapper, "[Assertion failed] - the mapper argument must be null");
		Class<?> clazz = mapper.getMappedClass();
		BeanPropertiesMapper<?> existingMapper = mappers.get(clazz);
		if (existingMapper != null) {
			throw new RuntimeException("A mapper is alread configured for the class: " + clazz.getName());
		}

		mappers.put(clazz, mapper);

		return this;
	}
}
