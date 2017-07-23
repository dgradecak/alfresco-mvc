package com.gradecak.alfresco.mvc.data.rest.resource;

import java.lang.reflect.Field;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.util.ReflectionUtils;

import com.gradecak.alfresco.mvc.data.rest.service.AlfrescoMvcCannedQueryService;
import com.gradecak.alfresco.mvc.data.support.AlfrescoNodeRepository;

public class AlfrescoEntityInvoker {

  private final AlfrescoNodeRepository<Persistable<NodeRef>> repository;
  private final AlfrescoMvcCannedQueryService cannedQueryService;
  private final ServiceRegistry serviceRegistry;

  public AlfrescoEntityInvoker(AlfrescoNodeRepository<Persistable<NodeRef>> repository, AlfrescoMvcCannedQueryService cannedQueryService, ServiceRegistry serviceRegistry) {
    this.repository = repository;
    this.cannedQueryService = cannedQueryService;
    this.serviceRegistry = serviceRegistry;
  }

  public Persistable<NodeRef> get(RootResourceInformation resourceInformation, final NodeRef nodeRef) {
    return repository.findOne(nodeRef);
  }

//  public List<CmFolder> breadcrumb(RootResourceInformation resourceInformation, final NodeRef nodeRef) {
//    Path path = serviceRegistry.getNodeService().getPath(nodeRef);
//    List<CmFolder> parents = new ArrayList<>();
//    for (Path.Element element : path) {
//      if (element instanceof ChildAssocElement) {
//        NodeRef parentRef = ((ChildAssocElement) element).getRef().getParentRef();
//        if (parentRef != null) {
//          QName type = serviceRegistry.getNodeService().getType(parentRef);
//          if (serviceRegistry.getDictionaryService().isSubClass(type, ContentModel.TYPE_FOLDER)) {
//            CmFolder folder = queryTemplate.queryForObject(parentRef, new CmFolderPropertiesMapper(serviceRegistry));
//            parents.add(folder);
//          }
//        }
//      }
//    }
//    return parents;
//  }

  public Page<Persistable<NodeRef>> list(RootResourceInformation resourceInformation, final NodeRef nodeRef, final Pageable pageable) {
    return cannedQueryService.cannedQuery(nodeRef, null, null, pageable, repository.getBeanEntityMapper(), null, null);
  }

  public void delete(RootResourceInformation resourceInformation, final NodeRef nodeRef) {
    repository.delete(nodeRef);
  }

  public Persistable<NodeRef> save(RootResourceInformation resourceInformation, final NodeRef nodeRef, AlfrescoEntityResource<Persistable<NodeRef>> resource, final NodeRef parentRef) {
    Persistable<NodeRef> entity = resource.getContent();
    Field idField = ReflectionUtils.findField(entity.getClass(), "id");
    if (idField != null) {
      ReflectionUtils.makeAccessible(idField);
      ReflectionUtils.setField(idField, entity, nodeRef);
    }

    return repository.save(parentRef, entity, resource.getType(), resource.getInputStream());
  }

  public boolean exists(RootResourceInformation resourceInformation, final NodeRef nodeRef) {
    return repository.exists(nodeRef);
  }

  public AlfrescoNodeRepository<Persistable<NodeRef>> getRepository() {
    return repository;
  }
}
