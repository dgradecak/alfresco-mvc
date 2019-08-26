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

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.util.Assert;

public class BeanPropertiesMapper<T> implements NodePropertiesMapper<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BeanPropertiesMapper.class);

	private final BeanPropertiesMapperConfigurer<T> configurer;
	private final NamespaceService namespaceService;
	private final DictionaryService dictionaryService;
	private final boolean reportNamespaceException;

	private Class<T> mappedClass;
	private Map<String, PropertyDescriptor> mappedFields;
	private Map<QName, PropertyDescriptor> mappedQNames;
	private Set<String> mappedProperties;

	protected BeanPropertiesMapper(final NamespaceService namespaceService, final DictionaryService dictionaryService) {
		this(namespaceService, dictionaryService, false);
	}

	protected BeanPropertiesMapper(final NamespaceService namespaceService, final DictionaryService dictionaryService,
			final boolean reportNamespaceException, Class<T> mappedClass) {
		this(namespaceService, dictionaryService, reportNamespaceException);
		if (this.mappedClass == null) {
			setMappedClass(mappedClass);
		}
	}

	protected BeanPropertiesMapper(final NamespaceService namespaceService, final DictionaryService dictionaryService,
			final boolean reportNamespaceException) {
		this(namespaceService, dictionaryService, null, reportNamespaceException);
	}

	@SuppressWarnings("unchecked")
	protected BeanPropertiesMapper(final NamespaceService namespaceService, final DictionaryService dictionaryService,
			final BeanPropertiesMapperConfigurer<T> configurer, final boolean reportNamespaceException) {
		Assert.notNull(namespaceService, "[Assertion failed] - the namespaceService argument must be null");
		Assert.notNull(dictionaryService, "[Assertion failed] - the dictionaryService argument must be null");

		this.namespaceService = namespaceService;
		this.dictionaryService = dictionaryService;
		this.reportNamespaceException = reportNamespaceException;

		Class<T> mappedClass = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(),
				NodePropertiesMapper.class);
		if (mappedClass != null) {
			setMappedClass(mappedClass);
		}

		BeanPropertiesMapperConfigurer<T> confTmp = configurer;
		if (configurer == null) {
			if (this instanceof BeanPropertiesMapperConfigurer) {
				confTmp = ((BeanPropertiesMapperConfigurer<T>) this);
			}
		}

		this.configurer = confTmp;
	}

	public BeanPropertiesMapperConfigurer<T> getConfigurer() {
		return configurer;
	}

	public T mapNodeProperties(final NodeRef nodeRef, final Map<QName, Serializable> properties) {
		try {
			T mappedObject = this.mappedClass.newInstance();

			BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(mappedObject);
			afterBeanWrapperInitialized(bw);

			for (Map.Entry<QName, PropertyDescriptor> entry : mappedQNames.entrySet()) {
				QName qName = entry.getKey();

				PropertyDescriptor pd = entry.getValue();
				if (pd != null) {
					bw.setPropertyValue(pd.getName(), properties.get(qName));
				}
			}

			if (configurer != null) {
				configurer.configure(nodeRef, mappedObject);
			}

			return mappedObject;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Set<QName> getMappedQnames() {
		return mappedQNames.keySet();
	}

	/**
	 * Initialize the given BeanWrapper to be used for row mapping. To be called for
	 * each row.
	 * <p>
	 * The default implementation is empty. Can be overridden in subclasses.
	 * 
	 * @param bw the BeanWrapper to initialize
	 */
	protected void afterBeanWrapperInitialized(BeanWrapper bw) {
	}

	/**
	 * Set the class that properties should be mapped to.
	 */
	protected void setMappedClass(Class<T> mappedClass) {
		if (this.mappedClass == null) {
			initialize(mappedClass);
		} else {
			if (!this.mappedClass.equals(mappedClass)) {
				throw new InvalidDataAccessApiUsageException("The mapped class can not be reassigned to map to "
						+ mappedClass + " since it is already providing mapping for " + this.mappedClass);
			}
		}

		Assert.notNull(this.mappedClass, "[Assertion failed] - the mappedClass argument must be null");
	}

	public Class<T> getMappedClass() {
		return mappedClass;
	}

	/**
	 * Initialize the mapping metadata for the given class.
	 * 
	 * @param mappedClass the mapped class.
	 */
	private void initialize(Class<T> mappedClass) {
		this.mappedClass = mappedClass;
		this.mappedFields = new HashMap<String, PropertyDescriptor>();
		this.mappedProperties = new HashSet<String>();
		this.mappedQNames = new HashMap<QName, PropertyDescriptor>();
		PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(mappedClass);
		for (PropertyDescriptor pd : pds) {
			if (pd.getWriteMethod() != null) {
				this.mappedFields.put(pd.getName().toLowerCase(), pd);
				String prefixName = prefixName(pd.getName());
				if (!pd.getName().toLowerCase().equals(prefixName)) {
					this.mappedFields.put(prefixName, pd);
				}
				this.mappedProperties.add(pd.getName());

				String prefixedString = prefixName.replaceFirst("_", ":");

				if (prefixedString.contains(":")) {
					try {
						QName qName = QName.createQName(prefixedString, namespaceService);
						if (dictionaryService.getProperty(qName) != null) {
							this.mappedQNames.put(qName, pd);
						}
					} catch (NamespaceException e) {
						LOGGER.warn("the property is not configured for this namespace", e);
						if (reportNamespaceException) {
							throw e;
						}
					}
				}
			}
		}

		afterInitialize();
	}

	protected void afterInitialize() {
	}

	/**
	 * Convert a name in camelCase to an underscored name in lower case. Any upper
	 * case letters are converted to lower case with a preceding underscore.
	 * 
	 * @param name the string containing original name
	 * @return the converted name
	 */
	private String prefixName(String name) {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		if (name != null && name.length() > 0) {
			result.append(name.substring(0, 1).toLowerCase());
			for (int i = 1; i < name.length(); i++) {
				String s = name.substring(i, i + 1);
				if (s.equals(s.toUpperCase())) {
					if (first) {
						result.append("_");
						result.append(s.toLowerCase());
						first = false;
					} else {
						result.append(s);
					}
				} else {
					result.append(s);
				}
			}
		}
		return result.toString();
	}
}
