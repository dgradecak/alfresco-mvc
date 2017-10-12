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

package com.gradecak.alfresco.mvc.jackson;

import java.io.IOException;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.springframework.util.StringUtils;

/**
 * @deprecated
 */
public class QnameDeserializer extends JsonDeserializer<QName> {

  private ServiceRegistry serviceRegistry;

  public QnameDeserializer(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }

  @Override
  public QName deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

    String qname = jp.getText();
    if (StringUtils.hasText(qname)) {
      return QName.createQName(qname, serviceRegistry.getNamespaceService());
    }

    throw ctxt.mappingException("Expected a valid QName string representation");
  }

}
