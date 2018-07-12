package com.gradecak.alfresco.mvc.util;

import java.util.Map;

import java.util.Map.Entry;

public class JsonUtils {
  
  private JsonUtils() {
  }
  
  public static String mapToJsonString(Map<String,Object> variables) {
    StringBuilder jsonBuilder = new StringBuilder();
    jsonBuilder.append("{");
    for(Entry<String, Object> entry: variables.entrySet()) {
      addEntry(jsonBuilder, entry.getKey(),entry.getValue());
      jsonBuilder.append(",");
    }
    
    if(jsonBuilder.length()>1) {
      jsonBuilder.deleteCharAt(jsonBuilder.length()-1);
    }
    
    jsonBuilder.append("}");
    return jsonBuilder.toString();
  }

  private static void addEntry(StringBuilder jsonBuilder,String key, Object value) {
    jsonBuilder.append("\"");
    jsonBuilder.append(key);
    jsonBuilder.append("\"");
    jsonBuilder.append(":");
    if(value instanceof String) {
      jsonBuilder.append("\"");
    }
    jsonBuilder.append(value);
    if(value instanceof String) {
      jsonBuilder.append("\"");
    }
  }

}
