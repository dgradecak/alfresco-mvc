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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.WrappingWebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.util.JavaScriptUtils;
import org.springframework.web.util.NestedServletException;

import com.gradecak.alfresco.mvc.ResponseMapBuilder;

public class DispatcherWebscript extends AbstractWebScript implements ServletContextAware, ApplicationContextAware, InitializingBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherWebscript.class);

  private DispatcherServlet s;
  private String contextConfigLocation;
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
    try {
      s.service(wrapper, sr);
    } catch (Throwable e) {
      convertExceptionToJson(e, sr);
    }

  }

  private void convertExceptionToJson(Throwable ex, HttpServletResponse res) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    ResponseMapBuilder builder = ResponseMapBuilder.createFailResponseMap().
    withEntry("event", "exception").
    withEntry("exception", ex.getClass()).
    withEntry("message", JavaScriptUtils.javaScriptEscape(ex.getMessage()));

    if (ex instanceof NestedServletException) {
      NestedServletException nestedServletException = (NestedServletException) ex;
      if (nestedServletException.getCause() != null) {
        builder.withEntry("cause", nestedServletException.getCause().getClass());
        builder.withEntry("causeMessage", nestedServletException.getCause().getMessage());
      }
    }

    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);

    objectMapper.writeValue(res.getOutputStream(), builder.build());
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

    s.setContextConfigLocation(contextConfigLocation);
    s.init(new DelegatingServletConfig());
  }

  public void setContextConfigLocation(String contextConfigLocation) {
    this.contextConfigLocation = contextConfigLocation;
  }

  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public void setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  /**
   * Internal implementation of the {@link ServletConfig} interface, to be passed to the servlet adapter.
   */
  private class DelegatingServletConfig implements ServletConfig {

    public String getServletName() {
      return "dispatcherWebscript";
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
