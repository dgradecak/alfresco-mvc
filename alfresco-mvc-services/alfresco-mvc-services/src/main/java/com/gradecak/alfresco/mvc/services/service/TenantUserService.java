package com.gradecak.alfresco.mvc.services.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authority.AuthorityException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.data.domain.CmFolder;
import com.gradecak.alfresco.mvc.data.domain.StSite;
import com.gradecak.alfresco.mvc.data.mapper.CmFolderPropertiesMapper;
import com.gradecak.alfresco.mvc.data.mapper.StSiteMapper;
import com.gradecak.alfresco.querytemplate.BeanPropertiesMapper;
import com.gradecak.alfresco.querytemplate.QueryTemplate;
import com.gradecak.alfresco.mvc.services.SecurityProvider;
import com.gradecak.alfresco.mvc.services.TaskOrderBy;
import com.gradecak.alfresco.mvc.services.domain.CmUser;
import com.gradecak.alfresco.mvc.services.domain.CoreTask;

@Service
public class TenantUserService {

  static public enum WorkflowType {
    reminder
  }

  @Autowired
  private ServiceRegistry serviceRegistry;

  @Autowired
  private IbappTenantService tenantService;

  @Autowired
  private TaskService activitiTaskService;

  @Autowired
  private RepositoryService repoService;

  @Autowired
  private HistoryService historyService;

  @Autowired
  private TenantWorkflowService workflowService;

  @Autowired
  private QueryTemplate queryTemplate;

  @Autowired
  private Repository repository;

  @Autowired
  private IbappSiteService siteService;

  @Autowired
  private CmFolderPropertiesMapper cmFolderMapper;

  @AlfrescoTransaction(readOnly = true)
  public Page<CoreTask> getActiveTasks(final Boolean active, final String user, Pageable page) {
    String username = user;
    if (!StringUtils.hasText(username)) {
      username = AuthenticationUtil.getFullyAuthenticatedUser();
    }
    Assert.hasText(username);

    if (serviceRegistry.getAuthorityService().authorityExists(username)) {
      String tenant = tenantService.getCurrentlyLoggedTenant();
      Assert.hasText(tenant);

      final List<CoreTask> assignedTaskList = new ArrayList<CoreTask>(3);

      long total = 0;
      if (active == null || Boolean.TRUE.equals(active)) {
        TaskQuery q = activitiTaskService.createTaskQuery().includeTaskLocalVariables().taskAssignee(username).includeProcessVariables()
            .processVariableValueEquals(TenantWorkflowService.IBAPP_TENANT, tenant).active();
        total = q.count();

        Sort sort = page.getSort();
        Order order = findFirstOrderBy(sort);
        TaskOrderBy orderBy = TaskOrderBy.valueOf(order.getProperty());
        if (TaskOrderBy.assignee == orderBy) {
          q.orderByTaskAssignee();
        } else if (TaskOrderBy.taskDueDate == orderBy) {
          q.orderByTaskDueDate();
        } else if (TaskOrderBy.description == orderBy) {
          q.orderByTaskDescription();
        } else if (TaskOrderBy.title == orderBy) {
          q.orderByTaskName();
        } else {
          q.orderByTaskDueDate();
        }

        if (order.isAscending()) {
          q.asc();
        } else {
          q.desc();
        }

        final List<Task> list = q.listPage(page.getOffset(), page.getPageSize());

        for (Task task : list) {
          assignedTaskList.add(TenantTaskService.mapTask(task, historyService, repoService, serviceRegistry));
        }
      } else {
        HistoricTaskInstanceQuery hq = historyService.createHistoricTaskInstanceQuery().finished().includeTaskLocalVariables().taskAssignee(username).includeProcessVariables()
            .processVariableValueEquals(TenantWorkflowService.IBAPP_TENANT, tenant);

        Sort sort = page.getSort();
        Order order = findFirstOrderBy(sort);
        TaskOrderBy orderBy = TaskOrderBy.valueOf(order.getProperty());
        if (TaskOrderBy.assignee == orderBy) {
          hq.orderByTaskAssignee();
        } else if (TaskOrderBy.taskDueDate == orderBy) {
          hq.orderByTaskDueDate();
        } else if (TaskOrderBy.description == orderBy) {
          hq.orderByTaskDescription();
        } else if (TaskOrderBy.title == orderBy) {
          hq.orderByTaskName();
        } else if (TaskOrderBy.completionDate == orderBy) {
          hq.orderByHistoricTaskInstanceEndTime();
        } else {
          hq.orderByTaskDueDate();
        }

        if (order.isAscending()) {
          hq.asc();
        } else {
          hq.desc();
        }

        total = hq.count();
        List<HistoricTaskInstance> list = hq.listPage(page.getOffset(), page.getPageSize());
        for (HistoricTaskInstance task : list) {
          assignedTaskList.add(TenantTaskService.mapTask(task, historyService, repoService, serviceRegistry));
        }
      }

      return new PageImpl<CoreTask>(assignedTaskList, page, total);
    } else {
      throw new AuthorityException("Invalid authority: " + username);
    }
  }

