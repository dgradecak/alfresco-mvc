package com.gradecak.alfresco.mvc.services.mapper;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.tika.mime.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.gradecak.alfresco.mvc.data.mapper.BeanEntityMapper;
import com.gradecak.alfresco.mvc.services.domain.ContainerDocument;

public abstract class AbstractDocumentPropertiesMapper<T extends ContainerDocument> extends BeanEntityMapper<T> {

  private final ServiceRegistry serviceRegistry;

  @Autowired
  public AbstractDocumentPropertiesMapper(final ServiceRegistry serviceRegistry) {
    super(serviceRegistry);
    this.serviceRegistry = serviceRegistry;
  }

  @Override
  public void configureMappedProperties(T document, Map<QName, Serializable> properties) {}

  @Override
  protected void configureMappedObject(T document) {

    NodeRef nodeRef = document.getId();

    document.setPreview(buildPreviewUrl(nodeRef, document.getCmName()));
    document.setDownload(buildDownloadUrl(nodeRef, document.getCmName()));
    document.setThumbnail(buildThumbnailUrl(nodeRef, document.getCmName()));

    Builder<String, Boolean> builder = ImmutableMap.builder();
    builder.put("delete", AccessStatus.ALLOWED.equals(serviceRegistry.getPermissionService().hasPermission(nodeRef, PermissionService.DELETE)));
    builder.put("edit", AccessStatus.ALLOWED.equals(serviceRegistry.getPermissionService().hasPermission(nodeRef, PermissionService.WRITE)));

    document.setPermissions(builder.build());

    ContentReader reader = serviceRegistry.getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT);
    if (reader != null) {
      document.setMimetype(reader.getMimetype());
      document.setSize(reader.getSize());
    } else {
      document.setMimetype("application/octet-stream");
      document.setSize(0L);
    }
  }

  private String buildDownloadUrl(NodeRef nodeRef, String name) {
    StoreRef storeRef = nodeRef.getStoreRef();

    String protocol = storeRef.getProtocol();
    if (Version2Model.STORE_PROTOCOL.equals(storeRef.getProtocol())) {
      protocol = StoreRef.PROTOCOL_WORKSPACE;
    }

    StringBuilder builder = new StringBuilder("/api/node/content/").append(protocol).append("/").append(storeRef.getIdentifier()).append("/").append(nodeRef.getId()).append("/").append(name);
    return builder.toString();
  }

  private String buildPreviewUrl(NodeRef nodeRef, String name) {
    StoreRef storeRef = nodeRef.getStoreRef();

    // new NodeRef(StoreRef.PROTOCOL_WORKSPACE, Version2Model.STORE_ID, version.getFrozenStateNodeRef().getId())

    String protocol = storeRef.getProtocol();
    if (Version2Model.STORE_PROTOCOL.equals(storeRef.getProtocol())) {
      protocol = StoreRef.PROTOCOL_WORKSPACE;
    }

    StringBuilder builder = new StringBuilder("/api/node/").append(protocol).append("/").append(storeRef.getIdentifier()).append("/").append(nodeRef.getId()).append("/content");

    String guessMimetype = serviceRegistry.getMimetypeService().guessMimetype(name);
    if (MimetypeMap.MIMETYPE_PDF.equals(guessMimetype)) {
      builder.append("/").append(name);
    } else {
      builder.append("/thumbnails/");
    }

    return builder.toString();
  }

  private String buildThumbnailUrl(NodeRef nodeRef, String name) {
    StoreRef storeRef = nodeRef.getStoreRef();

    String protocol = storeRef.getProtocol();
    if (Version2Model.STORE_PROTOCOL.equals(storeRef.getProtocol())) {
      protocol = StoreRef.PROTOCOL_WORKSPACE;
    }

    StringBuilder builder = new StringBuilder("/api/node/").append(protocol).append("/").append(storeRef.getIdentifier()).append("/").append(nodeRef.getId()).append("/content/thumbnails/doclib");

    return builder.toString();
  }
}
