package com.gradecak.alfresco.mvc.data.controller;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import com.gradecak.alfresco.mvc.data.rest.resource.AlfrescoEntityInvoker;
import com.gradecak.alfresco.mvc.data.rest.resource.RootResourceInformation;

public abstract class AbstractController {

  public static final String BASE_REQUEST_MAPPING = "/node";

  public static final String BASE_MAPPING = "/{repository}";

  // @RequestMapping(value = BASE_MAPPING+"/search/{search}", method = { RequestMethod.GET })
  // public ResponseEntity<Resources<?>> search(RootResourceInformation resourceInformation, @PathVariable String
  // search) {
  //
  // CountData<?> documentList = documentInvoker.search(resourceInformation);
  // // return ResponseMapBuilderHelper.createResponseMap(documentList, true).build();
  //
  // return ControllerUtils.toResponseEntity(HateaosUtils.toResources(documentList.dataList, new
  // PageMetadata(documentList.count, 1, 100)));
  // }

  protected boolean nodeExists(RootResourceInformation resourceInformation, NodeRef id) {
    AlfrescoEntityInvoker invoker = resourceInformation.getInvoker();
    return invoker.exists(resourceInformation, id);
  }

  public ConversionService getConversionService() {
    return null;
  }

  @InitBinder
  protected void initBinder(final WebDataBinder binder) {
    ConversionService conversionService = getConversionService();
    if (conversionService != null) {
      binder.setConversionService(conversionService);
    }
  }
}
