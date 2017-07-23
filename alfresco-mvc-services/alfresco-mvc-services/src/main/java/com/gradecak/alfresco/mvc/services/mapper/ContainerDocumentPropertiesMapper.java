package com.gradecak.alfresco.mvc.services.mapper;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.gradecak.alfresco.mvc.data.util.RepositoriesUtils;
import com.gradecak.alfresco.mvc.services.domain.ContainerDocument;
import com.gradecak.alfresco.mvc.services.model.IbappModel;
import com.gradecak.alfresco.mvc.services.service.TenantWorkflowService;

public class ContainerDocumentPropertiesMapper extends AbstractDocumentPropertiesMapper<ContainerDocument> {

  private final ServiceRegistry serviceRegistry;

  @Autowired
  public ContainerDocumentPropertiesMapper(final ServiceRegistry serviceRegistry) {
    super(serviceRegistry);
    this.serviceRegistry = serviceRegistry;
    setMappedClass(ContainerDocument.class);
  }

  public QName supportsNodeType() {
    return IbappModel.TYPE_DOCUMENT;
  }

  @Override
  protected void configureMappedObject(ContainerDocument document) {
    super.configureMappedObject(document);

    NodeRef nodeRef = document.getId();

    String entity = (String) serviceRegistry.getNodeService().getProperty(nodeRef, IbappModel.PROP_ENTITY);
    String primaryKey = (String) serviceRegistry.getNodeService().getProperty(nodeRef, IbappModel.PROP_PRIMARY_KEY);

    if (StringUtils.hasText(entity) && StringUtils.hasText(primaryKey)) {
      ImmutableMap<String, String> related = ImmutableMap.of(TenantWorkflowService.IBAPP_ENTITY_FIELD, entity, TenantWorkflowService.IBAPP_PRIMARYKEY_FIELD, primaryKey);
      document.setRelated(related);
    }

    document.setNodeType(RepositoriesUtils.getDefaultPathFor(mappedClass));
  }
}
