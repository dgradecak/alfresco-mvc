package com.gradecak.alfresco.mvc.services.controller;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gradecak.alfresco.mvc.ControllerUtils;
import com.gradecak.alfresco.mvc.services.domain.Note;
import com.gradecak.alfresco.mvc.services.service.NoteService;

@Controller
@RequestMapping("/note")
public class NoteController {
  private final NoteService noteService;

  @Autowired
  public NoteController(NoteService noteService) {
    Assert.notNull(noteService);

    this.noteService = noteService;
  }

  @RequestMapping(value = "{noteId}", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> get(@PathVariable String noteId) {
    try {
      Note note = noteService.get(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, noteId));
      return ControllerUtils.toResponseEntity(note);
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{noteId}", method = { RequestMethod.DELETE })
  @ResponseBody
  public ResponseEntity<?> deleteNote(@PathVariable String noteId) {
    try {
      noteService.delete(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, noteId));
      return ControllerUtils.toEmptyResponseEntity();
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{noteId}", method = { RequestMethod.PUT })
  @ResponseBody
  public ResponseEntity<?> updateNote(@PathVariable String noteId, @RequestBody Note note) {
    try {
      Note updated = noteService.update(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, noteId), note.getHtmlContent());
      return ControllerUtils.toResponseEntity(updated);
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }
}
