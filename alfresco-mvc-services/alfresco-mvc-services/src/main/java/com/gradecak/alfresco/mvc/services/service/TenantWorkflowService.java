package com.gradecak.alfresco.mvc.services.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.data.domain.CmDocument;
import com.gradecak.alfresco.mvc.data.rest.service.CoreCannedQueryService;
import com.gradecak.alfresco.querytemplate.NodePropertiesMapper;
import com.gradecak.alfresco.mvc.services.domain.CoreTask;
import com.gradecak.alfresco.mvc.services.domain.CoreWorkflow;
import com.gradecak.alfresco.mvc.services.domain.Note;
import com.gradecak.alfresco.mvc.services.mapper.ContainerDocumentPropertiesMapper;
import com.gradecak.alfresco.mvc.services.workflow.WorkflowQNameConverter;

@Service
public class TenantWorkflowService {

  public static final String IBAPP_ENTITY_FIELD = "ibapp_entity";
  public static final String IBAPP_WORKFLOW_TYPE = "ibapp_type";
  public static final String IBAPP_PRIMARYKEY_FIELD = "ibapp_primaryKey";
  public static final String PACKAGE_FIELD = "ibapp_package";
  public static final String RELATED_FIELD = "ibapp_related";

  public static final String PACKAGE_FOLDER = "packages";
  public static final String IBAPP_TENANT = "ibapp_tenant";
  public static final String IBAPP_WORKFLOW_DESCRIPTION = "ibapp_workflow_description";
  public static final String IBAPP_WORKFLOW_DUEDATE = "ibapp_workflow_duedate";

  @Autowired
  private WorkflowQNameConverter qnameConverter;

  @Autowired
  private RuntimeService runtimeService;

  @Autowired
  private HistoryService historyService;

  @Autowired
  private IbappTenantService tenantService;

  @Autowired
  private NoteService noteService;

  @Autowired
  private ServiceRegistry serviceRegistry;

  @Autowired
  private RepositoryService repoService;

  @Autowired
  private TaskService activitiTaskService;
  
  @Autowired
  private TenantDocumentService tenantDocumentService;
  
  @AlfrescoTransaction(readOnly = true)
  public Page<CoreWorkflow> list(Map<String, Object> variables, Pageable pageable) throws AccessDeniedException {
    String tenant = tenantService.getCurrentlyLoggedTenant();
    Assert.hasText(tenant);

    HistoricProcessInstanceQuery hQuery = historyService.createHistoricProcessInstanceQuery().includeProcessVariables();
    hQuery.variableValueEquals(TenantWorkflowService.IBAPP_TENANT, tenant);

    if (variables != null) {
      for (Map.Entry<String, Object> entry : variables.entrySet()) {
        hQuery.variableValueEquals(entry.getKey(), entry.getValue());
      }
    }

    // if (Boolean.TRUE.equals(active)) {
    hQuery.unfinished();
    // } else if (Boolean.FALSE.equals(active)) {
    // hQuery.finished();
    // }

    Sort sort = pageable.getSort();
    // if(sort != null) {
    // sort.
    // }

    // if (ProcessOrderBy.processId == sort) {
    // hQuery.orderByProcessInstanceId();
    // } else if (ProcessOrderBy.started == sort) {
    // hQuery.orderByProcessInstanceStartTime();
    // } else if (ProcessOrderBy.completed == sort) {
    // hQuery.orderByProcessInstanceEndTime();
    // } else {
    hQuery.orderByProcessInstanceId();
    // }
    //
    // if (Direction.ASC == dir) {
    hQuery.asc();
    // } else {
    // hQuery.desc();
    // }

    List<HistoricProcessInstance> instances = hQuery.listPage(pageable.getPageNumber() * pageable.getPageSize(), pageable.getPageSize());

    List<CoreWorkflow> workflows = new ArrayList<>(instances.size());
    for (HistoricProcessInstance historicProcessInstance : instances) {
      CoreWorkflow mapWorkflow = mapWorkflow(historicProcessInstance, repoService, serviceRegistry);
      workflows.add(mapWorkflow);
    }

    return new PageImpl<>(workflows, pageable, hQuery.count());
  }

  @AlfrescoTransaction(readOnly = true)
  public CoreWorkflow get(String processId) throws AccessDeniedException {
    String tenant = tenantService.getCurrentlyLoggedTenant();
    Assert.hasText(tenant);

    HistoricProcessInstance historicProcessInstance = getActivitiWorkflow(processId, tenant);
    if (historicProcessInstance == null) {
      throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
    }
    return mapWorkflow(historicProcessInstance, repoService, serviceRegistry);
  }

