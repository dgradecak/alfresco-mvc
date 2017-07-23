package com.gradecak.alfresco.mvc.services.service;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.node.getchildren.FilterPropString;
import org.alfresco.repo.node.getchildren.FilterPropString.FilterTypeString;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionDoesNotExistException;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.data.domain.CmDocument;
import com.gradecak.alfresco.mvc.data.domain.CoreVersion;
import com.gradecak.alfresco.mvc.data.rest.service.CoreCannedQueryService;
import com.gradecak.alfresco.querytemplate.NodePropertiesMapper;
import com.gradecak.alfresco.querytemplate.QueryTemplate;
import com.gradecak.alfresco.mvc.services.domain.CoreTask;
import com.gradecak.alfresco.mvc.services.domain.CoreWorkflow;
import com.gradecak.alfresco.mvc.services.domain.DocumentVersion;
import com.gradecak.alfresco.mvc.services.domain.Note;
import com.gradecak.alfresco.mvc.services.model.IbappModel;

@Service
public class TenantDocumentService {

  public static final String IBAPP_DOCUMENT_FIELD = "ibapp_document";

  @Autowired
  private CoreCannedQueryService cannedQueryService;

  @Autowired
  private ServiceRegistry serviceRegistry;

  @Autowired
  private QueryTemplate queryTemplate;

  @Autowired
  private NoteService noteService;

  @Autowired
  private TenantWorkflowService workflowService;

  @Autowired
  private IbappTenantService tenantService;

  @Autowired
  private TaskService activitiTaskService;

  @Autowired
  private RepositoryService repoService;

  @Autowired
  private HistoryService historyService;

  @AlfrescoTransaction(readOnly = true)
  public <T> Page<T> list(NodeRef packageRef, QName type, NodePropertiesMapper<T> mapper, Pageable pageable) {
    return list(packageRef, type, ContentModel.ASSOC_CONTAINS, mapper, pageable);
  }

  // TODO use pageable sort / filter
  @AlfrescoTransaction(readOnly = true)
  public <T> Page<T> list(NodeRef packageRef, QName type, QName assocType, NodePropertiesMapper<T> mapper, Pageable pageable) {
    if (serviceRegistry.getNodeService().exists(packageRef)) {
      String tenant = tenantService.getCurrentlyLoggedTenant();
      Assert.hasText(tenant);

      List<Pair<QName, Boolean>> sortProps = new ArrayList<Pair<QName, Boolean>>(2);
      sortProps.add(new Pair<QName, Boolean>(ContentModel.PROP_CREATED, Boolean.TRUE));

      // List<FilterProp> filterProps = Collections.singletonList((FilterProp) new
      // FilterPropString(ContentModel.PROP_CREATOR, forUser, FilterTypeString.EQUALS));

      List<FilterProp> filterProps = ImmutableList.of(new FilterPropString(IbappModel.PROP_TENANT, tenant, FilterTypeString.EQUALS));

      Page<T> cannedQuery = cannedQueryService.cannedQuery(packageRef, filterProps, sortProps, pageable, mapper, Collections.singleton(assocType), Collections.singleton(type));

      return cannedQuery;
    } else {
      throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
    }
  }

  @AlfrescoTransaction
  public NodeRef create(NodeRef parentRef, Map<QName, Serializable> properties, InputStream stream, QName type) {
    if (serviceRegistry.getNodeService().exists(parentRef)) {
      String tenant = tenantService.getCurrentlyLoggedTenant();
      Assert.hasText(tenant);

      // if (!serviceRegistry.getDictionaryService().isSubClass(type, IbappModel.TYPE_DOCUMENT)) {
      // throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
      // }

      Builder<QName, Serializable> builder = ImmutableMap.builder();
      ImmutableMap<QName, Serializable> metadata = builder.putAll(properties).put(IbappModel.PROP_TENANT, tenant).build();

      NodeRef nodeRef = serviceRegistry.getNodeService().createNode(parentRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, type, metadata).getChildRef();
      ContentWriter writer = serviceRegistry.getContentService().getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
      writer.setMimetype(serviceRegistry.getMimetypeService().guessMimetype((String) metadata.get(ContentModel.PROP_NAME)));
      writer.putContent(stream);
      return nodeRef;
    } else {
      throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
    }
  }

