package com.gradecak.alfresco.mvc;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class ResponseMapBuilder {

  private final Builder<String, Object> variables = ImmutableMap.builder();

  public ResponseMapBuilder withData(final Object data) {

    int size = 1;
    if (data != null) {
      if (data instanceof List<?>) {
        size = ((List<?>) data).size();
      }
    }

    return withEntry("data", data).withEntry("total", size);
  }

  public ResponseMapBuilder withSuccess(final boolean success) {
    return withEntry("success", success);
  }

  public ResponseMapBuilder withEntry(final String key, final Object value) {
    if (value != null) {
      variables.put(key, value);
    }
    return this;
  }

  public Map<String, Object> build() {
    return variables.build();
  }

  static public ResponseMapBuilder createSuccessResponseMap() {
    return new ResponseMapBuilder().withSuccess(true);
  }

  static public ResponseMapBuilder createFailResponseMap() {
    return new ResponseMapBuilder().withSuccess(false);
  }

  static public ResponseMapBuilder createResponseMap(final Object data, final boolean success) {
    return new ResponseMapBuilder().withData(data).withSuccess(success);
  }
  
  static public ResponseMapBuilder createResponseMap(final CountData<?> data, final boolean success) {
    return new ResponseMapBuilder().withData(data.dataList).withEntry("count", data.count).withSuccess(success);
  }
}
