package com.gradecak.alfresco.mvc.services.controller;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.gradecak.alfresco.mvc.ControllerUtils;
import com.gradecak.alfresco.mvc.HateaosUtils;
import com.gradecak.alfresco.mvc.data.rest.resource.RootResourceInformation;

@Controller
@RequestMapping(FolderController.BASE_REQUEST_MAPPING)
public class FolderController extends AbstractController {

  private final ConversionService conversionService;

  @Autowired
  public FolderController(ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  public ConversionService getConversionService() {
    return conversionService;
  }

  @RequestMapping(value = BASE_MAPPING + "/{id}/list", method = RequestMethod.GET)
  public ResponseEntity<?> get(@PathVariable NodeRef id, RootResourceInformation resourceInformation, Pageable pageable) {
    if (!nodeExists(resourceInformation, id)) {
      return new ResponseEntity<Resource<?>>(HttpStatus.NOT_FOUND);
    }

    Page<Object> page = resourceInformation.getInvoker().list(resourceInformation, id, pageable);
    return ControllerUtils.toResponseEntity(HateaosUtils.createPagedResource(page));
  }
}