  @AlfrescoTransaction(readOnly = true)
  public Page<CoreTask> history(String processId, Pageable pageable) throws AccessDeniedException {
    String tenant = tenantService.getCurrentlyLoggedTenant();
    Assert.hasText(tenant);

    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery().processInstanceId(processId).includeTaskLocalVariables();
    List<HistoricTaskInstance> tasks = query.orderByTaskId().asc().listPage(pageable.getPageNumber() * pageable.getPageSize(), pageable.getPageSize());

    List<CoreTask> taskList = new ArrayList<>();
    for (HistoricTaskInstance task : tasks) {
      taskList.add(TenantTaskService.mapTask(task, historyService, repoService, serviceRegistry));
    }

    return new PageImpl<>(taskList, pageable, taskList.size());
  }

  // @AlfrescoTransaction(readOnly = true)
  // public List<CoreWorkflowDefinition> listDeployed() {
  // List<ProcessDefinition> deployments = repositoryService.createProcessDefinitionQuery().list();
  // List<CoreWorkflowDefinition> list = new ArrayList<>();
  //
  // if (deployments != null) {
  // for (ProcessDefinition deployment : deployments) {
  // CoreWorkflowDefinition core = new CoreWorkflowDefinition();
  // core.setId(deployment.getId());
  // core.setDescription(deployment.getDescription() != null ? deployment.getDescription() : deployment.getName());
  // core.setName(deployment.getName());
  // list.add(core);
  // }
  // }
  //
  // return list;
  // }

  @AlfrescoTransaction
  public String start(final String processDefId, final Map<String, Object> variables, final NodeRef packageRef) {
    Assert.hasText(processDefId);
    Assert.notEmpty(variables);

    String tenant = tenantService.getCurrentlyLoggedTenant();
    Assert.hasText(tenant);

    String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
    Assert.hasText(currentUserName);

    Authentication.setAuthenticatedUserId(currentUserName);

    CommandContext context = Context.getCommandContext();
    boolean isContextSuspended = false;
    if (context != null && context.getException() == null) {
      // MNT-11926: push null context to stack to avoid context reusage when new instance is not flushed
      Context.setCommandContext(null);
      isContextSuspended = true;
    }
    try {

      HashMap<String, Object> hashMap = new HashMap<>(variables);
      Long dueDateTs = (Long) hashMap.get(IBAPP_WORKFLOW_DUEDATE);
      if (dueDateTs != null) {
        Date date = new Date(dueDateTs);
        hashMap.put(IBAPP_WORKFLOW_DUEDATE, date);
      } else {
        Date date = new Date();
        hashMap.put(IBAPP_WORKFLOW_DUEDATE, date);
      }

      String initialAuthority = (String) hashMap.get(TenantTaskService.INITIAL_AUTHORITY);
      if (StringUtils.hasText(initialAuthority)) {
        Assert.isTrue(serviceRegistry.getAuthorityService().authorityExists(initialAuthority));
      } else {
        hashMap.put(TenantTaskService.INITIAL_AUTHORITY, currentUserName);
      }

      hashMap.put(TenantTaskService.INITIATOR_AUTHORITY, currentUserName);

      Builder<String, Object> properties = ImmutableMap.<String, Object> builder().putAll(hashMap).put(TenantWorkflowService.IBAPP_TENANT, tenant);

      if (packageRef != null) {
        serviceRegistry.getWorkflowService().createPackage(packageRef);
        properties.put(PACKAGE_FIELD, packageRef);
      }

      ProcessDefinition processDefinitionByKey = getProcessDefinitionByKey(processDefId, repoService);
      ProcessInstance instance = runtimeService.startProcessInstanceById(processDefinitionByKey.getId(), properties.build());

      if (packageRef != null) {
        Serializable pckgInstanceId = serviceRegistry.getNodeService().getProperty(packageRef, WorkflowModel.PROP_WORKFLOW_INSTANCE_ID);
        if (pckgInstanceId != null && !pckgInstanceId.equals(instance.getId())) {
          throw new ActivitiIllegalArgumentException("The workflow package is already associated to an existing process: " + pckgInstanceId);
        }

        String definitionId = instance.getProcessDefinitionId();
        String definitionName = instance.getProcessDefinitionName();
        String instanceId = instance.getId();
        serviceRegistry.getNodeService().setProperty(packageRef, WorkflowModel.PROP_WORKFLOW_DEFINITION_ID, definitionId);
        serviceRegistry.getNodeService().setProperty(packageRef, WorkflowModel.PROP_WORKFLOW_DEFINITION_NAME, definitionName);
        serviceRegistry.getNodeService().setProperty(packageRef, WorkflowModel.PROP_WORKFLOW_INSTANCE_ID, instanceId);
      }

      return instance.getId();
    } finally {
      if (isContextSuspended) {
        // pop null context out of stack
        Context.removeCommandContext();
      }
    }
  }

