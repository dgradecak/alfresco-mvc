package com.gradecak.alfresco.mvc.sample.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gradecak.alfresco.mvc.annotation.EnableAlfrescoMvcProxy;
import com.gradecak.alfresco.mvc.data.annotation.EnableAlfrescoRepositories;
import com.gradecak.alfresco.mvc.data.config.AlfrescoMvcDataConfig;
import com.gradecak.alfresco.mvc.data.repository.CmDocumentRepository;
import com.gradecak.alfresco.mvc.sample.service.AlfrescoDataService;
import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript;

@Configuration
@EnableAlfrescoMvcProxy(basePackageClasses=AlfrescoDataService.class)
@EnableAlfrescoRepositories(basePackageClasses = CmDocumentRepository.class)
public class AlfrescoMvcDataModuleConfig extends AlfrescoMvcDataConfig{

  @Bean(name = { "webscript.alfresco-mvc.mvc.post", "webscript.alfresco-mvc.mvc.get", "webscript.alfresco-mvc.mvc.delete", "webscript.alfresco-mvc.mvc.put" })
  public DispatcherWebscript dispatcherWebscript() {
    DispatcherWebscript dispatcherWebscript = new DispatcherWebscript();
    dispatcherWebscript.setContextClass(org.springframework.web.context.support.AnnotationConfigWebApplicationContext.class);
    dispatcherWebscript.setContextConfigLocation(AlfrescoMvcDataServletContext.class.getName());
    return dispatcherWebscript;
  }
  
  @Bean
  public AlfrescoDataService alfrescoDataService() {
    return new AlfrescoDataService();
  }
}
