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
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.WrappingWebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.util.JavaScriptUtils;
import org.springframework.web.util.NestedServletException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecak.alfresco.mvc.LocalHttpServletResponse;
import com.gradecak.alfresco.mvc.ResponseMapBuilder;

public class DispatcherWebscript extends AbstractWebScript implements ServletContextAware, ApplicationContextAware, InitializingBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherWebscript.class);

  protected DispatcherServlet s;  
  private String contextConfigLocation;
  private Class<?> contextClass;
  private ApplicationContext applicationContext;
  private ServletContext servletContext;

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
    LocalHttpServletResponse mockHttpServletResponse = new LocalHttpServletResponse();
    try {
      s.service(wrapper, mockHttpServletResponse);

      writeResponseToWebscript(wsr, mockHttpServletResponse);
    } catch (Throwable e) {
      convertExceptionToJson(e, wsr, sr, mockHttpServletResponse);
    }
  }

  private void writeResponseToWebscript(WebScriptServletResponse wsr, LocalHttpServletResponse mockHttpServletResponse) throws UnsupportedEncodingException, IOException {
    String contentAsString = mockHttpServletResponse.getContentAsString();

    Collection<String> headerNames = mockHttpServletResponse.getHeaderNames();
    for (String header : headerNames) {
      wsr.setHeader(header, mockHttpServletResponse.getHeader(header));
    }

    wsr.setStatus(mockHttpServletResponse.getStatus());
    wsr.setContentType(mockHttpServletResponse.getContentType());

    if (StringUtils.hasText(mockHttpServletResponse.getErrorMessage())) {
      wsr.getHttpServletResponse().sendError(mockHttpServletResponse.getStatus(), mockHttpServletResponse.getErrorMessage());
    } else if (StringUtils.hasText(contentAsString)) {
      wsr.getWriter().write(contentAsString);
    }
  }

  private void convertExceptionToJson(Throwable ex, WebScriptServletResponse wsr, final HttpServletResponse sr, LocalHttpServletResponse mockHttpServletResponse) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    ResponseMapBuilder builder = ResponseMapBuilder.createFailResponseMap().withEntry("event", "exception").withEntry("exception", ex.getClass()).withEntry("message",
        JavaScriptUtils.javaScriptEscape(ex.getMessage()));

    int status = mockHttpServletResponse.getStatus();
    if (HttpServletResponse.SC_OK == status) {
      status = HttpServletResponse.SC_BAD_REQUEST;
    }

    // String errorMessage = ex.getLocalizedMessage();
    if (ex instanceof NestedServletException) {
      NestedServletException nestedServletException = (NestedServletException) ex;
      if (nestedServletException.getCause() != null) {
        builder.withEntry("cause", nestedServletException.getCause().getClass());
        builder.withEntry("causeMessage", nestedServletException.getCause().getMessage());
        if (nestedServletException.getCause() instanceof DataAccessException) {
          if (HttpServletResponse.SC_OK == mockHttpServletResponse.getStatus()) {
            status = HttpServletResponse.SC_NOT_ACCEPTABLE;
          }
        }
      }
    }

    // mockHttpServletResponse.sendError(status, errorMessage);
    mockHttpServletResponse.setStatus(status);
    mockHttpServletResponse.setContentType("application/json");
    objectMapper.writeValue(mockHttpServletResponse.getOutputStream(), builder.build());
    writeResponseToWebscript(wsr, mockHttpServletResponse);
  }

  public void afterPropertiesSet() throws Exception {

    s = new DispatcherServlet() {

      private static final long serialVersionUID = -7492692694742840997L;

      @Override
      protected WebApplicationContext initWebApplicationContext() {
        WebApplicationContext wac = createWebApplicationContext(applicationContext);
        if (wac == null) {
          wac = super.initWebApplicationContext();
        }
        return wac;
      }

    };

    if(contextClass != null) {
      s.setContextClass(contextClass);
    }
    s.setContextConfigLocation(contextConfigLocation);
    configureDispatcherServlet(s);

    s.init(new DelegatingServletConfig());
  }

  public void configureDispatcherServlet(DispatcherServlet dispatcherServlet) {}

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

  /**
   * Internal implementation of the {@link ServletConfig} interface, to be passed to the servlet adapter.
   */
  public class DelegatingServletConfig implements ServletConfig {

    public String getServletName() {
      return "Alfresco @MVC Dispatcher Webscript";
    }

    public ServletContext getServletContext() {
      return DispatcherWebscript.this.servletContext;
    }

    public String getInitParameter(String paramName) {
      return null;
    }

    public Enumeration<String> getInitParameterNames() {
      return Collections.enumeration(new HashSet<String>());
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
      Pattern pattern = Pattern.compile("(^" + origReq.getServiceContextPath() + "/)(.*)(/" + origReq.getExtensionPath() + ")");
      Matcher matcher = pattern.matcher(uri);

      final int extensionPathRegexpGroupIndex = 3;
      if (matcher.find()) {
        try {
          return matcher.group(extensionPathRegexpGroupIndex);
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

    public WebScriptServletRequest getWebScriptServletRequest() {
      return origReq;
    }
  }

}
