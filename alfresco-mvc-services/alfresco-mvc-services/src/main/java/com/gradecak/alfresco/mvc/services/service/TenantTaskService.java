package com.gradecak.alfresco.mvc.services.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.CommentEntity;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfo;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authority.AuthorityException;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.workflow.activiti.properties.ActivitiPropertyConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.services.domain.CoreTask;
import com.gradecak.alfresco.mvc.services.domain.CoreWorkflow;
import com.gradecak.alfresco.mvc.services.domain.Note;
import com.gradecak.alfresco.mvc.services.domain.Note.NoteType;

// TODO add tenant
@Service
public class TenantTaskService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantTaskService.class);

  public static final String COMPLETE_STRING = "complete";
  public static final String BY_PASS_STRING = "bypass";
  public static final String APPROVE_STRING = "approve";
  public static final String REJECT_STRING = "reject";

  public static final String TASK_OUTCOME_FIELD = "ibapp_outcome";
  public static final String TASK_REASON_FIELD = "ibapp_reason";

  public static final String INITIAL_AUTHORITY = "ibapp_initial";
  public static final String REVIEW_STEP = "ibapp_reviewStep";
  public static final String INITIATOR_AUTHORITY = "ibapp_initiator";

  @Autowired
  private ServiceRegistry serviceRegistry;

  @Autowired
  private RuntimeService runtimeService;

  @Autowired
  private RepositoryService repoService;

  @Autowired
  private HistoryService historyService;

  @Autowired
  private TaskService activitiTaskService;

  @Autowired
  private IbappTenantService tenantService;

  @Autowired
  private TenantWorkflowService tenantWorkflowService;

  // @AlfrescoTransaction(readOnly = true)
  // public CountData<CoreTask> getTasks(final String authority, final Boolean active, final Boolean unassigned, final
  // PaginationParams pagination) {
  //
  // final List<CoreTask> assignedTaskList = new ArrayList<CoreTask>(3);
  // Long count = 0L;
  //
  // if (Boolean.TRUE.equals(active)) {
  //
  //
  // TaskQuery q = activitiTaskService.createTaskQuery();
  //
  // if (Boolean.TRUE.equals(unassigned)) {
  // q.taskUnassigned();
  // if (StringUtils.startsWithIgnoreCase(authority, PermissionService.GROUP_PREFIX)) {
  // q.taskCandidateGroup(authority);
  // } else {
  // q.taskCandidateUser(authority);
  // }
  // } else {
  // if (StringUtils.startsWithIgnoreCase(authority, PermissionService.GROUP_PREFIX)) {
  // q.taskCandidateGroup(authority);
  // } else {
  // q.taskAssignee(authority);
  // }
  // }
  //
  // if (TaskOrderBy.assignee.name().equalsIgnoreCase(pagination.getSort())) {
  // q.orderByTaskAssignee();
  // } else if (TaskOrderBy.taskDueDate.name().equalsIgnoreCase(pagination.getSort())) {
  // q.orderByTaskDueDate();
  // } else if (TaskOrderBy.description.name().equalsIgnoreCase(pagination.getSort())) {
  // q.orderByTaskDescription();
  // } else if (TaskOrderBy.title.name().equalsIgnoreCase(pagination.getSort())) {
  // q.orderByTaskName();
  // } else {
  // q.orderByTaskPriority();
  // }
  //
  // if (Direction.ASC == pagination.getDir()) {
  // q.asc();
  // } else {
  // q.desc();
  // }
  //
  // count = q.count();
  // final List<Task> list = q.includeTaskLocalVariables().includeProcessVariables().listPage((pagination.getPage() - 1)
  // * pagination.getLimit(), pagination.getLimit());
  //
  // for (org.activiti.engine.task.Task task : list) {
  // assignedTaskList.add(mapTask(task));
  // }
  //
  // } else if (Boolean.FALSE.equals(active)) {
  // HistoricTaskInstanceQuery q = historyService.createHistoricTaskInstanceQuery();
  // if (Boolean.TRUE.equals(active)) {
  // q.unfinished();
  // } else if (Boolean.FALSE.equals(active)) {
  // q.finished();
  // }
  //
  // q.taskAssignee(authority);
  //
  // if (TaskOrderBy.assignee.name().equalsIgnoreCase(pagination.getSort())) {
  // q.orderByTaskAssignee();
  // } else if (TaskOrderBy.taskDueDate.name().equalsIgnoreCase(pagination.getSort())) {
  // q.orderByTaskDueDate();
  // } else if (TaskOrderBy.description.name().equalsIgnoreCase(pagination.getSort())) {
  // q.orderByTaskDescription();
  // } else if (TaskOrderBy.title.name().equalsIgnoreCase(pagination.getSort())) {
  // q.orderByTaskName();
  // } else {
  // q.orderByTaskPriority();
  // }
  //
  // if (Direction.ASC == pagination.getDir()) {
  // q.asc();
  // } else {
  // q.desc();
  // }
  //
  // count = q.count();
  // final List<HistoricTaskInstance> list =
  // q.includeTaskLocalVariables().includeProcessVariables().listPage((pagination.getPage() - 1) *
  // pagination.getLimit(), pagination.getLimit());
  // for (HistoricTaskInstance task : list) {
  // // TODO remove the need to re-get
  // WorkflowTask workflowTask = serviceRegistry.getWorkflowService().getTaskById("activiti$" + task.getId());
  // assignedTaskList.add(mapTask(task));
  // }
  //
  // }
  //
  // return new CountData<CoreTask>(count, assignedTaskList);
  // }

  @AlfrescoTransaction(readOnly = true)
  public CoreTask getTask(final String taskId) {
    Assert.hasText(taskId);

    String tenant = tenantService.getCurrentlyLoggedTenant();
    Assert.hasText(tenant);

    TaskInfo task = getActivitiTask(taskId, tenant);
    CoreTask coreTask = mapTask(task, historyService, repoService, serviceRegistry);

    return coreTask;
  }

  private HistoricProcessInstance getActivitiWorkflow(String taskId) {
    Assert.hasText(taskId);

    String tenant = tenantService.getCurrentlyLoggedTenant();
    Assert.hasText(tenant);

    TaskInfo task = getActivitiTask(taskId, tenant);
    String processId = task.getProcessInstanceId();

    HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processId).includeProcessVariables().singleResult();
    if (historicInstance == null) {
      throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
    }

    return historicInstance;
  }

  @AlfrescoTransaction(readOnly = true)
  public CoreWorkflow getWorkflow(final String taskId) {
    Assert.hasText(taskId);

    String tenant = tenantService.getCurrentlyLoggedTenant();
    Assert.hasText(tenant);

    // no need to recheck the tenant at this stage
    HistoricProcessInstance historicInstance = getActivitiWorkflow(taskId);
    CoreWorkflow workflow = TenantWorkflowService.mapWorkflow(historicInstance, repoService, serviceRegistry);
    return workflow;
  }

  private TaskInfo getActivitiTask(String taskId, String tenant) {
    Assert.hasText(tenant);
    Assert.hasText(taskId);

    TaskInfo task = activitiTaskService.createTaskQuery().taskId(taskId).includeTaskLocalVariables().includeProcessVariables().processVariableValueEquals(TenantWorkflowService.IBAPP_TENANT, tenant)
        .singleResult();
    if (task == null) {
      task = historyService.createHistoricTaskInstanceQuery().taskId(taskId).includeTaskLocalVariables().includeTaskLocalVariables()
          .processVariableValueEquals(TenantWorkflowService.IBAPP_TENANT, tenant).singleResult();
    }

    if (task == null) {
      throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
    }

    return task;
  }

  @AlfrescoTransaction(readOnly = true)
  public Page<Note> getNotes(final String taskId, final Pageable pageable) {
    Assert.hasText(taskId);

    HistoricProcessInstance historicProcessInstance = getActivitiWorkflow(taskId);

    return tenantWorkflowService.listNotes(historicProcessInstance.getId(), pageable);
  }

  @AlfrescoTransaction
  public void assign(final String taskId, String username, String comment) {
    Assert.hasText(taskId);
    Assert.hasText(username);

    if (serviceRegistry.getAuthorityService().authorityExists(username)) {
      CoreTask task = getTask(taskId);
      String assignee = task.getAssignee();
      String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();

      // TODO check group
      if (currentUser.equals(assignee)) {
        if (currentUser.equals(username)) {
          throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
        }
      }

      Authentication.setAuthenticatedUserId(AuthenticationUtil.getFullyAuthenticatedUser());
      activitiTaskService.setAssignee(taskId, username);

      ImmutableMap<String, String> systemNote = null;
      if (!currentUser.equals(username)) {
        systemNote = ImmutableMap.of("type", "task", "action", "reassigned", "from", assignee, "to", username, "comment", comment != null ? comment : "");
      } else {
        if (currentUser.equals(username)) {
          systemNote = ImmutableMap.of("type", "task", "action", "claimed", "from", assignee != null ? assignee : "", "to", username, "comment", comment != null ? comment : "");
        } else {
          systemNote = ImmutableMap.of("type", "task", "action", "assigned", "from", assignee, "to", username, "comment", comment != null ? comment : "");
        }
      }

      addSystemNote(taskId, systemNote);
    } else {
      throw new AuthorityException("Invalid authority: " + username);
    }
  }

  private void addSystemNote(String taskId, Map<String, String> systemNote) {
    try {
      String sysnote = new ObjectMapper().writeValueAsString(systemNote);
      activitiTaskService.addComment(taskId, null, NoteType.system.name(), sysnote);
    } catch (JsonProcessingException e) {
      Throwables.propagate(e);
    }
  }

  @AlfrescoTransaction(readOnly = true)
  public List<Note> history(String taskId) throws AccessDeniedException {
    String tenant = tenantService.getCurrentlyLoggedTenant();
    Assert.hasText(tenant);

    List<Comment> taskComments = activitiTaskService.getTaskComments(taskId, NoteType.system.name());
    List<Note> commentList = new ArrayList<Note>();
    for (Comment comment : taskComments) {
      Note note = mapComment(comment, serviceRegistry);
      commentList.add(note);
    }

    return commentList;
  }

  @AlfrescoTransaction
  public Note createNote(final String taskId, String comment) {
    HistoricProcessInstance historicProcessInstance = getActivitiWorkflow(taskId);
    return tenantWorkflowService.createNote(historicProcessInstance.getId(), comment);
  }

  @AlfrescoTransaction
  public Note createChecklistItem(final String taskId, String comment, boolean isUpdate) {
    Assert.hasText(taskId);
    Assert.hasText(comment);

    String tenant = tenantService.getCurrentlyLoggedTenant();
    Assert.hasText(tenant);

    TaskInfo task = getActivitiTask(taskId, tenant);
    String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
    Authentication.setAuthenticatedUserId(currentUser);

    String type = getChecklistType(task.getName(), currentUser);
    Comment c = activitiTaskService.addComment(null, task.getProcessInstanceId(), type, comment);

    if (!isUpdate) {
      ImmutableMap<String, String> systemNote = ImmutableMap.of("type", "checklist", "action", "created", "comment", comment);
      addSystemNote(taskId, systemNote);
    }

    return mapComment(c, serviceRegistry);
  }

  @AlfrescoTransaction
  public void deleteChecklistItem(final String taskId, String checklistItemId, boolean isUpdate) {
    Assert.hasText(taskId);
    Assert.hasText(checklistItemId);

    String tenant = tenantService.getCurrentlyLoggedTenant();
    Assert.hasText(tenant);

    TaskInfo task = getActivitiTask(taskId, tenant);
    String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
    Authentication.setAuthenticatedUserId(currentUser);

    String type = getChecklistType(task.getName(), currentUser);
    Comment comment = activitiTaskService.getComment(checklistItemId);
    if (comment.getId().equals(checklistItemId)) {
      if (comment.getType().equals(type) && currentUser.equals(comment.getUserId())) {
        activitiTaskService.deleteComment(checklistItemId);

        if (!isUpdate) {
          ImmutableMap<String, String> systemNote = ImmutableMap.of("type", "checklist", "action", "deleted", "comment", getMessage(comment.getFullMessage()));
          addSystemNote(taskId, systemNote);
        }
        return;
      } else {
        throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
      }
    }

    throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
  }

  @AlfrescoTransaction
  public Note updateChecklistItem(final String taskId, String checklistItemId, String comment) {
    Assert.hasText(taskId);
    Assert.hasText(checklistItemId);
    Assert.hasText(comment);

    String tenant = tenantService.getCurrentlyLoggedTenant();
    Assert.hasText(tenant);

    TaskInfo task = getActivitiTask(taskId, tenant);
    String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
    Authentication.setAuthenticatedUserId(currentUser);

    Comment c = activitiTaskService.getComment(checklistItemId);
    String stored = c.getFullMessage();

    boolean storedChecked = isChecked(stored);
    boolean updatedChecked = isChecked(comment);
    if (storedChecked != updatedChecked) {
      if (updatedChecked) {
        ImmutableMap<String, String> systemNote = ImmutableMap.of("type", "checklist", "action", "completed", "comment", comment);
        addSystemNote(taskId, systemNote);
      } else {
        ImmutableMap<String, String> systemNote = ImmutableMap.of("type", "checklist", "action", "incompleted", "comment", comment);
        addSystemNote(taskId, systemNote);
      }
    }

    deleteChecklistItem(taskId, checklistItemId, true);

    return createChecklistItem(taskId, comment, true);
  }

  @AlfrescoTransaction(readOnly = true)
  public List<Note> getChecklist(final String taskId) {
    Assert.hasText(taskId);

    String tenant = tenantService.getCurrentlyLoggedTenant();
    Assert.hasText(tenant);

    String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
    Authentication.setAuthenticatedUserId(currentUser);
    TaskInfo task = getActivitiTask(taskId, tenant);

    List<Comment> comments = new ArrayList<>();
    String type = getChecklistType(task.getName(), currentUser);
    comments.addAll(activitiTaskService.getProcessInstanceComments(task.getProcessInstanceId(), type));

    List<Note> notes = new ArrayList<Note>();
    if (comments != null) {
      for (Comment comment : comments) {
        Note note = mapComment(comment, serviceRegistry);
        notes.add(note);
      }
    }
    return notes;
  }

  private String getChecklistType(final String taskName, final String username) {

    // taskName ___ username
    return taskName + "___" + username;
  }

  /**
   * 
   * @param taskId
   * @param outcome
   * @param comment
   * @return the next task if any otherwise null
   */
  @AlfrescoTransaction
  public String complete(final String taskId, final String outcome, final String comment) {
    Assert.hasText(taskId);
    Assert.hasText(outcome);

    CoreTask task = getTask(taskId);

    Authentication.setAuthenticatedUserId(AuthenticationUtil.getFullyAuthenticatedUser());
    Builder<String, Object> builder = ImmutableMap.builder();
    builder.put(TASK_OUTCOME_FIELD, outcome);
    builder.put(TASK_REASON_FIELD, comment);
    // if (StringUtils.hasText(comment)) {
    // // builder.put(TASK_COMMENT_FIELD, comment);
    // activitiTaskService.addComment(taskId, null, NoteType.user.name(), comment);
    // }

    ImmutableMap<String, String> systemNote = ImmutableMap.of("type", "task", "action", outcome, "comment", comment != null ? comment : "");
    addSystemNote(taskId, systemNote);

    String instanceId = activitiTaskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();

    // query for the next task ? if more take the first.
    List<Task> activeTaskList = activitiTaskService.createTaskQuery().processInstanceId(instanceId).taskAssignee(AuthenticationUtil.getFullyAuthenticatedUser()).active().list();

    activitiTaskService.complete(taskId, builder.build());

    String nextId = activeTaskList != null && activeTaskList.size() > 0 ? activeTaskList.get(0).getId() : null;
    return nextId;
  }

  static public CoreTask mapTask(final TaskInfo task, HistoryService historyService, RepositoryService repoService, ServiceRegistry serviceRegistry) {
    if (task == null) {
      throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
    }

    String assignee = task.getAssignee();

    CoreTask mappedTask = new CoreTask();
    mappedTask.setId(task.getId());
    mappedTask.setName(task.getName());
    mappedTask.setDescription(task.getDescription());
    mappedTask.setCompletionDate(null);

    Builder<String, Boolean> builder = ImmutableMap.builder();
    boolean isAssignee = AuthenticationUtil.getFullyAuthenticatedUser().equals(assignee);
    boolean closed = false;
    if (task instanceof HistoricTaskInstance) {
      HistoricTaskInstance h = (HistoricTaskInstance) task;
      if (h.getEndTime() != null) {
        closed = true;

        mappedTask.setCompletionDate(h.getEndTime());
      }
    }

    if (closed) {
      mappedTask.setStatus("CLOSED");
      builder.put("claim", false);
      builder.put("complete", false);
    } else {
      mappedTask.setStatus("IN_PROGRESS");

      builder.put("claim", !isAssignee);
      builder.put("complete", isAssignee);
    }

    mappedTask.setPriority(String.valueOf(task.getPriority()));
    mappedTask.setStartDate(task.getCreateTime());
    mappedTask.setAssignee(assignee);
    mappedTask.setDueDate(task.getDueDate());
    Map<String, Object> taskLocalVariables = task.getTaskLocalVariables();
    mappedTask.setOutcome((String) task.getTaskLocalVariables().get(TASK_OUTCOME_FIELD));
    mappedTask.setProcessId(task.getProcessInstanceId());

    mappedTask.setActions(builder.build());

    HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).includeProcessVariables().singleResult();
    if (historicInstance == null) {
      throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
    }

    mappedTask.setWorkflow(TenantWorkflowService.mapWorkflow(historicInstance, repoService, serviceRegistry));

    Map<String, Object> processVariables = historicInstance.getProcessVariables();
    String entity = (String) processVariables.get(TenantWorkflowService.IBAPP_ENTITY_FIELD);
    String primaryKey = (String) processVariables.get(TenantWorkflowService.IBAPP_PRIMARYKEY_FIELD);

    if (StringUtils.hasText(entity) && StringUtils.hasText(primaryKey)) {
      mappedTask.setRelated(ImmutableMap.of(TenantWorkflowService.IBAPP_ENTITY_FIELD, entity, TenantWorkflowService.IBAPP_PRIMARYKEY_FIELD, primaryKey));
    }

    return mappedTask;
  }

  public static Note mapComment(Comment comment, ServiceRegistry serviceRegistry) {
    Assert.notNull(comment);

    Note note = new Note();
    note.setId(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, comment.getId()));
    note.setCmCreated(comment.getTime());
    note.setCmCreator(comment.getUserId());
    note.setCmModified(comment.getTime());
    note.setCmModifier(comment.getUserId());
    note.setHtmlContent(((CommentEntity)comment).getMessage());

    if (!NoteType.system.name().equals(comment.getType())) {
      String creator = comment.getUserId();
      Boolean canEdit = AuthenticationUtil.getFullyAuthenticatedUser().equals(creator) || serviceRegistry.getAuthorityService().hasAdminAuthority();
      ImmutableMap<String, Boolean> permissions = ImmutableMap.of("edit", canEdit, "delete", canEdit);
      note.setPermissions(permissions);
      note.setType(NoteType.user);
    } else {
      note.setType(NoteType.system);
    }

    return note;
  }

  private static boolean isChecked(String checklistItem) {
    HashMap<Object, Object> map = null;
    try {
      map = new ObjectMapper().readValue(checklistItem, new TypeReference<HashMap<Object, Object>>() {});
    } catch (IOException e) {
      Throwables.propagate(e);
    }

    Boolean checked = (Boolean) map.get("checked");
    if (checked == null) {
      return false;
    }

    return checked.booleanValue();
  }

  private static String getMessage(String checklistItem) {
    Assert.hasText(checklistItem);

    HashMap<Object, Object> map = null;
    try {
      map = new ObjectMapper().readValue(checklistItem, new TypeReference<HashMap<Object, Object>>() {});
    } catch (IOException e) {
      Throwables.propagate(e);
    }
    String comment = (String) map.get("text");
    return comment != null ? comment : "";
  }
}