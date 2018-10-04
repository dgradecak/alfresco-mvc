package com.gradecak.alfresco.mvc.rest.config;

import org.alfresco.rest.framework.resource.parameters.Params;
import org.springframework.core.MethodParameter;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.gradecak.alfresco.mvc.rest.AlfrescoApiResponseInterceptor;
import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript.WebscriptRequestWrapper;

public class ParamsHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return Params.class.equals(parameter.getParameterType());
  }

  @Override
  public Params resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
    Object nativeRequest = webRequest.getNativeRequest();
    if (nativeRequest instanceof WebscriptRequestWrapper) {
      WebScriptServletRequest webScriptServletRequest = ((WebscriptRequestWrapper) nativeRequest).getWebScriptServletRequest();
      return AlfrescoApiResponseInterceptor.getDefaultParameters(webScriptServletRequest);
    }
    return AlfrescoApiResponseInterceptor.getDefaultParameters(null);
  }
}
