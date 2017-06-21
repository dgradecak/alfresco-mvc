package com.gradecak.alfresco.mvc.data.mapper;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;

import com.gradecak.alfresco.mvc.data.domain.CmDocument;

public class CmDocumentPropertiesMapper extends BeanEntityMapper<CmDocument> {

  private final ServiceRegistry serviceRegistry;

  @Autowired
  public CmDocumentPropertiesMapper(final ServiceRegistry serviceRegistry) {
    super(serviceRegistry);
    this.serviceRegistry = serviceRegistry;
  }

  public QName supportsNodeType() {
    return ContentModel.TYPE_CONTENT;
  }

  @Override
  public void configureMappedProperties(CmDocument document, Map<QName, Serializable> properties) {}

  @Override
  protected void configureMappedObject(CmDocument document) {

    NodeRef nodeRef = document.getId();
    if (nodeRef != null) {

      document.setPreview(buildPreviewUrl(nodeRef, document.getCmName()));
      document.setDownload(buildDownloadUrl(nodeRef, document.getCmName()));
      document.setThumbnail(buildThumbnailUrl(nodeRef, document.getCmName()));

      ContentReader reader = serviceRegistry.getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT);
      if (reader != null) {
        document.setMimetype(reader.getMimetype());
        document.setSize(reader.getSize());
      } else {
        document.setMimetype("application/octet-stream");
        document.setSize(0L);
      }
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
