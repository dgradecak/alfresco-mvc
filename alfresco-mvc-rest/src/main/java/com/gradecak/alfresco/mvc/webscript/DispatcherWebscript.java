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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.WrappingWebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;
import org.springframework.util.Assert;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class DispatcherWebscript extends AbstractWebScript
		implements ApplicationListener<ContextRefreshedEvent>, ServletContextAware, ApplicationContextAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherWebscript.class);
	private static final int EXTENSION_PATH_REGEXP_GROUP_INDEX = 3;

	protected DispatcherServlet s;
	private String contextConfigLocation;
	private Class<?> contextClass;
	private ApplicationContext applicationContext;
	private ServletContext servletContext;

	private final EnumSet<ServletConfigOptions> servletConfigOptions = EnumSet.noneOf(ServletConfigOptions.class);
	private final String servletName;
	private final boolean inheritGlobalProperties;

	public DispatcherWebscript() {
		this("alfresco-mvc.mvc", false);
	}

	public DispatcherWebscript(final String servletName) {
		this(servletName, false);
	}

	public DispatcherWebscript(final String servletName, boolean inheritGlobalProperties) {
		Assert.hasText(servletName,
				"[Assertion failed] - this String servletName must have text; it must not be null, empty, or blank");
		this.servletName = servletName;
		this.inheritGlobalProperties = inheritGlobalProperties;
	}

	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		final WebScriptServletRequest origReq = (WebScriptServletRequest) req;

		WebScriptServletResponse wsr = null;
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
			s.service(wrapper, sr);

		} catch (Throwable e) {
			throw new IOException(e);
		}
	}

	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext refreshContext = event.getApplicationContext();
		if (refreshContext != null && refreshContext.equals(applicationContext)) {

			s = new DispatcherWebscriptServlet((WebApplicationContext) applicationContext, this, servletName);

			if (!servletConfigOptions.isEmpty()) {
				s.setDetectAllHandlerMappings(
						!servletConfigOptions.contains(ServletConfigOptions.DISABLED_PARENT_HANDLER_MAPPINGS));
				s.setDetectAllHandlerAdapters(
						!servletConfigOptions.contains(ServletConfigOptions.DISABLED_PARENT_HANDLER_ADAPTERS));
				s.setDetectAllViewResolvers(
						!servletConfigOptions.contains(ServletConfigOptions.DISABLED_PARENT_VIEW_RESOLVERS));
				s.setDetectAllHandlerExceptionResolvers(!servletConfigOptions
						.contains(ServletConfigOptions.DISABLED_PARENT_HANDLER_EXCEPTION_RESOLVERS));
			}

			s.setContextClass(contextClass != null ? contextClass : AnnotationConfigWebApplicationContext.class);
			s.setContextConfigLocation(contextConfigLocation);
			configureDispatcherServlet(s);

			try {
				s.init(new DelegatingServletConfig(servletName));
				LOGGER.info("Alfresco @MVC Dispatcher Webscript: {} has been started", servletName);
			} catch (ServletException e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected void configureDispatcherServlet(DispatcherServlet dispatcherServlet) {
		if (inheritGlobalProperties) {
			final Properties globalProperties = (Properties) this.applicationContext.getBean("global-properties");

			ConfigurableEnvironment servletEnv = dispatcherServlet.getEnvironment();
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
	}

	public DispatcherServlet getDispatcherServlet() {
		return s;
	}

	public String getContextConfigLocation() {
		return contextConfigLocation;
	}

	public void setContextConfigLocation(String contextConfigLocation) {
		this.contextConfigLocation = contextConfigLocation;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public void setContextClass(Class<?> contextClass) {
		this.contextClass = contextClass;
	}

	public Class<?> getContextClass() {
		return this.contextClass;
	}

	public void addServletConfigOptions(ServletConfigOptions[] detectServletConfig) {
		if (detectServletConfig != null) {
			this.servletConfigOptions.addAll(Arrays.asList(detectServletConfig));
		}
	}

	public ServletConfigOptions[] getServletConfigOptions() {
		return servletConfigOptions.toArray(new ServletConfigOptions[0]);
	}

	/**
	 * Internal implementation of the {@link ServletConfig} interface, to be passed
	 * to the servlet adapter.
	 */
	public class DelegatingServletConfig implements ServletConfig {

		final private String name;

		public DelegatingServletConfig(final String name) {
			Assert.hasText(name,
					"[Assertion failed] - this String name must have text; it must not be null, empty, or blank");
			this.name = name;
		}

		public String getServletName() {
			return name;
		}

		public ServletContext getServletContext() {
			return DispatcherWebscript.this.servletContext;
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
			if (uri.contains("$")) {
				uri = uri.replaceAll("\\$", "%24");
			}

			String origUri = origReq.getExtensionPath();
			if (origUri.contains("$")) {
				origUri = origUri.replaceAll("\\$", "%24");
			}

			Pattern pattern = Pattern.compile("(^" + origReq.getServiceContextPath() + "/)(.*)(/" + origUri + ")");
			Matcher matcher = pattern.matcher(uri);

			if (matcher.find()) {
				try {
					return matcher.group(EXTENSION_PATH_REGEXP_GROUP_INDEX);
				} catch (Exception e) {
					// let an empty string be returned
					LOGGER.warn("no such group (3) in regexp while URI evaluation", e);
				}
			}

			return "";
		}

		public String getContextPath() {
			return origReq.getContextPath();
		}

		public String getServletPath() {
			return "";
		}

		@Override
		public String getPathInfo() {
			return super.getPathInfo();
			// return "/s/mvc/swagger-ui/index.html";
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
		private final DispatcherWebscript dispatcherWebscript;

		public DispatcherWebscriptServlet(WebApplicationContext applicationContext,
				DispatcherWebscript dispatcherWebscript, String servletName) {
			super(applicationContext);
			setContextId(servletName);
			this.applicationContext = applicationContext;
			this.dispatcherWebscript = dispatcherWebscript;
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

					if (beanFactory.containsBean("dispatcherServlet")) {
						BeanDefinition beanDefinition = (BeanDefinition) beanFactory
								.getBeanDefinition("dispatcherServlet");
						if (!(beanDefinition instanceof AbstractBeanDefinition)) {
							throw new RuntimeException(
									"Webscript dispatcherServlet has not been configured. Make sure to @Import(com.gradecak.alfresco.mvc.rest.config.AlfrescoRestServletRegistrar.class)");
						}
						Class<?> beanClass = ((AbstractBeanDefinition) beanDefinition).getBeanClass();
						if (!(beanClass.isAssignableFrom(DispatcherWebscriptServlet.class))) {
							throw new RuntimeException(
									"Webscript dispatcherServlet has not been configured. Make sure to @Import(com.gradecak.alfresco.mvc.rest.config.AlfrescoRestServletRegistrar.class)");
						}
						Supplier<?> supplier = ((AbstractBeanDefinition) beanDefinition).getInstanceSupplier();
						if (supplier != null) {
							Object object = supplier.get();
							if (!(object instanceof DispatcherWebscriptServlet)) {
								throw new RuntimeException(
										"Webscript dispatcherServlet has not been configured. Make sure to @Import(com.gradecak.alfresco.mvc.rest.config.AlfrescoRestServletRegistrar.class)");
							}
						} else {
							((AbstractBeanDefinition) beanDefinition)
									.setInstanceSupplier(() -> DispatcherWebscriptServlet.this);
						}

					} else {
						AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder
								.genericBeanDefinition(DispatcherWebscriptServlet.class).getBeanDefinition();
						beanDefinition.setPrimary(true);
						beanDefinition.setInstanceSupplier(() -> DispatcherWebscriptServlet.this);
						((BeanDefinitionRegistry) beanFactory).registerBeanDefinition("dispatcherServlet",
								beanDefinition);
					}
				}
			});
		}

		public DispatcherWebscript getDispatcherWebscript() {
			return this.dispatcherWebscript;
		}
	}

}
