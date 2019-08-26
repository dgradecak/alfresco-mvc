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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 *
 * @deprecated Alfresco 5.2+ include a REST API and we recommend using classes
 *             from the package <code>org.alfresco.rest.api</code>
 */
public class BeanPropertiesMapperUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(BeanPropertiesMapperUtil.class);

	private final BeanPropertiesMapperRegistry beanPropertiesMapperRegistry;
	private final NodePropertiesMapperUtil mapperUtil;

	public BeanPropertiesMapperUtil(final BeanPropertiesMapperRegistry beanPropertiesMapperRegistry) {
		Assert.notNull(beanPropertiesMapperRegistry, "A beanPropertiesMapperRegistry is required.");

		this.beanPropertiesMapperRegistry = beanPropertiesMapperRegistry;
		this.mapperUtil = new NodePropertiesMapperUtil();
	}

	public <T> List<T> mapResultSet(final ResultSet results, final Class<T> clazz) {
		Assert.notNull(results, "[Assertion failed] - the results argument must be null");
		Assert.notNull(clazz, "[Assertion failed] - the clazz argument must be null");

		BeanPropertiesMapper<T> mapper = beanPropertiesMapperRegistry.getForClass(clazz);
		return mapperUtil.mapResultSet(results, mapper);
	}

	public <T> T mapProperties(final NodeRef nodeRef, final Map<QName, Serializable> properties, final Class<T> clazz) {
		Assert.notNull(nodeRef, "[Assertion failed] - the nodeRef argument must be null");
		Assert.notNull(properties, "[Assertion failed] - the properties argument must be null");
		Assert.notNull(clazz, "[Assertion failed] - the clazz argument must be null");

		BeanPropertiesMapper<T> mapper = beanPropertiesMapperRegistry.getForClass(clazz);
		return mapperUtil.mapProperties(nodeRef, properties, mapper);
	}
}
