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

package com.gradecak.alfresco.mvc.rest.converter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.Converter;

public class NodeRefConverter implements ConditionalGenericConverter, Converter<String, NodeRef> {

	private static final Set<ConvertiblePair> CONVERTIBLE_PAIRS = new HashSet<ConvertiblePair>();

	static {
		CONVERTIBLE_PAIRS.add(new ConvertiblePair(String.class, NodeRef.class));
		CONVERTIBLE_PAIRS.add(new ConvertiblePair(UUID.class, NodeRef.class));
	}

	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (String.class.isAssignableFrom(sourceType.getType())) {
			return NodeRef.class.isAssignableFrom(targetType.getType());
		}

		return NodeRef.class.isAssignableFrom(sourceType.getType())
				&& String.class.isAssignableFrom(targetType.getType());
	}

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		return CONVERTIBLE_PAIRS;
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (String.class.isAssignableFrom(sourceType.getType())) {
			return convert((String) source);
		} else {
			return source.toString();
		}
	}

	@Override
	public NodeRef convert(String source) {
		NodeRef nodeRef = null;
		if (!NodeRef.isNodeRef(source)) {
			nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, source);
		} else {
			nodeRef = new NodeRef(source);
		}
		return nodeRef;
	}
}
