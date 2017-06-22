package com.gradecak.alfresco.mvc.sample.service;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.sample.domain.CmFolder;
import com.gradecak.alfresco.mvc.sample.mapper.CmFolderPropertiesMapper;
import com.gradecak.alfresco.querytemplate.QueryBuilder;
import com.gradecak.alfresco.querytemplate.QueryTemplate;

@Service
public class QueryTemplateService {

  @Autowired
  private ServiceRegistry serviceRegistry;

  @Autowired
  private Repository repository;
  
  @AlfrescoTransaction(readOnly = true)
  public NodeRef findRootNodeRef() {
    return repository.getCompanyHome();
  }

  @AlfrescoTransaction(readOnly = true)
  public CmFolder getCompanyHomeFolder() {
    NodeRef findRootNodeRef = findRootNodeRef();
    return new QueryTemplate(serviceRegistry).queryForObject(findRootNodeRef, new CmFolderPropertiesMapper(serviceRegistry));
  }

  @AlfrescoTransaction(readOnly = true)
  public Page<CmFolder> search() {
    return new QueryTemplate(serviceRegistry).queryForList(new QueryBuilder().type(ContentModel.TYPE_FOLDER), new CmFolderPropertiesMapper(serviceRegistry));
  }

}
