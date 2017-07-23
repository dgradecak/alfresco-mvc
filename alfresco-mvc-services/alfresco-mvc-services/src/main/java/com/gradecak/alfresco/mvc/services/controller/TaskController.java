package com.gradecak.alfresco.mvc.services.controller;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableList;
import com.gradecak.alfresco.mvc.ControllerUtils;
import com.gradecak.alfresco.mvc.HateaosUtils;
import com.gradecak.alfresco.mvc.services.domain.CoreTask;
import com.gradecak.alfresco.mvc.services.domain.CoreWorkflow;
import com.gradecak.alfresco.mvc.services.domain.Note;
import com.gradecak.alfresco.mvc.services.service.TenantTaskService;

@Controller
@RequestMapping("/task")
public class TaskController {

  @Autowired
  private TenantTaskService taskService;

  @RequestMapping(value = "{id}", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> get(@PathVariable String id) {
    try {
      CoreTask task = taskService.getTask(id);
      return ControllerUtils.toResponseEntity(task);
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{id}/workflow", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> getProcess(@PathVariable String id) {
    try {
      CoreWorkflow workflow = taskService.getWorkflow(id);
      // WARN: use this due to the client side component
      return ControllerUtils.toResponseEntity(HateaosUtils.createPagedResource(new PageImpl<>(ImmutableList.of(workflow))));
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{id}/notes", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> getNotes(@PathVariable String id, Pageable pageable) {
    try {
      Page<Note> comments = taskService.getNotes(id, pageable);
      return ControllerUtils.toResponseEntity(HateaosUtils.createPagedResource(comments));
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{id}/notes", method = { RequestMethod.POST })
  @ResponseBody
  public ResponseEntity<?> createNote(@PathVariable String id, @RequestBody Note note) {
    try {
      Note comment = taskService.createNote(id, note.getHtmlContent());
      return ControllerUtils.toResponseEntity(comment);
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{id}/history", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> history(@PathVariable String id) {
    try {
      List<Note> notes = taskService.history(id);
      return ControllerUtils.toResponseEntity(notes);
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{id}/checklist", method = { RequestMethod.POST })
  @ResponseBody
  public ResponseEntity<?> createChecklistItem(@PathVariable String id, @RequestBody Note note) {
    try {
      Note comment = taskService.createChecklistItem(id, note.getHtmlContent(), false);
      return ControllerUtils.toResponseEntity(comment);
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{id}/checklist", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> getChecklist(@PathVariable String id) {
    try {
      List<Note> comments = taskService.getChecklist(id);
      return ControllerUtils.toResponseEntity(HateaosUtils.createPagedResource(new PageImpl<>(comments)));
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{id}/checklist/{noteId}", method = { RequestMethod.DELETE })
  @ResponseBody
  public ResponseEntity<?> deleteChecklistItem(@PathVariable String id, @PathVariable String noteId) {
    try {
      taskService.deleteChecklistItem(id, noteId, false);
      return ControllerUtils.toEmptyResponseEntity();
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{id}/checklist/{noteId}", method = { RequestMethod.PUT })
  @ResponseBody
  public ResponseEntity<?> updateCheckListItem(@PathVariable String id, @PathVariable String noteId, @RequestBody Note note) {
    try {
      Note comment = taskService.updateChecklistItem(id, noteId, note.getHtmlContent());
      return ControllerUtils.toResponseEntity(comment);
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{id}/reassign", method = { RequestMethod.POST })
  @ResponseBody
  public ResponseEntity<?> reassign(@PathVariable String id, @RequestParam String username, @RequestParam String comment) {
    try {
      taskService.assign(id, username, comment);
      return ControllerUtils.toEmptyResponseEntity();
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{id}/claim", method = { RequestMethod.POST })
  @ResponseBody
  public ResponseEntity<?> claim(@PathVariable String id) {
    try {
      taskService.assign(id, AuthenticationUtil.getFullyAuthenticatedUser(), null);
      return ControllerUtils.toEmptyResponseEntity();
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{id}/complete", method = { RequestMethod.POST })
  @ResponseBody
  public ResponseEntity<?> complete(@PathVariable String id, @RequestParam(required = false) String comment) {
    try {
      taskService.complete(id, TenantTaskService.COMPLETE_STRING, comment);
      return ControllerUtils.toEmptyResponseEntity();
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{id}/bypass", method = { RequestMethod.POST })
  @ResponseBody
  public ResponseEntity<?> bypass(@PathVariable String id, @RequestParam String comment) {
    try {
      taskService.complete(id, TenantTaskService.BY_PASS_STRING, comment);
      return ControllerUtils.toEmptyResponseEntity();
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{id}/approve", method = { RequestMethod.POST })
  @ResponseBody
  public ResponseEntity<?> approve(@PathVariable String id, @RequestParam String comment) {
    try {
      taskService.complete(id, TenantTaskService.APPROVE_STRING, comment);
      return ControllerUtils.toEmptyResponseEntity();
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{id}/reject", method = { RequestMethod.POST })
  @ResponseBody
  public ResponseEntity<?> reject(@PathVariable String id, @RequestParam String comment) {
    try {
      taskService.complete(id, TenantTaskService.REJECT_STRING, comment);
      return ControllerUtils.toEmptyResponseEntity();
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }
}
