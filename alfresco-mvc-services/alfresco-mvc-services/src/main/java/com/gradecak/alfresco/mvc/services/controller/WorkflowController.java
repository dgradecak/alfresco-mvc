package com.gradecak.alfresco.mvc.services.controller;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gradecak.alfresco.mvc.ControllerUtils;
import com.gradecak.alfresco.mvc.HateaosUtils;
import com.gradecak.alfresco.mvc.services.domain.ContainerDocument;
import com.gradecak.alfresco.mvc.services.domain.CoreTask;
import com.gradecak.alfresco.mvc.services.domain.CoreWorkflow;
import com.gradecak.alfresco.mvc.services.domain.Note;
import com.gradecak.alfresco.mvc.services.mapper.ContainerDocumentPropertiesMapper;
import com.gradecak.alfresco.mvc.services.service.TenantTaskService;
import com.gradecak.alfresco.mvc.services.service.TenantWorkflowService;

@Controller
@RequestMapping("/workflow")
public class WorkflowController {
  private final TenantWorkflowService workflowService;
  private final TenantTaskService taskService;
  private final ContainerDocumentPropertiesMapper mapper;

  @Autowired
  public WorkflowController(TenantWorkflowService workflowService, TenantTaskService taskService, ContainerDocumentPropertiesMapper mapper) {
    Assert.notNull(workflowService);
    Assert.notNull(taskService);
    Assert.notNull(mapper);

    this.workflowService = workflowService;
    this.taskService = taskService;
    this.mapper = mapper;
  }

  @RequestMapping(value = "{workflowId}/delete", method = { RequestMethod.POST })
  @ResponseBody
  public ResponseEntity<?> delete(@PathVariable String workflowId, @RequestParam String reason) {
    try {
      workflowService.delete(workflowId, reason);
      return ControllerUtils.toEmptyResponseEntity();
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{workflowId}", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> get(@PathVariable String workflowId) {
    try {
      CoreWorkflow workflow = workflowService.get(workflowId);
      return ControllerUtils.toResponseEntity(workflow);
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @RequestMapping(value = "{workflowId}/documents", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> getDocuments(@PathVariable String workflowId, Pageable pageable) {
    try {
      Page<ContainerDocument> page = workflowService.getDocuments(workflowId, mapper, pageable);
      return ControllerUtils.toResponseEntity(HateaosUtils.createPagedResource(page));
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }
  //
  // @RequestMapping(value = "/documents", method = { RequestMethod.POST })
  // @ResponseBody
  // public ResponseEntity<?> createDocument(@PathVariable String entity, @PathVariable String primaryKey, WasteDocument
  // document, @RequestParam("filedata") final MultipartFile file) {
  // try {
  // NodeRef entityContainerRef = entityContainerService.getOrCreateEntityContainer(entity, primaryKey);
  // String cmName = document.getCmName();
  // String originalFilename = file.getOriginalFilename();
  // if (StringUtils.hasText(cmName)) {
  // document.setCmName(new StringBuilder(cmName).append("-").append(originalFilename).toString());
  // } else {
  // document.setCmName(originalFilename);
  // }
  // WasteDocument created = entityContainerService.createDocument(entityContainerRef, document, file.getInputStream());
  // return ControllerUtils.toResponseEntity(created);
  // } catch (Exception e) {
  // return ControllerUtils.toEmptyResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
  // }
  // }

  @RequestMapping(value = "{workflowId}/notes", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> getNotes(@PathVariable String workflowId, Pageable pageable) {
    // TODO add pagination
    try {
      Page<Note> page = workflowService.listNotes(workflowId, pageable);
      return ControllerUtils.toResponseEntity(HateaosUtils.createPagedResource(page));
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{workflowId}/notes", method = { RequestMethod.POST })
  @ResponseBody
  public ResponseEntity<?> createNote(@PathVariable String workflowId, @RequestBody Note note) {
    try {
      Note n = workflowService.createNote(workflowId, note.getHtmlContent());
      return ControllerUtils.toResponseEntity(n);
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{workflowId}/tasks", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> getActiveTasks(@PathVariable String workflowId, Pageable pageable) {
    try {
      Page<CoreTask> tasks = workflowService.getActiveTasks(workflowId, pageable);
      return ControllerUtils.toResponseEntity(HateaosUtils.createPagedResource(tasks));
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{workflowId}/history", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> history(@PathVariable String workflowId, Pageable pageable) {
    // TODO add pagination
    try {
      Page<CoreTask> page = workflowService.history(workflowId, pageable);
      return ControllerUtils.toResponseEntity(HateaosUtils.createPagedResource(page));
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }
}