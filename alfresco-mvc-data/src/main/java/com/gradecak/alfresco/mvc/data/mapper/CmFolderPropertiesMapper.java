package com.gradecak.alfresco.mvc.data.mapper;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.gradecak.alfresco.mvc.data.domain.CmFolder;
import com.gradecak.alfresco.mvc.data.util.RepositoriesUtils;

@Component
public class CmFolderPropertiesMapper extends BeanEntityMapper<CmFolder> {

  private final ServiceRegistry serviceRegistry;

  @Autowired
  public CmFolderPropertiesMapper(final ServiceRegistry serviceRegistry) {
    super(serviceRegistry);
    this.serviceRegistry = serviceRegistry;
  }

  public QName supportsNodeType() {
    return ContentModel.TYPE_FOLDER;
  }

  @Override
  public void configureMappedProperties(CmFolder folder, Map<QName, Serializable> properties) {}

  @Override
  protected void configureMappedObject(CmFolder folder) {}
}
