package com.gradecak.alfresco.mvc.services.service;

import java.util.Set;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.services.SecurityProvider;
import com.gradecak.alfresco.mvc.services.SecurityProvider.SecurityCompanyRole;
import com.gradecak.alfresco.mvc.services.model.IbappModel;

@Service
public class IbappTenantService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IbappTenantService.class);

  @Autowired
  private ServiceRegistry serviceRegistry;

  @AlfrescoTransaction(readOnly=true)
  public String getCurrentlyLoggedTenant() {
    return getCurrentlyLogged().getCompany();
  }
  
  @AlfrescoTransaction(readOnly=true)
  public SecurityCompanyRole getCurrentlyLogged() {
    Set<String> authorities = serviceRegistry.getAuthorityService().getAuthorities();
    SecurityCompanyRole companyRole = SecurityProvider.getAllowedCompanyRole(authorities);

    return companyRole;
  }
  
  public String checkTenantAccess(NodeRef nodeRef) {
    Assert.notNull(nodeRef);
    
    String tenant = getCurrentlyLoggedTenant();
    Assert.hasText(tenant);
    
    if (AccessStatus.ALLOWED.equals(serviceRegistry.getPermissionService().hasPermission(nodeRef, PermissionService.READ))) {
    	return tenant;
    }

    String t = (String) serviceRegistry.getNodeService().getProperty(nodeRef, IbappModel.PROP_TENANT);
    if (!tenant.equals(t)) {
      throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
    }

    return tenant;
  }
}