  @AlfrescoTransaction(readOnly = true)
  public Set<CmUser> findAll() {
    String tenant = tenantService.getCurrentlyLoggedTenant();
    Assert.hasText(tenant);

    List<String> allGroups = serviceRegistry.getAuthorityService()
        .getAuthorities(AuthorityType.GROUP, AuthorityService.ZONE_APP_DEFAULT, SecurityProvider.ROLE_PREFIX + tenant + SecurityProvider.SEPARATOR, true, true, new PagingRequest(Integer.MAX_VALUE))
        .getPage();

    Set<CmUser> allUsers = new HashSet<CmUser>();

    for (String group : allGroups) {
      allUsers.addAll(getUsersFromGroup(group, true));
    }

    return allUsers;
  }

  @AlfrescoTransaction(readOnly = true)
  public List<CmUser> getUsersFromGroup(final String group, final boolean immediate) {
    Assert.hasText(group);

    String tenant = tenantService.getCurrentlyLoggedTenant();
    Assert.hasText(tenant);

    if (!group.startsWith(PermissionService.GROUP_PREFIX + SecurityProvider.ROLE_PREFIX + tenant + SecurityProvider.SEPARATOR) && !group.startsWith(PermissionService.GROUP_PREFIX + "site")) {
      return Collections.emptyList();
    }

    List<CmUser> userList = new ArrayList<CmUser>();

    Set<String> authorities = serviceRegistry.getAuthorityService().getContainedAuthorities(AuthorityType.USER, group, immediate);
    for (String userName : authorities) {
      CmUser cmUser = getUser(userName);
      userList.add(cmUser);
    }

    return userList;
  }

  @AlfrescoTransaction(readOnly = true)
  public CmUser getUser(final String username) {
    NodeRef authorityNodeRef = serviceRegistry.getAuthorityService().getAuthorityNodeRef(username);
    BeanPropertiesMapper<CmUser> mapper = new BeanPropertiesMapper<CmUser>(serviceRegistry);
    mapper.setMappedClass(CmUser.class);
    CmUser cmUser = queryTemplate.queryForObject(authorityNodeRef, mapper);

    return cmUser;
  }

  @AlfrescoTransaction
  public String createWorkflow(String processDef, WorkflowType workflowType, Map<String, Object> properties) {
    Assert.hasText(processDef);
    Assert.notNull(workflowType);

    Builder<String, Object> builder = ImmutableMap.<String, Object> builder();
    if (!CollectionUtils.isEmpty(properties)) {
      builder.putAll(properties);
    }

    builder.put(TenantWorkflowService.IBAPP_WORKFLOW_TYPE, workflowType.toString());
    String workflowId = workflowService.start(processDef, builder.build(), null);
    return workflowId;
  }

  @AlfrescoTransaction(readOnly = true)
  public Page<CoreTask> getActiveTasksOfType(Date fromDate, Date toDate, WorkflowType workflowType, Pageable page) {
    Assert.notNull(fromDate);
    Assert.notNull(toDate);
    Assert.notNull(workflowType);

    String tenant = tenantService.getCurrentlyLoggedTenant();
    Assert.hasText(tenant);

    final List<CoreTask> assignedTaskList = new ArrayList<CoreTask>(3);

    TaskQuery q = activitiTaskService.createTaskQuery().active().includeTaskLocalVariables().includeProcessVariables().processVariableValueEquals(TenantWorkflowService.IBAPP_TENANT, tenant)
        .processVariableValueEquals(TenantWorkflowService.IBAPP_WORKFLOW_TYPE, workflowType.toString()).taskDueAfter(fromDate).taskDueBefore(toDate);

    long total = q.count();
    final List<Task> list = q.orderByTaskDueDate().asc().listPage(page.getOffset(), page.getPageSize());

    for (Task task : list) {
      assignedTaskList.add(TenantTaskService.mapTask(task, historyService, repoService, serviceRegistry));
    }

    return new PageImpl<CoreTask>(assignedTaskList, page, total);
  }

  static public Order findFirstOrderBy(Sort sort) {
    if (sort != null) {
      TaskOrderBy[] values = TaskOrderBy.values();
      for (TaskOrderBy taskOrderBy : values) {
        Order order = sort.getOrderFor(taskOrderBy.toString());
        if (order != null) {
          return order;
        }
      }
    }
    return new Order(Direction.ASC, TaskOrderBy.taskDueDate.toString());
  }

  @AlfrescoTransaction(readOnly = true)
  public List<CmFolder> listLibraries() {
    List<CmFolder> libraries = new ArrayList<>();

    List<SiteInfo> sites = serviceRegistry.getSiteService().listSites(AuthenticationUtil.getFullyAuthenticatedUser());
    for (SiteInfo siteInfo : sites) {
      StSite stSite = siteService.get(siteInfo.getNodeRef());
      libraries.add(stSite);
    }

    if (serviceRegistry.getAuthorityService().hasAdminAuthority()) {
      NodeRef companyHome = repository.getCompanyHome();
      CmFolder repository = queryTemplate.queryForObject(companyHome, cmFolderMapper);
      repository.setCmTitle("Repository");
      repository.setCmName("Repository");

      StSite stSite = new StSite();
      BeanUtils.copyProperties(repository, stSite);
      stSite.setDocumentLibrary(companyHome);
      libraries.add(stSite);
    }

    return libraries;
  }
}
