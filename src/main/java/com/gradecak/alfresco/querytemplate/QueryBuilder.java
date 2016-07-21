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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.springframework.util.StringUtils;

import com.gradecak.alfresco.mvc.Query;


/**
 * supports only lucene/solr and fts_alfresco languages. Other languages have not been tested, might be that it works for
 * some of them.
 * 
 * @author dgradecak
 */
public class QueryBuilder extends Query{
  
  private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
  private final StringBuilder queryStringBuilder = new StringBuilder();

  private String language;

  public QueryBuilder() {
    withLanguage(SearchService.LANGUAGE_LUCENE);
  }

  public QueryBuilder(final String language) {
    withLanguage(language);
  }
  
  public String getLanguage() {
    return language;
  }
  
  public QueryBuilder withLanguage(final String language) {
    if (!SearchService.LANGUAGE_LUCENE.equals(language) && !SearchService.LANGUAGE_FTS_ALFRESCO.equals(language)) {
      // TODO add logger
    }

    this.language = language;
    
    return this;
  }

  public QueryBuilder exactAnd(Map<QName, Serializable> properties) {
    for (Entry<QName, Serializable> entry : properties.entrySet()) {
      Object value = entry.getValue();

      String convert = value == null ? null : convert(value);
      if (StringUtils.hasText(convert)) {
        this.and(); // FIXME
        this.property(entry.getKey()).exact(value);
      }
    }

    return this;
  }

  public QueryBuilder exactOr(Map<QName, Serializable> properties) {
    for (Entry<QName, Serializable> entry : properties.entrySet()) {
      Object value = entry.getValue();

      String convert = value == null ? null : convert(value);
      if (StringUtils.hasText(convert)) {
        this.or(); // FIXME
        this.property(entry.getKey()).exact(value);
      }
    }

    return this;
  }

  public QueryBuilder likeAnd(Map<QName, Serializable> properties) {
    for (Entry<QName, Serializable> entry : properties.entrySet()) {
      Object value = entry.getValue();

      String convert = value == null ? null : convert(value);
      if (StringUtils.hasText(convert)) {
        this.and(); // FIXME
        this.property(entry.getKey()).like(convert.toLowerCase());
      }
    }

    return this;
  }

  public QueryBuilder likeOr(Map<QName, Serializable> properties) {
    for (Entry<QName, Serializable> entry : properties.entrySet()) {
      Object value = entry.getValue();

      String convert = value == null ? null : convert(value);
      if (StringUtils.hasText(convert)) {
        this.or(); // FIXME
        this.property(entry.getKey()).like(convert.toLowerCase());
      }
    }

    return this;
  }

  public QueryBuilder containsAnd(Map<QName, Serializable> properties) {
    for (Entry<QName, Serializable> entry : properties.entrySet()) {
      Object value = entry.getValue();

      String convert = value == null ? null : convert(value);
      if (StringUtils.hasText(convert)) {
        this.and(); // FIXME
        this.property(entry.getKey()).contains(value);
      }
    }

    return this;
  }

  public QueryBuilder containsOr(Map<QName, Serializable> properties) {
    for (Entry<QName, Serializable> entry : properties.entrySet()) {
      Object value = entry.getValue();

      String convert = value == null ? null : convert(value);
      if (StringUtils.hasText(convert)) {
        this.or(); // FIXME
        this.property(entry.getKey()).contains(value);
      }
    }

    return this;
  }

  public QueryBuilder append(String expression) {
    queryStringBuilder.append(expression);
    return this;
  }

  public QueryBuilder exact(Object pCriteria) {
    String criteria = convert(pCriteria);
    queryStringBuilder.append("\"");
    queryStringBuilder.append(criteria);
    queryStringBuilder.append("\"");

    return this;
  }

  public QueryBuilder like(Object pCriteria) {
    String criteria = convert(pCriteria);
    queryStringBuilder.append("\"");
    queryStringBuilder.append(criteria);
    queryStringBuilder.append("*\"");

    return this;
  }

  public QueryBuilder contains(Object pCriteria) {
    String criteria = convert(pCriteria);
    queryStringBuilder.append("\"*");
    queryStringBuilder.append(criteria);
    queryStringBuilder.append("*\"");

    return this;
  }

