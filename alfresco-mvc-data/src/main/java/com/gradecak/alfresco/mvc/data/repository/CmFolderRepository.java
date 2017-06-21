package com.gradecak.alfresco.mvc.data.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gradecak.alfresco.mvc.data.domain.CmFolder;
import com.gradecak.alfresco.mvc.data.support.AlfrescoNodeRepository;

@Repository
@Transactional
public interface CmFolderRepository extends AlfrescoNodeRepository<CmFolder> {}
