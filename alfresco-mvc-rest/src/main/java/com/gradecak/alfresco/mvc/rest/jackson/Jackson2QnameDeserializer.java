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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class Jackson2QnameDeserializer extends JsonDeserializer<QName> implements Converter<String, QName> {

	private ServiceRegistry serviceRegistry;

	@Autowired
	public Jackson2QnameDeserializer(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	@Override
	public QName deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
		String qname = jp.getText();
		return QName.createQName(qname, serviceRegistry.getNamespaceService());
	}

	@Override
	public QName convert(String qname) {
		return QName.createQName(qname, serviceRegistry.getNamespaceService());
	}

}
