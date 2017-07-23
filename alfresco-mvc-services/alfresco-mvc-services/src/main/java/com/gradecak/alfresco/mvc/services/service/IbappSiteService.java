package com.gradecak.alfresco.mvc.services.service;

import java.util.List;

import org.alfresco.repo.model.Repository;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.data.domain.StSite;
import com.gradecak.alfresco.mvc.data.mapper.StSiteMapper;
import com.gradecak.alfresco.querytemplate.QueryTemplate;
import com.gradecak.alfresco.mvc.services.domain.CmUser;

@Service
public class IbappSiteService {

  @Autowired
  private ServiceRegistry serviceRegistry;
  
  @Autowired
  private TenantUserService userService;
  
  @Autowired
  private QueryTemplate queryTemplate;

  @Autowired
  private StSiteMapper siteMapper;

  @AlfrescoTransaction(readOnly = true)
  public List<CmUser> managers(final NodeRef nodeRef) {
    String shortName = serviceRegistry.getSiteService().getSiteShortName(nodeRef);
    String siteRoleGroup = serviceRegistry.getSiteService().getSiteRoleGroup(shortName, SiteModel.SITE_MANAGER);
    return userService.getUsersFromGroup(siteRoleGroup, true);
  }

  @AlfrescoTransaction(readOnly = true)
  public List<CmUser> members(final NodeRef nodeRef) {
    String shortName = serviceRegistry.getSiteService().getSiteShortName(nodeRef);
    String siteRoleGroup = serviceRegistry.getSiteService().getSiteGroup(shortName);
    return userService.getUsersFromGroup(siteRoleGroup, false);
  }
  
  @AlfrescoTransaction(readOnly = true)
  public StSite get(final NodeRef nodeRef) {
    return queryTemplate.queryForObject(nodeRef, siteMapper);
  }
}
