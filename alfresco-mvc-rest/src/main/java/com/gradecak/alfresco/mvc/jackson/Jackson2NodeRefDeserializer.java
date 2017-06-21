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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class Jackson2NodeRefDeserializer extends JsonDeserializer<NodeRef>implements Converter<String, NodeRef> {

  public Jackson2NodeRefDeserializer() {}

  @Override
  public NodeRef deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

    String id = jp.getText();
    if (StringUtils.hasText(id)) {
      return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    }

    throw ctxt.mappingException("Expected a valid NodeRef string representation");
  }

  @Override
  public NodeRef convert(String id) {
    if (!StringUtils.hasText(id)) {
      return null;
    }
    return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
  }

}
