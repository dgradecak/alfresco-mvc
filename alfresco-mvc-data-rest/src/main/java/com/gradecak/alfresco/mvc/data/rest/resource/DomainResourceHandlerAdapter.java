package com.gradecak.alfresco.mvc.data.rest.resource;

import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

public class DomainResourceHandlerAdapter extends RequestMappingHandlerAdapter {

  private final List<HandlerMethodArgumentResolver> argumentResolvers;

  /**
   * Creates a new {@link DomainResourceHandlerAdapter} using the given {@link HandlerMethodArgumentResolver} and
   * {@link ResourceProcessor}s.
   * 
   * @param argumentResolvers must not be {@literal null}.
   * @param resourceProcessors must not be {@literal null}.
   */
  public DomainResourceHandlerAdapter(List<HandlerMethodArgumentResolver> argumentResolvers, List<ResourceProcessor<?>> resourceProcessors) {

    // super(resourceProcessors);
    this.argumentResolvers = argumentResolvers;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.rest.webmvc.ResourceProcessorInvokingHandlerAdapter#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() {
    setCustomArgumentResolvers(argumentResolvers);
    super.afterPropertiesSet();
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter#getOrder()
   */
  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#supportsInternal(org.
   * springframework.web.method.HandlerMethod)
   */
  @Override
  protected boolean supportsInternal(HandlerMethod handlerMethod) {

    Class<?> controllerType = handlerMethod.getBeanType();

    // return AnnotationUtils.findAnnotation(controllerType, RepositoryRestController.class) != null;
    return true;
  }
}
