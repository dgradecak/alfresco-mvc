package com.gradecak.alfresco.mvc.sample.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.gradecak.alfresco.mvc.data.repository.CmDocumentRepository;
import com.gradecak.alfresco.mvc.data.repository.CmFolderRepository;
import com.gradecak.alfresco.mvc.sample.domain.CmFolder;
import com.gradecak.alfresco.mvc.sample.service.AlfrescoDataService;

/**
 * shows how to use the Alfresco @MVC framework
 */

@Controller
@RequestMapping("/data")
public class AlfrescoMvcDataController {

  private final AlfrescoDataService service;

  @Autowired
  private CmDocumentRepository documentRepository;
  
  @Autowired
  private CmFolderRepository folderRepository;

  @Autowired
  public AlfrescoMvcDataController(final AlfrescoDataService service) {
    this.service = service;
  }

  @RequestMapping(value = "document", method = { RequestMethod.GET })
  public ResponseEntity<?> document() throws IOException {
    CmFolder companyHomeFolder = service.getCompanyHomeFolder();
    documentRepository.exists(companyHomeFolder.getId());
    return new ResponseEntity<>(service.getCompanyHomeFolder(), HttpStatus.OK);
  }
  
  @RequestMapping(value = "folder", method = { RequestMethod.GET })
  public ResponseEntity<?> folder() throws IOException {
    CmFolder companyHomeFolder = service.getCompanyHomeFolder();
    folderRepository.exists(companyHomeFolder.getId());
    return new ResponseEntity<>(service.getCompanyHomeFolder(), HttpStatus.OK);
  }
}
