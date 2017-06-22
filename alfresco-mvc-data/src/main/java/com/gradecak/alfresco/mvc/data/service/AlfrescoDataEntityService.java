package com.gradecak.alfresco.mvc.data.service;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.thumbnail.CreateThumbnailActionExecuter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Persistable;
import org.springframework.util.Assert;

import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.data.annotation.AlfrescoNodeCreator;
import com.gradecak.alfresco.querytemplate.NodePropertiesMapper;
import com.gradecak.alfresco.querytemplate.QueryBuilder;
import com.gradecak.alfresco.querytemplate.QueryTemplate;

// TODO think about tx management, not sure it is needed. related to com.gradecak.alfresco.mvc.data.support.AlfrescoRepositoryConfigExtension.registerBeansForRoot
public class AlfrescoDataEntityService {

  private ServiceRegistry serviceRegistry;
  private QueryTemplate queryTemplate;

  public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
    this.queryTemplate = new QueryTemplate(serviceRegistry);
  }

  @AlfrescoTransaction(readOnly = true)
  public <T extends Persistable<NodeRef>> T get(NodePropertiesMapper<T> nodeMapper, final NodeRef nodeRef) {
    return queryTemplate.queryForObject(nodeRef, nodeMapper);
  }

  @AlfrescoTransaction(readOnly = true)
  public boolean exists(final NodeRef nodeRef) {
    return serviceRegistry.getNodeService().exists(nodeRef);
  }

  @AlfrescoTransaction
  public void delete(final NodeRef nodeRef) {
    serviceRegistry.getNodeService().deleteNode(nodeRef);
  }

  @AlfrescoTransaction
  public <T extends Persistable<NodeRef>> NodeRef update(NodePropertiesMapper<T> nodeMapper, final NodeRef nodeRef, Map<QName, Serializable> properties) {
    serviceRegistry.getNodeService().addProperties(nodeRef, properties);
    return nodeRef;
  }

  @AlfrescoTransaction
  public <T> NodeRef create(AlfrescoNodeCreator<T> creator, final T entity, Map<QName, Serializable> properties) {
    NodeRef nodeRef = creator.create(entity, properties);

    ActionService actionService = serviceRegistry.getActionService();

    // doclib
    Action doclibAction = serviceRegistry.getActionService().createAction(CreateThumbnailActionExecuter.NAME);
    doclibAction.setExecuteAsynchronously(true);
    doclibAction.setParameterValue(CreateThumbnailActionExecuter.PARAM_THUMBANIL_NAME, "doclib");
    actionService.executeAction(doclibAction, nodeRef, true, true);

    return nodeRef;
  }

  @AlfrescoTransaction
  public NodeRef create(final NodeRef parentRef, Map<QName, Serializable> properties, QName type, InputStream is) {
    Assert.notNull(type);

    ChildAssociationRef node = serviceRegistry.getNodeService().createNode(parentRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, type, properties);
    NodeRef nodeRef = node.getChildRef();

    if (is != null) {
      ContentWriter writer = serviceRegistry.getContentService().getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
      writer.setMimetype(serviceRegistry.getMimetypeService().guessMimetype((String) properties.get(ContentModel.PROP_NAME)));
      writer.putContent(is);
    }

    ActionService actionService = serviceRegistry.getActionService();

    // doclib
    Action doclibAction = serviceRegistry.getActionService().createAction(CreateThumbnailActionExecuter.NAME);
    doclibAction.setExecuteAsynchronously(true);
    doclibAction.setParameterValue(CreateThumbnailActionExecuter.PARAM_THUMBANIL_NAME, "doclib");
    actionService.executeAction(doclibAction, nodeRef, true, true);

    return nodeRef;
  }

  @AlfrescoTransaction(readOnly = true)
  public <T extends Persistable<NodeRef>> Page<T> search(NodePropertiesMapper<T> mapper, final QueryBuilder query) {

    Page<T> documentList = queryTemplate.queryForList(query.build(), mapper, 10, 0, 10, StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, query.getLanguage());

    return documentList;
  }
}
