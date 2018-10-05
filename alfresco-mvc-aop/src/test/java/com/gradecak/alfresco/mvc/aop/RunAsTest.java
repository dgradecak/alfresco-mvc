/**
 * Copyright gradecak.com

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradecak.alfresco.mvc.aop;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gradecak.alfresco.mvc.service.RunAsService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = { "classpath:test-aop-context.xml" })
public class RunAsTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Mock
  private MutableAuthenticationService authenticationService;

  @Mock
  private AuthorityService authorityService;

  @Mock
  private NodeService nodeService;

  @Autowired
  private ServiceRegistry serviceRegistry;

  @Autowired
  private RunAsService service;

  private AuthenticationUtil util = new AuthenticationUtil();
  private NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "aaa");

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(serviceRegistry.getAuthenticationService()).thenReturn(authenticationService);
    when(serviceRegistry.getAuthorityService()).thenReturn(authorityService);
    when(serviceRegistry.getNodeService()).thenReturn(nodeService);
    when(authenticationService.getCurrentTicket()).thenReturn("ticket");

    when(authorityService.hasGuestAuthority()).thenReturn(true);
    when(authorityService.hasAdminAuthority()).thenReturn(false);

    util.afterPropertiesSet();

    assertTrue(AopUtils.isAopProxy(service));
    AuthenticationUtil.clearCurrentSecurityContext();
  }

  @Test
  public void noAutehntication_runAsSystem() {
    service.getNamePropertyAsSystem(nodeRef);

    Assert.assertNull(AuthenticationUtil.getRunAsUser());
    Assert.assertNull(AuthenticationUtil.getFullyAuthenticatedUser());
  }

  @Test
  public void authentifiedAsTest_runAsUser() {

    AuthenticationUtil.setFullyAuthenticatedUser("test");
    when(authorityService.hasGuestAuthority()).thenReturn(false);
    service.getNamePropertyAsUser(nodeRef);
    Assert.assertEquals("test", AuthenticationUtil.getRunAsUser());
    Assert.assertEquals("test", AuthenticationUtil.getFullyAuthenticatedUser());
  }
}