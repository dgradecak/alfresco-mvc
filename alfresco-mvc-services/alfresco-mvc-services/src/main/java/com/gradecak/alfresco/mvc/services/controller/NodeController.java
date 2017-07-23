package com.gradecak.alfresco.mvc.services.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Persistable;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.gradecak.alfresco.mvc.ControllerUtils;
import com.gradecak.alfresco.mvc.HateaosUtils;
import com.gradecak.alfresco.mvc.data.domain.CmFolder;
import com.gradecak.alfresco.mvc.data.rest.resource.AlfrescoEntityResource;
import com.gradecak.alfresco.mvc.data.rest.resource.RootResourceInformation;

@Controller
@RequestMapping(NodeController.BASE_REQUEST_MAPPING)
public class NodeController extends AbstractController {

  private final ConversionService conversionService;

  @Autowired
  public NodeController(ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  public ConversionService getConversionService() {
    return conversionService;
  }

  @RequestMapping(value = BASE_MAPPING + "/{id}", method = RequestMethod.GET)
  public ResponseEntity<Resource<?>> get(@PathVariable NodeRef id, RootResourceInformation resourceInformation) {
    if (!nodeExists(resourceInformation, id)) {
      return new ResponseEntity<Resource<?>>(HttpStatus.NOT_FOUND);
    }

    Object resource = resourceInformation.getInvoker().get(resourceInformation, id);
    return ControllerUtils.toResponseEntity(HateaosUtils.toResource(resource));
  }

  @RequestMapping(value = BASE_MAPPING + "/{id}/breadcrumb", method = RequestMethod.GET)
  public ResponseEntity<?> breadcrumb(@PathVariable NodeRef id, RootResourceInformation resourceInformation) {
    if (!nodeExists(resourceInformation, id)) {
      return new ResponseEntity<Resource<?>>(HttpStatus.NOT_FOUND);
    }

    List<CmFolder> breadcrumb = resourceInformation.getInvoker().breadcrumb(resourceInformation, id);
    return ControllerUtils.toResponseEntity(breadcrumb);
  }

  @RequestMapping(value = BASE_MAPPING + "/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> delete(@PathVariable NodeRef id, RootResourceInformation resourceInformation) {
    if (!nodeExists(resourceInformation, id)) {
      return new ResponseEntity<Resource<?>>(HttpStatus.NOT_FOUND);
    }

    resourceInformation.getInvoker().delete(resourceInformation, id);
    return ControllerUtils.toEmptyResponseEntity();
  }

  @RequestMapping(value = BASE_MAPPING + "/{id}", method = RequestMethod.PUT)
  public ResponseEntity<? extends ResourceSupport> put(@PathVariable NodeRef id, RootResourceInformation resourceInformation, AlfrescoEntityResource<Persistable<NodeRef>> resource) {
    if (!nodeExists(resourceInformation, id)) {
      return new ResponseEntity<Resource<?>>(HttpStatus.NOT_FOUND);
    }

    Object updated = resourceInformation.getInvoker().save(resourceInformation, id, resource, null);
    return ControllerUtils.toResponseEntity(HateaosUtils.toResource(updated));
  } 

  @RequestMapping(value = BASE_MAPPING + "/{id}", method = RequestMethod.POST)
  public ResponseEntity<? extends ResourceSupport> post(@PathVariable NodeRef id, RootResourceInformation resourceInformation, AlfrescoEntityResource<Persistable<NodeRef>> resource) {
    if (!nodeExists(resourceInformation, id)) {
      return new ResponseEntity<Resource<?>>(HttpStatus.NOT_FOUND);
    }

    Object updated = resourceInformation.getInvoker().save(resourceInformation, resource.getContent().getId(), resource, id);
    return ControllerUtils.toResponseEntity(HateaosUtils.toResource(updated));
  }

  @RequestMapping(value = BASE_MAPPING, method = RequestMethod.POST)
  public ResponseEntity<? extends ResourceSupport> post(RootResourceInformation resourceInformation, AlfrescoEntityResource<Persistable<NodeRef>> resource) {
    Object updated = resourceInformation.getInvoker().save(resourceInformation, null, resource, null);
    return ControllerUtils.toResponseEntity(HateaosUtils.toResource(updated));
  }
}