  @AlfrescoTransaction
  public void delete(String workflowId, String reason) throws AccessDeniedException {
    Assert.hasText(workflowId);
    Assert.hasText(reason);

    try {
      String tenant = tenantService.getCurrentlyLoggedTenant();
      Assert.hasText(tenant);
      ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(workflowId).variableValueEquals(TenantWorkflowService.IBAPP_TENANT, tenant).singleResult();
      if (instance != null) {
        runtimeService.deleteProcessInstance(instance.getId(), reason);
      } else {
        throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
      }
    } catch (Exception e) {
      throw new AccessDeniedException("Access not allowed. Please contact the Administrators.", e);
    }
  }

  public Map<String, Object> convertQnameToStringProperties(Map<QName, Object> properties) {
    Assert.notEmpty(properties);

    Map<String, Object> variables = new HashMap<String, Object>();
    for (Entry<QName, Object> entry : properties.entrySet()) {
      QName key = entry.getKey();
      Object value = entry.getValue();
      String keyStr = qnameConverter.mapQNameToName(key);
      variables.put(keyStr, value);
    }
    return variables;
  }

  @AlfrescoTransaction
  public Note createNote(String processId, String content) {
    Assert.hasText(processId);
    Assert.hasText(content);

    String tenant = tenantService.getCurrentlyLoggedTenant();
    Assert.hasText(tenant);
    HistoricProcessInstance instance = getActivitiWorkflow(processId, tenant);
    NodeRef packageRef = (NodeRef) instance.getProcessVariables().get(PACKAGE_FIELD);

    Note created = noteService.create(packageRef, content);
    return created;
  }

  @AlfrescoTransaction(readOnly = true)
  public Page<Note> listNotes(String processId, final Pageable pageable) {
    Assert.hasText(processId);

    String tenant = tenantService.getCurrentlyLoggedTenant();
    Assert.hasText(tenant);
    HistoricProcessInstance instance = getActivitiWorkflow(processId, tenant);
    NodeRef packageRef = (NodeRef) instance.getProcessVariables().get(PACKAGE_FIELD);

    // todo dgradecak convert pageable
    Page<Note> list = noteService.list(packageRef, pageable);
    return list;
  }

  private HistoricProcessInstance getActivitiWorkflow(String processId, String tenant) {
    Assert.hasText(tenant);
    Assert.hasText(processId);

    HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processId).includeProcessVariables()
        .variableValueEquals(TenantWorkflowService.IBAPP_TENANT, tenant).singleResult();
    if (historicInstance == null) {
      throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
    }

