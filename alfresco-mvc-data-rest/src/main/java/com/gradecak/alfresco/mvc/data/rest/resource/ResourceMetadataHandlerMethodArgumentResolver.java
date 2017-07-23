package com.gradecak.alfresco.mvc.data.rest.resource;

import static org.springframework.util.ClassUtils.isAssignable;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.trimTrailingCharacter;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UrlPathHelper;

/**
 * {@link HandlerMethodArgumentResolver} to create {@link ResourceMetadata} instances.
 */
public class ResourceMetadataHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
  private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

  private final URI baseUri;

  public ResourceMetadataHandlerMethodArgumentResolver(URI uri) {

    Assert.notNull(uri, "Base URI must not be null!");

    String uriString = uri.toString();
    this.baseUri = URI.create(trimTrailingCharacter(trimTrailingCharacter(uriString, '/'), '/'));
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return isAssignable(parameter.getParameterType(), RepositoryInformation.class);
  }

  @Override
  public String resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

    String lookupPath = URL_PATH_HELPER.getLookupPathForRequest(webRequest.getNativeRequest(HttpServletRequest.class));
    String domainLookupPath = getDomainLookupPath(lookupPath);
    String domainKey = findMappingVariable("repository", parameter, domainLookupPath);

    if (!hasText(domainKey)) {
      return null;
    }
    
    return domainKey;
  }

  public String getDomainLookupPath(String lookupPath) {

    Assert.notNull(lookupPath, "Lookup path must not be null!");

    lookupPath = lookupPath.contains("{") ? lookupPath.substring(0, lookupPath.indexOf('{')) : lookupPath;
    lookupPath = trimTrailingCharacter(lookupPath, '/');

    if (!baseUri.isAbsolute()) {

      String uri = baseUri.toString();

      if (!StringUtils.hasText(uri)) {
        return lookupPath;
      }

      uri = uri.startsWith("/") ? uri : "/".concat(uri);
      return lookupPath.startsWith(uri) ? lookupPath.substring(uri.length(), lookupPath.length()) : null;
    }

    List<String> baseUriSegments = UriComponentsBuilder.fromUri(baseUri).build().getPathSegments();
    Collections.reverse(baseUriSegments);
    String tail = "";

    for (String tailSegment : baseUriSegments) {

      tail = "/".concat(tailSegment).concat(tail);

      if (lookupPath.startsWith(tail)) {
        return lookupPath.substring(tail.length(), lookupPath.length());
      }
    }

    return null;
  }
  
  public static String findMappingVariable(String variable, MethodParameter parameter, String lookupPath) {

    Assert.hasText(variable, "Variable name must not be null or empty!");
    Assert.notNull(parameter, "Method parameter must not be null!");

    RequestMapping annotation = parameter.getMethodAnnotation(RequestMapping.class);

    for (String mapping : annotation.value()) {

        Map<String, String> variables = new org.springframework.web.util.UriTemplate(mapping).match(lookupPath);
        String value = variables.get(variable);

        if (value != null) {
            return value;
        }
    }

    return null;
}
}
