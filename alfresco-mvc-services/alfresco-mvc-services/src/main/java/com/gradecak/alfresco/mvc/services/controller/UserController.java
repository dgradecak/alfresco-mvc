package com.gradecak.alfresco.mvc.services.controller;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.gradecak.alfresco.mvc.ControllerUtils;
import com.gradecak.alfresco.mvc.HateaosUtils;
import com.gradecak.alfresco.mvc.services.SecurityProvider.SecurityCompanyRole;
import com.gradecak.alfresco.mvc.services.domain.CmUser;
import com.gradecak.alfresco.mvc.services.domain.CoreTask;
import com.gradecak.alfresco.mvc.services.service.IbappTenantService;
import com.gradecak.alfresco.mvc.services.service.TenantUserService;
import com.gradecak.alfresco.mvc.services.service.TenantUserService.WorkflowType;
import com.gradecak.alfresco.mvc.services.service.UserInformation;

@Controller
@RequestMapping("/user")
public class UserController {

  @Autowired
  private TenantUserService userService;

  @Autowired
  private IbappTenantService tenantservice;
  
  @Autowired
  private List<UserInformation> userInformationList;

  @RequestMapping(value = "/role", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> get() {

    try {
      String username = AuthenticationUtil.getFullyAuthenticatedUser();
      SecurityCompanyRole currentlyLogged = tenantservice.getCurrentlyLogged();
      CmUser user = userService.getUser(username);
      
      Builder<String, Serializable> builder = ImmutableMap.<String, Serializable>builder();
      if(userInformationList != null) {
        for (UserInformation userInformation : userInformationList) {
          Map<String, Serializable> map = userInformation.getUserInformation(username);
          builder.putAll(map);
        }
      }
      
      ImmutableMap<String, Serializable> map = builder.put("role", currentlyLogged).put("user", user).build();
      return ControllerUtils.toResponseEntity(map);
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "/tasks", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> getActiveTasks(@RequestParam(required = false) Boolean active, @RequestParam(required = false) String user, Pageable pageable) {
    try {
      Page<CoreTask> tasks = userService.getActiveTasks(active, user, pageable);
      return ControllerUtils.toResponseEntity(HateaosUtils.createPagedResource(tasks));
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "list", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> listAllCompanyUsers() {

    try {
      Set<CmUser> findAll = userService.findAll();
      return ControllerUtils.toResponseEntity(findAll);
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "/workflows", method = { RequestMethod.POST })
  @ResponseBody
  public ResponseEntity<?> createWorkflow(@RequestParam String processDef, @RequestParam String workflowType, @RequestBody(required = false) HashMap<String, Object> properties) {
    String createWorkflow = userService.createWorkflow(processDef, WorkflowType.valueOf(workflowType), properties);
    return ControllerUtils.toResponseEntity(ImmutableMap.of("id", createWorkflow));
  }

  @RequestMapping(value = "/workflows", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> getWorkflows(@RequestParam Date fromDate, @RequestParam Date toDate, @RequestParam String workflowType, Pageable pageable) {
    // TODO add pagination
    try {
      Page<CoreTask> page = userService.getActiveTasksOfType(fromDate, toDate, WorkflowType.valueOf(workflowType), pageable);
      return ControllerUtils.toResponseEntity(HateaosUtils.createPagedResource(page));
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }
  
  @RequestMapping(value = "sites", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> listSites() {

    try {
      List<?> libraries = userService.listLibraries();
      return ControllerUtils.toResponseEntity(libraries);
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @InitBinder
  protected void initBinder(final WebDataBinder binder) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
  }
}