    return historicInstance;
  }

  @AlfrescoTransaction(readOnly = true)
  public Page<CoreTask> getActiveTasks(final String workflowId, Pageable page) {
    Assert.hasText(workflowId);

    String tenant = tenantService.getCurrentlyLoggedTenant();
    Assert.hasText(tenant);

    final List<CoreTask> assignedTaskList = new ArrayList<CoreTask>(3);
    TaskQuery q = activitiTaskService.createTaskQuery().active().includeTaskLocalVariables().includeProcessVariables().processVariableValueEquals(TenantWorkflowService.IBAPP_TENANT, tenant)
        .processInstanceId(workflowId);

    long total = q.count();
    final List<Task> list = q.orderByTaskDueDate().asc().listPage(page.getOffset(), page.getPageSize());

    for (Task task : list) {
      assignedTaskList.add(TenantTaskService.mapTask(task, historyService, repoService, serviceRegistry));
    }

    return new PageImpl<CoreTask>(assignedTaskList, page, total);
  }
  
  @AlfrescoTransaction(readOnly = true)
  public <T extends CmDocument> Page<T> getDocuments(final String workflowId, NodePropertiesMapper<T> mapper, Pageable page) {
    Assert.hasText(workflowId);

    String tenant = tenantService.getCurrentlyLoggedTenant();
    Assert.hasText(tenant);

    HistoricProcessInstance historicProcessInstance = getActivitiWorkflow(workflowId, tenant);
    Map<String, Object> processVariables = historicProcessInstance.getProcessVariables();
    NodeRef packageRef = (NodeRef) processVariables.get(PACKAGE_FIELD);
    
    List<T> documents = new ArrayList<>();
    if(packageRef != null) {
      return tenantDocumentService.list(packageRef, ContentModel.TYPE_CONTENT, mapper, page);
    }

    return new PageImpl<T>(documents, page, 0);
  }

  public static ProcessDefinition getProcessDefinitionByKey(String processKey, RepositoryService repoService) {
    return repoService.createProcessDefinitionQuery().processDefinitionKey(processKey).latestVersion().singleResult();
  }

  @AlfrescoTransaction
  public NodeRef makePackageContainer(NodeRef entityRef) {
    Assert.notNull(entityRef);

    NodeRef workflowsContainer = serviceRegistry.getFileFolderService().searchSimple(entityRef, "workflow_packages");
    if (workflowsContainer == null) {
      QName packageFolderName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, PACKAGE_FOLDER);
      Map<QName, Serializable> properties = ImmutableMap.<QName, Serializable> of(ContentModel.PROP_NAME, "workflow_packages");
      ChildAssociationRef packageFolderAssoc = serviceRegistry.getNodeService().createNode(entityRef, ContentModel.ASSOC_CONTAINS, packageFolderName, ContentModel.TYPE_SYSTEM_FOLDER, properties);

      workflowsContainer = packageFolderAssoc.getChildRef();
    }

    String packageId = "pkg_" + GUID.generate();
    QName packageName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, packageId);

    try {
      // policyBehaviourFilter.disableBehaviour(packages, ContentModel.ASPECT_AUDITABLE);
      ChildAssociationRef packageAssoc = serviceRegistry.getNodeService().createNode(workflowsContainer, ContentModel.ASSOC_CONTAINS, packageName, WorkflowModel.TYPE_PACKAGE);
      NodeRef packageContainer = packageAssoc.getChildRef();
      // TODO: For now, grant full access to everyone
      serviceRegistry.getPermissionService().setPermission(packageContainer, PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true);
      return packageContainer;
    } finally {
      // policyBehaviourFilter.enableBehaviour(packages, ContentModel.ASPECT_AUDITABLE);
    }
  }

  static public CoreWorkflow mapWorkflow(final HistoricProcessInstance historicInstance, RepositoryService repoService, ServiceRegistry serviceRegistry) {
    if (historicInstance == null) {
      throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
    }

    // Get the local task variables from the history
    // Map<String, Object> variables = activitiPropertyConverter.getHistoricTaskVariables(workflow.getId());
    // Map<QName, Serializable> historicTaskProperties = activitiPropertyConverter.getTaskProperties(workflow,
    // variables);
    Map<String, Object> variables = historicInstance.getProcessVariables();

    CoreWorkflow mapped = new CoreWorkflow();

    mapped.setId(historicInstance.getId());

    ProcessDefinition def = getProcessDefinitionByKey(historicInstance.getProcessDefinitionId().split(":")[0], repoService);
    mapped.setName(def.getName());
    mapped.setDescription((String) variables.get(IBAPP_WORKFLOW_DESCRIPTION));
    mapped.setCreator(historicInstance.getStartUserId());
    mapped.setDefinition(historicInstance.getProcessDefinitionId());

    Date endDate = historicInstance.getEndTime();
    String deleteReason = historicInstance.getDeleteReason();

    mapped.setDeleteReason(deleteReason);
    mapped.setStatus(endDate == null ? "IN_PROGRESS" : deleteReason == null ? "CLOSED" : "DELETED");
    mapped.setStartDate(historicInstance.getStartTime());
    mapped.setCompletionDate(endDate);
    mapped.setDueDate((Date) variables.get(IBAPP_WORKFLOW_DUEDATE));

    Map<String, Object> processVariables = historicInstance.getProcessVariables();
    String entity = (String) processVariables.get(IBAPP_ENTITY_FIELD);
    String primaryKey = (String) processVariables.get(IBAPP_PRIMARYKEY_FIELD);

    if (StringUtils.hasText(entity) && StringUtils.hasText(primaryKey)) {
      mapped.setRelated(ImmutableMap.of(TenantWorkflowService.IBAPP_ENTITY_FIELD, entity, TenantWorkflowService.IBAPP_PRIMARYKEY_FIELD, primaryKey));
    }
    
    NodeRef packageRef = (NodeRef) variables.get(PACKAGE_FIELD);
    if(packageRef != null) {
      Builder<String, Boolean> builder = ImmutableMap.builder();
      builder.put("delete", AccessStatus.ALLOWED.equals(serviceRegistry.getPermissionService().hasPermission(packageRef, PermissionService.DELETE)));
      builder.put("edit", AccessStatus.ALLOWED.equals(serviceRegistry.getPermissionService().hasPermission(packageRef, PermissionService.WRITE)));
      
      mapped.setPermissions(builder.build());
      
      mapped.setRelated(ImmutableMap.of(PACKAGE_FIELD, packageRef.getId()));
    }

    return mapped;
  }
}
