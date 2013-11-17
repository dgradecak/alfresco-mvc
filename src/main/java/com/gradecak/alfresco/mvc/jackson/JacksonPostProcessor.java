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

import java.text.SimpleDateFormat;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;

import com.gradecak.alfresco.mvc.converter.QnameDeserializer;
import com.gradecak.alfresco.mvc.converter.QnameSerializer;

public class JacksonPostProcessor implements BeanPostProcessor {
	public static final String DEFAULT_JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	@Autowired
	private ServiceRegistry serviceRegistry;

	private String dateFormat;

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof AnnotationMethodHandlerAdapter) {
			AnnotationMethodHandlerAdapter adapter = (AnnotationMethodHandlerAdapter) bean;
			HttpMessageConverter<?>[] converters = adapter.getMessageConverters();
			for (HttpMessageConverter<?> converter : converters) {
				if (converter instanceof MappingJacksonHttpMessageConverter) {
					MappingJacksonHttpMessageConverter jsonConverter = (MappingJacksonHttpMessageConverter) converter;

					ObjectMapper objectMapper = new ObjectMapper();
					SimpleModule module = new SimpleModule("Alfresco MVC Module", new Version(1, 0, 0, null));
					module.addSerializer(QName.class, new QnameSerializer(serviceRegistry));
					module.addDeserializer(QName.class, new QnameDeserializer(serviceRegistry));

					objectMapper.setDateFormat(new SimpleDateFormat(getDateFormat()));
					objectMapper.registerModule(module);

					jsonConverter.setObjectMapper(objectMapper);
				}
			}
		}
		return bean;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getDateFormat() {
		if (StringUtils.hasText(this.dateFormat)) { return this.dateFormat; }
		return DEFAULT_JSON_DATE_FORMAT;
	}

}
