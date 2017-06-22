package com.gradecak.alfresco.mvc.sample.service;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;

import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.data.repository.CmFolderRepository;
import com.gradecak.alfresco.mvc.sample.domain.CmFolder;

public class AlfrescoDataService {

  @Autowired
  private Repository repository;

  @Autowired
  private CmFolderRepository cmRepository;

  @AlfrescoTransaction(readOnly = true)
  public NodeRef findRootNodeRef() {
    return repository.getCompanyHome();
  }

  @AlfrescoTransaction(readOnly = true)
  public CmFolder getCompanyHomeFolder() {
    NodeRef rootNodeRef = findRootNodeRef();
    return cmRepository.findOne(rootNodeRef);
  }
}
