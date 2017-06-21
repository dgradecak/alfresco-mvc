package com.gradecak.alfresco.mvc.sample.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gradecak.alfresco.mvc.annotation.EnableAlfrescoMvcProxy;
import com.gradecak.alfresco.mvc.data.config.AlfrescoMvcDataConfig;
import com.gradecak.alfresco.mvc.sample.service.QueryTemplateService;
import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript;

@Configuration
@EnableAlfrescoMvcProxy(basePackageClasses=QueryTemplateService.class)
public class AlfrescoMvcQueryTemplateModuleConfig extends AlfrescoMvcDataConfig{

  @Bean(name = { "webscript.alfresco-mvc.mvc.post", "webscript.alfresco-mvc.mvc.get", "webscript.alfresco-mvc.mvc.delete", "webscript.alfresco-mvc.mvc.put" })
  public DispatcherWebscript dispatcherWebscript() {
    DispatcherWebscript dispatcherWebscript = new DispatcherWebscript();
    dispatcherWebscript.setContextClass(org.springframework.web.context.support.AnnotationConfigWebApplicationContext.class);
    dispatcherWebscript.setContextConfigLocation(AlfrescoMvcQueryTemplateServletContext.class.getName());
    return dispatcherWebscript;
  }
  
  @Bean
  public QueryTemplateService queryTemplateService() {
    System.out.println();
    return new QueryTemplateService();
  }
}
