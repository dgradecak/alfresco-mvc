package com.gradecak.alfresco.mvc.services.controller;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.ImmutableMap;
import com.gradecak.alfresco.mvc.ControllerUtils;
import com.gradecak.alfresco.mvc.data.controller.AbstractController;
import com.gradecak.alfresco.mvc.data.domain.StSite;
import com.gradecak.alfresco.mvc.data.rest.resource.RootResourceInformation;
import com.gradecak.alfresco.mvc.services.domain.CmUser;
import com.gradecak.alfresco.mvc.services.service.IbappSiteService;

@Controller
@RequestMapping(SiteController.BASE_REQUEST_MAPPING)
public class SiteController extends AbstractController {

  private final ConversionService conversionService;
  private final IbappSiteService siteService;

  @Autowired
  public SiteController(ConversionService conversionService, IbappSiteService siteService) {
    this.conversionService = conversionService;
    this.siteService = siteService;
  }

  public ConversionService getConversionService() {
    return conversionService;
  }

  @RequestMapping(value = BASE_MAPPING + "/{id}/members", method = RequestMethod.GET)
  public ResponseEntity<?> members(@PathVariable NodeRef id, RootResourceInformation resourceInformation) {
    if (!nodeExists(resourceInformation, id)) {
      return new ResponseEntity<Resource<?>>(HttpStatus.NOT_FOUND);
    }

    List<CmUser> members = siteService.members(id);
    return ControllerUtils.toResponseEntity(members);
  }

  @RequestMapping(value = BASE_MAPPING + "/{id}/profile", method = RequestMethod.GET)
  public ResponseEntity<?> managers(@PathVariable NodeRef id, RootResourceInformation resourceInformation, Pageable pageable) {
    if (!nodeExists(resourceInformation, id)) {
      return new ResponseEntity<Resource<?>>(HttpStatus.NOT_FOUND);
    }

    List<CmUser> managers = siteService.managers(id);    
    StSite stSite = siteService.get(id);
    return ControllerUtils.toResponseEntity(ImmutableMap.of("site", stSite, "managers", managers));
  }
}
