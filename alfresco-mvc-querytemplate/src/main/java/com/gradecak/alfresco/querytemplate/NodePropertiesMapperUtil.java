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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class NodePropertiesMapperUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(NodePropertiesMapperUtil.class);

	public <T> List<T> mapResultSet(final ResultSet results, final NodePropertiesMapper<T> mapper) {
		Assert.notNull(results, "[Assertion failed] - the results argument must be null");
		Assert.notNull(mapper, "[Assertion failed] - the mapper argument must be null");

		List<T> list = new ArrayList<>();
		for (ResultSetRow resultSetRow : results) {
			Map<QName, Serializable> properties = new HashMap<>();

			Set<QName> mappedQnames = mapper.getMappedQnames();
			for (QName qName : mappedQnames) {
				properties.put(qName, resultSetRow.getValue(qName));
			}
			list.add(mapper.mapNodeProperties(resultSetRow.getNodeRef(), properties));
		}

		return list;
	}

	public <T> T mapProperties(final NodeRef nodeRef, final Map<QName, Serializable> properties,
			final NodePropertiesMapper<T> mapper) {
		Assert.notNull(nodeRef, "[Assertion failed] - the nodeRef argument must be null");
		Assert.notNull(properties, "[Assertion failed] - the properties argument must be null");
		Assert.notNull(mapper, "[Assertion failed] - the mapper argument must be null");

		return mapper.mapNodeProperties(nodeRef, properties);
	}
}