  public QueryBuilder property(QName property) {
    queryStringBuilder.append("@");
    queryStringBuilder.append(escapeQName(this.language, property));
    queryStringBuilder.append(":");
    return this;
  }

  public QueryBuilder isNotNull(QName property) {
    queryStringBuilder.append("ISNOTNULL");
    queryStringBuilder.append(":");
    queryStringBuilder.append("\"");
    queryStringBuilder.append(escapeQName(this.language, property));
    queryStringBuilder.append("\"");
    return this;
  }

  public QueryBuilder isNull(QName property) {
    queryStringBuilder.append("ISNULL");
    queryStringBuilder.append(":");
    queryStringBuilder.append("\"");
    queryStringBuilder.append(escapeQName(this.language, property));
    queryStringBuilder.append("\"");
    return this;
  }

  public QueryBuilder isUnset(QName property) {
    queryStringBuilder.append("ISUNSET");
    queryStringBuilder.append(":");
    queryStringBuilder.append("\"");
    queryStringBuilder.append(escapeQName(this.language, property));
    queryStringBuilder.append("\"");
    return this;
  }

  public QueryBuilder id(String id) {
    queryStringBuilder.append("ID:\"");
    queryStringBuilder.append(escape(this.language, id));
    queryStringBuilder.append("\"");
    return this;
  }

  public QueryBuilder type(QName type) {
    queryStringBuilder.append("TYPE:\"");
    queryStringBuilder.append(escapeQName(this.language, type));
    queryStringBuilder.append("\"");
    return this;
  }

  public QueryBuilder parent(NodeRef parentRef) {
    queryStringBuilder.append("PARENT:\"");
    queryStringBuilder.append(parentRef.toString());
    queryStringBuilder.append("\"");
    return this;
  }

  public QueryBuilder aspect(QName aspect) {
    queryStringBuilder.append("ASPECT:\"");
    queryStringBuilder.append(escapeQName(this.language, aspect));
    queryStringBuilder.append("\"");
    return this;
  }

  public QueryBuilder or() {
    if (queryStringBuilder.length() != 0) {
      queryStringBuilder.append(" OR ");
    }
    return this;
  }

  public QueryBuilder not() {
    queryStringBuilder.append(" NOT ");
    return this;
  }

  public QueryBuilder and() {
    if (queryStringBuilder.length() != 0) {
      queryStringBuilder.append(" AND ");
    }
    return this;
  }

  public QueryBuilder range(Object start, Object end) {
    queryStringBuilder.append("[");
    queryStringBuilder.append(start != null ? convert(start) : "MIN");
    queryStringBuilder.append(" TO ");
    queryStringBuilder.append(end != null ? convert(end) : "MAX");
    queryStringBuilder.append("]");
    return this;
  }

  public QueryBuilder path(final String path) {
    queryStringBuilder.append("PATH:\"");
    queryStringBuilder.append(path);
    queryStringBuilder.append("\"");
    return this;
  }

  public QueryBuilder text(final String term) {
    queryStringBuilder.append("TEXT:\"");
    queryStringBuilder.append(term);
    queryStringBuilder.append("\"");
    return this;
  }

  private String convert(Object value) {
    if (value instanceof Date) {
      return simpleDateFormat.format(value);
    }

    return value.toString();
  }

  public String toString() {
    return queryStringBuilder.toString();
  }
  
  public String build() {
    return queryStringBuilder.toString();
  }

  public static String escapeQName(final String language, final QName qName) {
    String string = qName.toString();
    return escape(language, string);
  }

  public static String escape(final String language, final String string) {
    if (!SearchService.LANGUAGE_LUCENE.equals(language)) {
      return string;
    }

    final int numOfCharsToAdd = 4;

    StringBuilder builder = new StringBuilder(string.length() + numOfCharsToAdd);
    for (int i = 0; i < string.length(); i++) {
      char character = string.charAt(i);
      if ((character == '{') || (character == '}') || (character == ':') || (character == '-')) {
        builder.append('\\');
      }

      builder.append(character);
    }
    return builder.toString();
  }
  
}
