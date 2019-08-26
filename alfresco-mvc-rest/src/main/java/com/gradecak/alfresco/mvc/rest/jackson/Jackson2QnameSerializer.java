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

package com.gradecak.alfresco.mvc.rest.jackson;

import java.io.IOException;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class Jackson2QnameSerializer extends StdSerializer<QName> {

	private ServiceRegistry serviceRegistry;

	public Jackson2QnameSerializer(ServiceRegistry serviceRegistry) {
		super(QName.class);
		this.serviceRegistry = serviceRegistry;
	}

	@Override
	public void serialize(QName value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		String prefixString = value.toPrefixString(serviceRegistry.getNamespaceService());
		jgen.writeString(prefixString);
	}

	@Override
	public Class<QName> handledType() {
		return QName.class;
	}

}
