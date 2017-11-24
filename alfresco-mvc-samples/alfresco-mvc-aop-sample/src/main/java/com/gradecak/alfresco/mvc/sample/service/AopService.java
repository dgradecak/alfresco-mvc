package com.gradecak.alfresco.mvc.sample.service;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;

@Service
public class AopService {

	@Autowired
	private Repository repository;

	@AlfrescoTransaction(readOnly = true)
	public NodeRef findRootNodeRef() {
		return repository.getCompanyHome();
	}

}
