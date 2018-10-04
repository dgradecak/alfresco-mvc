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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.transaction.SystemException;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
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

import com.gradecak.alfresco.mvc.service.TransactionalService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = { "classpath:test-aop-context.xml" })
public class TransactionalTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Mock
  private RetryingTransactionHelper retryingTransactionHelper;

  @Autowired
  private ServiceRegistry serviceRegistry;

  @Autowired
  private TransactionalService service;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(serviceRegistry.getRetryingTransactionHelper()).thenReturn(retryingTransactionHelper);

    assertTrue(AopUtils.isAopProxy(service));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void txRWriteWithPropagationRequired() throws SystemException {
    service.transactionWriteWithoutPropagation();
    verify(retryingTransactionHelper).doInTransaction(any(RetryingTransactionCallback.class), eq(false), eq(false));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void txReadOnlyWithPropagationRequired() throws SystemException {
    service.transactioReadOnlyWithPropagationRequired();
    verify(retryingTransactionHelper).doInTransaction(any(RetryingTransactionCallback.class), eq(true), eq(false));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void txReadOnlyWithPropagationRequiresNew() throws SystemException {
    service.transactioReadOnlyWithPropagationRequiresNew();
    verify(retryingTransactionHelper).doInTransaction(any(RetryingTransactionCallback.class), eq(true), eq(true));
  }
}