  @AlfrescoTransaction(readOnly = true)
  public <T> T get(NodeRef nodeRef, NodePropertiesMapper<T> mapper) {
    if (serviceRegistry.getNodeService().exists(nodeRef)) {
      tenantService.checkTenantAccess(nodeRef);

      return queryTemplate.queryForObject(nodeRef, mapper);
    } else {
      throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
    }
  }

  @AlfrescoTransaction(readOnly = true)
  public List<CoreVersion> getVersions(NodeRef nodeRef) {
    Assert.notNull(nodeRef);

    if (serviceRegistry.getNodeService().exists(nodeRef)) {
      tenantService.checkTenantAccess(nodeRef);

      VersionHistory versionHistory = serviceRegistry.getVersionService().getVersionHistory(nodeRef);
      if (versionHistory == null) {
        return Collections.emptyList();
      }

      List<CoreVersion> versionList = new ArrayList<>();
      String currentVersion = (String) serviceRegistry.getNodeService().getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);

      Collection<Version> versions = versionHistory.getAllVersions();
      for (Version version : versions) {
        if (currentVersion.equals(version.getVersionLabel())) {
          continue;
        }

        CoreVersion snapshot = mapVersion(version);
        versionList.add(snapshot);
      }

      return versionList;
    } else {
      throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
    }
  }

  @AlfrescoTransaction
  public void delete(NodeRef nodeRef) {
    if (serviceRegistry.getNodeService().exists(nodeRef)) {
      tenantService.checkTenantAccess(nodeRef);
      serviceRegistry.getNodeService().deleteNode(nodeRef);
    } else {
      throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
    }
  }

  @AlfrescoTransaction
  public void update(NodeRef nodeRef, Map<QName, Serializable> properties, InputStream stream) {
    if (serviceRegistry.getNodeService().exists(nodeRef)) {
      tenantService.checkTenantAccess(nodeRef);

      serviceRegistry.getNodeService().addProperties(nodeRef, properties);
      if (stream != null) {
        ContentWriter writer = serviceRegistry.getContentService().getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        writer.putContent(stream);
      }
    } else {
      throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
    }
  }

  @AlfrescoTransaction(readOnly = true)
  public <T extends CmDocument> DocumentVersion<T> getVersion(final NodeRef nodeRef, NodePropertiesMapper<T> mapper, final String version) {

    if (serviceRegistry.getNodeService().exists(nodeRef)) {
      tenantService.checkTenantAccess(nodeRef);

      VersionHistory versionHistory = serviceRegistry.getVersionService().getVersionHistory(nodeRef);
      Version ver = versionHistory.getVersion(version);
      if (ver != null) {

        // ContentReader reader = serviceRegistry.getContentService().getReader(ver.getFrozenStateNodeRef(),
        // ContentModel.PROP_CONTENT);
        try {
          // TODO dgradecak : map properties & urls
          DocumentVersion<T> snapshot = mapVersion(ver);
          T document = queryTemplate.queryForObject(ver.getFrozenStateNodeRef(), mapper);
          snapshot.setDocument(document);
          return snapshot;
        } catch (Exception e) {
          Throwables.propagate(e);
        }
      }
      throw new VersionDoesNotExistException(version);
    } else {
      throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
    }
  }

  @AlfrescoTransaction
  public Note createNote(NodeRef nodeRef, String content) {
    Assert.notNull(nodeRef);
    Assert.hasText(content);

    String tenant = tenantService.getCurrentlyLoggedTenant();
    Assert.hasText(tenant);

    Note created = noteService.create(nodeRef, content);
    return created;
  }

  @AlfrescoTransaction(readOnly = true)
  public Page<Note> listNotes(final NodeRef nodeRef, final Pageable pageable) {
    Assert.notNull(nodeRef);
    tenantService.checkTenantAccess(nodeRef);

    // todo dgradecak convert pageable
    Page<Note> list = noteService.list(nodeRef, pageable);
    return list;
  }

  @AlfrescoTransaction
  public String createWorkflow(String processDef, NodeRef nodeRef, Map<String, Object> properties) {
    Assert.hasText(processDef);
    Assert.notNull(nodeRef);
    tenantService.checkTenantAccess(nodeRef);

    Builder<String, Object> builder = ImmutableMap.<String, Object> builder();
    if (!CollectionUtils.isEmpty(properties)) {
      builder.putAll(properties);
    }

    builder.putAll(ImmutableMap.of(IBAPP_DOCUMENT_FIELD, nodeRef.getId()));

    NodeRef pkg = serviceRegistry.getWorkflowService().createPackage(null);
    serviceRegistry.getPermissionService().deletePermissions(pkg);
    serviceRegistry.getPermissionService().setInheritParentPermissions(pkg, false);
    Set<AccessPermission> permissions = serviceRegistry.getPermissionService().getAllSetPermissions(nodeRef);
    for (AccessPermission accessPermission : permissions) {
      serviceRegistry.getPermissionService().setPermission(pkg, accessPermission.getAuthority(), accessPermission.getPermission(), AccessStatus.ALLOWED.equals(accessPermission.getAccessStatus()));
    }

    NodeRef packageContainer = workflowService.makePackageContainer(pkg);
    serviceRegistry.getNodeService().addChild(packageContainer, nodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);

    String workflowId = workflowService.start(processDef, builder.build(), packageContainer);
    String description = (String) properties.get(TenantWorkflowService.IBAPP_WORKFLOW_DESCRIPTION);
    Assert.hasText(description);

    noteService.createSystemNote(nodeRef, ImmutableMap.of("type", "workflow", "action", "created", "id", workflowId, "description", description));

    return workflowId;
  }

  @AlfrescoTransaction(readOnly = true)
  public Page<CoreWorkflow> listWorkflows(NodeRef nodeRef, Map<String, Object> properties, Pageable pageable) {
    Assert.notNull(nodeRef);
    tenantService.checkTenantAccess(nodeRef);

    Builder<String, Object> builder = ImmutableMap.<String, Object> builder();
    if (!CollectionUtils.isEmpty(properties)) {
      builder.putAll(properties);
    }

    Builder<String, Object> containerProperties = builder.putAll(ImmutableMap.of(IBAPP_DOCUMENT_FIELD, nodeRef.getId()));
    return workflowService.list(containerProperties.build(), pageable);
  }

  @AlfrescoTransaction(readOnly = true)
  public Page<CoreTask> listActiveTasks(NodeRef nodeRef, Map<String, Object> properties, Pageable pageable) {
    Assert.notNull(nodeRef);
    tenantService.checkTenantAccess(nodeRef);

    Builder<String, Object> builder = ImmutableMap.<String, Object> builder();
    if (!CollectionUtils.isEmpty(properties)) {
      builder.putAll(properties);
    }

    Builder<String, Object> workflowProperties = builder.putAll(ImmutableMap.of(IBAPP_DOCUMENT_FIELD, nodeRef.getId()));

    TaskQuery query = activitiTaskService.createTaskQuery().active().orderByTaskDueDate().asc().includeTaskLocalVariables().includeProcessVariables();

    for (Map.Entry<String, Object> entry : workflowProperties.build().entrySet()) {
      query.processVariableValueEquals(entry.getKey(), entry.getValue());
    }

    List<Task> instances = query.listPage(pageable.getOffset(), pageable.getPageSize());
    final List<CoreTask> assignedTaskList = new ArrayList<CoreTask>(3);
    for (Task task : instances) {
      assignedTaskList.add(TenantTaskService.mapTask(task, historyService, repoService, serviceRegistry));
    }

    return new PageImpl<>(assignedTaskList, pageable, query.count());
  }

  private <T extends CmDocument> DocumentVersion<T> mapVersion(Version version) {
    DocumentVersion<T> snapshot = new DocumentVersion<>();
    snapshot.setId(version.getFrozenStateNodeRef());
    snapshot.setCmCreated(version.getFrozenModifiedDate());
    snapshot.setCmCreator(version.getFrozenModifier());
    snapshot.setVersion(version.getVersionLabel());
    return snapshot;
  }
}
