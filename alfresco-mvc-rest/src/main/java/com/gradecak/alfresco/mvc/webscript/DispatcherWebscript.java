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

package com.gradecak.alfresco.mvc.webscript;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.WrappingWebScriptRequest;
import org.springframework.extensions.webscripts.WrappingWebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;
import org.springframework.util.Assert;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

public class DispatcherWebscript extends AbstractWebScript {

	private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherWebscript.class);
	private static final int EXTENSION_PATH_REGEXP_GROUP_INDEX = 3;

	private final DispatcherWebscriptServlet dispatcherServlet;

	public DispatcherWebscript(DispatcherWebscriptServlet dispatcherServlet) {
		this.dispatcherServlet = dispatcherServlet;
	}

	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		WebScriptServletRequest origReq;
		if (req instanceof WrappingWebScriptRequest) {
			origReq = (WebScriptServletRequest) ((WrappingWebScriptRequest) req).getNext();
		} else {
			origReq = (WebScriptServletRequest) req;
		}

		WebScriptServletResponse wsr;
		if (res instanceof WrappingWebScriptResponse) {
			wsr = (WebScriptServletResponse) ((WrappingWebScriptResponse) res).getNext();
		} else {
			wsr = (WebScriptServletResponse) res;
		}

		final HttpServletResponse sr = wsr.getHttpServletResponse();
		res.setHeader("Cache-Control", "no-cache");

		WebscriptRequestWrapper wrapper = new WebscriptRequestWrapper(origReq);
		try {
			// wrapper.setAttribute(WebUtils.INCLUDE_SERVLET_PATH_ATTRIBUTE, "/s/mvc");
			dispatcherServlet.service(wrapper, sr);

		} catch (Throwable e) {
			LOGGER.error("Failed to call {}", origReq.getURL());
			throw new IOException(e);
		}
	}

	public DispatcherServlet getDispatcherServlet() {
		return dispatcherServlet;
	}

	/**
	 * Internal implementation of the {@link ServletConfig} interface, to be passed
	 * to the servlet adapter.
	 */
	static public class DelegatingServletConfig implements ServletConfig {

		private final String name;
		private final ServletContext rootServletContext;

		public DelegatingServletConfig(ServletContext servletContext, String name) {
			Assert.hasText(name,
					"[Assertion failed] - this String name must have text; it must not be null, empty, or blank");
			this.name = name;
			this.rootServletContext = servletContext;
		}

		public String getServletName() {
			return name;
		}

		public ServletContext getServletContext() {
			return this.rootServletContext;
		}

		public String getInitParameter(String paramName) {
			return null;
		}

		public Enumeration<String> getInitParameterNames() {
			return Collections.enumeration(Collections.emptySet());
		}
	}

	public class WebscriptRequestWrapper extends HttpServletRequestWrapper {

		private WebScriptServletRequest origReq;

		public WebscriptRequestWrapper(WebScriptServletRequest request) {
			super(request.getHttpServletRequest());
			this.origReq = request;
		}

		@Override
		public String getRequestURI() {
			String uri = super.getRequestURI();
			try {
				uri = URLDecoder.decode(uri, StandardCharsets.UTF_8.toString());
			} catch (UnsupportedEncodingException e) {
				LOGGER.error(e.getMessage(), e);
				return "";
			}

			LOGGER.debug("Processing [{}] {}", super.getMethod(), uri);

			if (uri.contains("$")) {
				uri = uri.replaceAll("\\$", "%24");
			}

			String origUri = origReq.getExtensionPath();
			if (origUri.contains("$")) {
				origUri = origUri.replaceAll("\\$", "%24");
			}

			Pattern pattern = Pattern.compile(
					"(^" + origReq.getServiceContextPath() + "/)" + "(.*)" + "(/" + Pattern.quote(origUri) + ")");
			Matcher matcher = pattern.matcher(uri);
			if (matcher.find()) {
				try {
					String result = matcher.group(EXTENSION_PATH_REGEXP_GROUP_INDEX);
					LOGGER.debug("Found [{}] {} ---> {}", super.getMethod(), uri, result);
					return result;
				} catch (Exception e) {
					// let an empty string be returned
					LOGGER.warn("no such group (3) in regexp while URI evaluation", e);
				}
			}

			return "";
		}

		public String getContextPath() {
			return "";
		}

		public String getServletPath() {
			return "";
		}

		public WebScriptServletRequest getWebScriptServletRequest() {
			return origReq;
		}
	}

	public static enum ServletConfigOptions {
		DISABLED_PARENT_HANDLER_MAPPINGS, DISABLED_PARENT_HANDLER_ADAPTERS, DISABLED_PARENT_VIEW_RESOLVERS,
		DISABLED_PARENT_HANDLER_EXCEPTION_RESOLVERS
	}

	public static class DispatcherWebscriptServlet extends DispatcherServlet {
		private static final long serialVersionUID = -7492692694742840997L;

		private final WebApplicationContext applicationContext;
		private final Class<? extends WebApplicationContext> servletContextClass;
		private final boolean inheritGlobalProperties;

		public DispatcherWebscriptServlet(WebApplicationContext applicationContext, ServletContext rootServletContext,
				String servletName, Class<? extends WebApplicationContext> servletContextClass, Class<?> servletContext,
				boolean inheritGlobalProperties) {
			super(null);
			setContextId(servletName);
			this.applicationContext = applicationContext;
			this.servletContextClass = servletContextClass;
			this.setContextClass(
					servletContextClass != null ? servletContextClass : AnnotationConfigWebApplicationContext.class);
			this.setContextConfigLocation(servletContext.getName());
			this.inheritGlobalProperties = inheritGlobalProperties;

			configureDispatcherServlet();

			try {
				this.init(new DelegatingServletConfig(rootServletContext, servletName));
				LOGGER.info("Alfresco @MVC Dispatcher Webscript: {} has been started", servletName);
			} catch (ServletException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void setEnvironment(Environment environment) {
			ConfigurableEnvironment servletEnv = super.getEnvironment();

			if (environment instanceof ConfigurableEnvironment configurableEnvironment) {
				servletEnv.merge(configurableEnvironment);
			}

			if (inheritGlobalProperties) {
				final Properties globalProperties = (Properties) this.applicationContext.getBean("global-properties");

				servletEnv.merge(new AbstractEnvironment() {
					@Override
					public MutablePropertySources getPropertySources() {
						MutablePropertySources mutablePropertySources = new MutablePropertySources();
						mutablePropertySources
								.addFirst(new PropertiesPropertySource("alfresco-global.properties", globalProperties));
						return mutablePropertySources;
					}
				});
			}

			super.setEnvironment(servletEnv);
		}

		protected void configureDispatcherServlet() {
		}

		public Class<? extends WebApplicationContext> getServletContextClass() {
			return servletContextClass;
		}

		@Override
		protected WebApplicationContext initWebApplicationContext() {
			WebApplicationContext wac = createWebApplicationContext(applicationContext);
			if (wac == null) {
				wac = super.initWebApplicationContext();
			}
			return wac;
		}

		@Override
		protected void postProcessWebApplicationContext(ConfigurableWebApplicationContext wac) {
			wac.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {

				@Override
				public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

					if (!beanFactory.containsLocalBean("dispatcherServlet")) {
						AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder
								.genericBeanDefinition(DispatcherWebscriptServlet.class).getBeanDefinition();
						beanDefinition.setPrimary(true);
						beanDefinition.setBeanClass(DispatcherServlet.class);
						beanDefinition.setInstanceSupplier(() -> DispatcherWebscriptServlet.this);
						((BeanDefinitionRegistry) beanFactory).registerBeanDefinition("dispatcherServlet",
								beanDefinition);
					} else {
						log("dispatcherServlet is already registered. @AlfrescoMVC context should not define a dispatcherServlet bean");
						throw new RuntimeException(
								"dispatcherServlet is already registered. @AlfrescoMVC context should not define a dispatcherServlet bean");
					}
				}
			});
		}

	}

}
