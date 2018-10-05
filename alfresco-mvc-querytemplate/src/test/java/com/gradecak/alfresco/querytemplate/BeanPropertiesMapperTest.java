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

package com.gradecak.alfresco.querytemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class BeanPropertiesMapperTest {

  private static final NodeRef NODE_REF = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test_id");

  @Mock
  private NamespaceService namespaceService;

  @Mock
  private DictionaryService dictionaryService;

  @Mock
  private PropertyDefinition propertyDefinitionMock;

  private BeanPropertiesMapper<CmFolder> mapper;
  private BeanPropertiesMapperConfigurer<CmFolder> configurer;

  @Before
  public void before() throws Exception {
    doAnswer(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) {

        return NamespaceService.CONTENT_MODEL_1_0_URI;
      }
    }).when(namespaceService).getNamespaceURI("cm");

    doAnswer(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) {

        return "custom_uri";
      }
    }).when(namespaceService).getNamespaceURI("custom");

    doAnswer(new Answer<PropertyDefinition>() {
      @Override
      public PropertyDefinition answer(InvocationOnMock invocation) {

        return propertyDefinitionMock;
      }
    }).when(dictionaryService).getProperty(any());

    mapper = new CmFolderPropertiesMapper(namespaceService, dictionaryService, true) {};
  }

  @Test
  public void mapNodeProperties_withCorrectData() {
    CmFolder cmFolder = mapper.mapNodeProperties(NODE_REF, ImmutableMap.of(ContentModel.PROP_TITLE, "Test Title value", ContentModel.PROP_DESCRIPTION, "Test Description value"));
    assertEquals("Test Title value", cmFolder.getCmTitle());
    assertEquals("Test Description value", cmFolder.getCmDescription());
    assertEquals(NODE_REF.getId(), cmFolder.getRef());
    assertNull(cmFolder.getCustomNamespaceData());
  }

  @Test
  public void mapNodeProperties_withCustomNamespaceCorrectData() {
    Date testDate = new Date();
    CmFolder cmFolder = mapper.mapNodeProperties(NODE_REF, ImmutableMap.of(QName.createQName("custom_uri", "namespaceData"), testDate));
    assertNull(cmFolder.getCmDescription());
    assertNull(cmFolder.getCmTitle());
    assertEquals(NODE_REF.getId(), cmFolder.getRef());
    assertEquals(testDate, cmFolder.getCustomNamespaceData());
  }

  @Test(expected = NamespaceException.class)
  public void mapNodeProperties_withNonExistingProperty_reportException() {
    BeanPropertiesMapper<NonExistingNodeProperties> mapperWrong = new BeanPropertiesMapperBuilder<NonExistingNodeProperties>().mappedClass(NonExistingNodeProperties.class)
        .namespaceService(namespaceService).dictionaryService(dictionaryService).reportNamespaceException(true).build();
    mapperWrong.mapNodeProperties(NODE_REF, ImmutableMap.of());
  }

  @Test
  public void mapNodeProperties_withNonExistingProperty_doNotReportException() {
    BeanPropertiesMapper<NonExistingNodeProperties> mapperWrong = new BeanPropertiesMapperBuilder<NonExistingNodeProperties>().mappedClass(NonExistingNodeProperties.class)
        .namespaceService(namespaceService).dictionaryService(dictionaryService).build();
    NonExistingNodeProperties nonExistingNodeProperties = mapperWrong.mapNodeProperties(NODE_REF, ImmutableMap.of());
    assertNull(nonExistingNodeProperties.getNonexistingData());
  }

  @Test
  public void registry_withCorrectDataAndConfigurer() {
    Date testDate = new Date();
    BeanPropertiesMapper<CmFolder> mapper = new BeanPropertiesMapperRegistry(namespaceService, dictionaryService).addBeanPropertiesMapper(this.mapper).getForClass(CmFolder.class);
    CmFolder cmFolder = mapper.mapNodeProperties(NODE_REF,
        ImmutableMap.of(QName.createQName("custom_uri", "namespaceData"), testDate, ContentModel.PROP_TITLE, "Test Title value", ContentModel.PROP_DESCRIPTION, "Test Description value"));
    assertEquals("Test Title value", cmFolder.getCmTitle());
    assertEquals("Test Description value", cmFolder.getCmDescription());
    assertEquals(NODE_REF.getId(), cmFolder.getRef());
    assertEquals(testDate, cmFolder.getCustomNamespaceData());
  }

  @Test
  public void registry_withCorrectData() {
    Date testDate = new Date();
    BeanPropertiesMapper<CmFolder> mapper = new BeanPropertiesMapperRegistry(namespaceService, dictionaryService).getForClass(CmFolder.class);
    CmFolder cmFolder = mapper.mapNodeProperties(NODE_REF,
        ImmutableMap.of(QName.createQName("custom_uri", "namespaceData"), testDate, ContentModel.PROP_TITLE, "Test Title value", ContentModel.PROP_DESCRIPTION, "Test Description value"));
    assertEquals("Test Title value", cmFolder.getCmTitle());
    assertEquals("Test Description value", cmFolder.getCmDescription());
    assertNull(cmFolder.getRef());
    assertEquals(testDate, cmFolder.getCustomNamespaceData());
  }

  @Test(expected = RuntimeException.class)
  public void registry_doubleEntry() {
    BeanPropertiesMapperRegistry registry = new BeanPropertiesMapperRegistry(namespaceService, dictionaryService);

    BeanPropertiesMapper<NonExistingNodeProperties> mapper1 = new BeanPropertiesMapperBuilder<NonExistingNodeProperties>().mappedClass(NonExistingNodeProperties.class)
        .namespaceService(namespaceService).dictionaryService(dictionaryService).reportNamespaceException(false).build();

    BeanPropertiesMapper<NonExistingNodeProperties> mapper2 = new BeanPropertiesMapperBuilder<NonExistingNodeProperties>().mappedClass(NonExistingNodeProperties.class)
        .namespaceService(namespaceService).dictionaryService(dictionaryService).reportNamespaceException(false).build();

    registry.addBeanPropertiesMapper(mapper1);
    registry.addBeanPropertiesMapper(mapper2);
  }

  @Test
  public void registry_severalEntries() {
    BeanPropertiesMapperRegistry registry = new BeanPropertiesMapperRegistry(namespaceService, dictionaryService);

    BeanPropertiesMapper<NonExistingNodeProperties> mapper1 = new BeanPropertiesMapperBuilder<NonExistingNodeProperties>().mappedClass(NonExistingNodeProperties.class)
        .namespaceService(namespaceService).dictionaryService(dictionaryService).reportNamespaceException(false).build();

    BeanPropertiesMapper<CmFolder> mapper2 = new BeanPropertiesMapperBuilder<CmFolder>().mappedClass(CmFolder.class).namespaceService(namespaceService).dictionaryService(dictionaryService)
        .reportNamespaceException(false).build();

    registry.addBeanPropertiesMapper(mapper1);
    registry.addBeanPropertiesMapper(mapper2);
  }

  // TODO test mamper utilities (bean and node)
  @Test
  public void beanPropertiesMapperUtil_withCorrectdata() {
    BeanPropertiesMapperUtil beanPropertiesMapperUtil = new BeanPropertiesMapperUtil(new BeanPropertiesMapperRegistry(namespaceService, dictionaryService));

  }
}
