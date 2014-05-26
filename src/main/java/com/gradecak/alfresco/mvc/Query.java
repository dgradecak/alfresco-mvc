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

package com.gradecak.alfresco.mvc;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.util.StringUtils;

public class Query {
	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
	private final StringBuilder queryBuilder = new StringBuilder();

	public Query exactAnd(Map<QName, Serializable> properties) {
		for (Entry<QName, Serializable> entry : properties.entrySet()) {
			Object value = entry.getValue();

			String convert = value == null ? null : convert(value);
			if (StringUtils.hasText(convert)) {
				this.and();
				this.property(entry.getKey()).exact(value);
			}
		}

		return this;
	}

	public Query exactOr(Map<QName, Serializable> properties) {
		for (Entry<QName, Serializable> entry : properties.entrySet()) {
			Object value = entry.getValue();

			String convert = value == null ? null : convert(value);
			if (StringUtils.hasText(convert)) {
				this.or();
				this.property(entry.getKey()).exact(value);
			}
		}

		return this;
	}

	public Query likeAnd(Map<QName, Serializable> properties) {
		for (Entry<QName, Serializable> entry : properties.entrySet()) {
			Object value = entry.getValue();

			String convert = value == null ? null : convert(value);
			if (StringUtils.hasText(convert)) {
				this.and();
				this.property(entry.getKey()).like(convert.toLowerCase());
			}
		}

		return this;
	}

	public Query likeOr(Map<QName, Serializable> properties) {
		for (Entry<QName, Serializable> entry : properties.entrySet()) {
			Object value = entry.getValue();

			String convert = value == null ? null : convert(value);
			if (StringUtils.hasText(convert)) {
				this.or();
				this.property(entry.getKey()).like(convert.toLowerCase());
			}
		}

		return this;
	}

	public Query containsAnd(Map<QName, Serializable> properties) {
		for (Entry<QName, Serializable> entry : properties.entrySet()) {
			Object value = entry.getValue();

			String convert = value == null ? null : convert(value);
			if (StringUtils.hasText(convert)) {
				this.and();
				this.property(entry.getKey()).contains(value);
			}
		}

		return this;
	}

	public Query containsOr(Map<QName, Serializable> properties) {
		for (Entry<QName, Serializable> entry : properties.entrySet()) {
			Object value = entry.getValue();

			String convert = value == null ? null : convert(value);
			if (StringUtils.hasText(convert)) {
				this.or();
				this.property(entry.getKey()).contains(value);
			}
		}

		return this;
	}

	public Query append(String expression) {
		queryBuilder.append(expression);
		return this;
	}

	public Query exact(Object pCriteria) {
		String criteria = convert(pCriteria);
		queryBuilder.append("\"");
		queryBuilder.append(criteria);
		queryBuilder.append("\"");

		return this;
	}

	public Query like(Object pCriteria) {
		String criteria = convert(pCriteria);
		queryBuilder.append("\"");
		queryBuilder.append(criteria);
		queryBuilder.append("*\"");

		return this;
	}

	public Query contains(Object pCriteria) {
		String criteria = convert(pCriteria);
		queryBuilder.append("\"*");
		queryBuilder.append(criteria);
		queryBuilder.append("*\"");

		return this;
	}

	public Query property(QName property) {
		queryBuilder.append("@");
		queryBuilder.append(escapeQName(property));
		queryBuilder.append(":");
		return this;
	}

	public Query isNotNull(QName property) {
		queryBuilder.append("ISNOTNULL");
		queryBuilder.append(":");
		queryBuilder.append("\"");
		queryBuilder.append(escapeQName(property));
		queryBuilder.append("\"");
		return this;
	}

	public Query isNull(QName property) {
		queryBuilder.append("ISNULL");
		queryBuilder.append(":");
		queryBuilder.append("\"");
		queryBuilder.append(escapeQName(property));
		queryBuilder.append("\"");
		return this;
	}

	public Query isUnset(QName property) {
		queryBuilder.append("ISUNSET");
		queryBuilder.append(":");
		queryBuilder.append("\"");
		queryBuilder.append(escapeQName(property));
		queryBuilder.append("\"");
		return this;
	}

	public Query id(String id) {
		queryBuilder.append("ID:\"");
		queryBuilder.append(escape(id));
		queryBuilder.append("\"");
		return this;
	}

	public Query type(QName type) {
		queryBuilder.append("TYPE:\"");
		queryBuilder.append(escapeQName(type));
		queryBuilder.append("\"");
		return this;
	}

	public Query parent(NodeRef parentRef) {
		queryBuilder.append("PARENT:\"");
		queryBuilder.append(parentRef.toString());
		queryBuilder.append("\"");
		return this;
	}

	public Query aspect(QName aspect) {
		queryBuilder.append("ASPECT:\"");
		queryBuilder.append(escapeQName(aspect));
		queryBuilder.append("\"");
		return this;
	}

	public Query or() {
		if (queryBuilder.length() != 0) {
			queryBuilder.append(" OR ");
		}
		return this;
	}

	public Query not() {
		queryBuilder.append(" NOT ");
		return this;
	}

	public Query and() {
		if (queryBuilder.length() != 0) {
			queryBuilder.append(" AND ");
		}
		return this;
	}

	public Query range(Object start, Object end) {
		queryBuilder.append("[");
		queryBuilder.append(start != null ? convert(start) : "MIN");
		queryBuilder.append(" TO ");
		queryBuilder.append(end != null ? convert(end) : "MAX");
		queryBuilder.append("]");
		return this;
	}

	public Query path(final String path) {
		queryBuilder.append("PATH:\"");
		queryBuilder.append(path);
		queryBuilder.append("\"");
		return this;
	}

	private String convert(Object value) {
		if (value instanceof Date) { return simpleDateFormat.format(value); }

		return value.toString();
	}

	public String toString() {
		return queryBuilder.toString();
	}

	public static String escapeQName(QName qName) {
		String string = qName.toString();
		return escape(string);
	}

	public static String escape(String string) {
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
