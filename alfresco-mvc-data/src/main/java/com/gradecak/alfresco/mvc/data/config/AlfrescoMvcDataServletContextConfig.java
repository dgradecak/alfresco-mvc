package com.gradecak.alfresco.mvc.data.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import com.gradecak.alfresco.mvc.config.AlfrescoMvcServletContextConfig;

@Configuration
public class AlfrescoMvcDataServletContextConfig extends AlfrescoMvcServletContextConfig {

  @Bean
  public PageableHandlerMethodArgumentResolver pageableResolver() {
    return new PageableHandlerMethodArgumentResolver(sortResolver());
  }

  @Bean
  public SortHandlerMethodArgumentResolver sortResolver() {
    return new SortHandlerMethodArgumentResolver();
  }

  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(sortResolver());
    argumentResolvers.add(pageableResolver());
  }
}
