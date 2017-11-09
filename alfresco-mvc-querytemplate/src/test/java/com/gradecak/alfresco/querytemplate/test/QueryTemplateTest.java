package com.gradecak.alfresco.querytemplate.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;

import com.gradecak.alfresco.querytemplate.QueryBuilder;
import com.gradecak.alfresco.querytemplate.QueryTemplate;
import com.gradecak.alfresco.querytemplate.domain.CmFolder;
import com.gradecak.alfresco.querytemplate.mapper.CmFolderPropertiesMapper;

public class QueryTemplateTest {

  private static final String QNAME_PATTERN = "{%s}%s";
  private ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
  private QueryTemplate queryTemplate;
  private CmFolderPropertiesMapper propertiesMapper;
  private NodeService nodeService = mock(NodeService.class);
  private NamespaceService namespaceService = mock(NamespaceService.class);
  private SearchService searchService = mock(SearchService.class);

  @Before
  public void setUp() {
    when(serviceRegistry.getNamespaceService()).thenReturn(namespaceService);
    when(serviceRegistry.getDictionaryService()).thenReturn(mock(DictionaryService.class));
    when(serviceRegistry.getNodeService()).thenReturn(nodeService);
    when(serviceRegistry.getSearchService()).thenReturn(searchService);
    when(namespaceService.getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX)).thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);
    when(namespaceService.getNamespaceURI(NamespaceService.SYSTEM_MODEL_PREFIX)).thenReturn(NamespaceService.SYSTEM_MODEL_1_0_URI);
    when(namespaceService.getNamespaceURI(NamespaceService.APP_MODEL_PREFIX)).thenReturn(NamespaceService.APP_MODEL_1_0_URI);
    when(serviceRegistry.getDictionaryService().getProperty(Mockito.any(QName.class))).thenReturn(mock(PropertyDefinition.class));
    queryTemplate = new QueryTemplate(serviceRegistry);
    propertiesMapper = new CmFolderPropertiesMapper(serviceRegistry);
  }

  @Test
  public void queryForObject_shouldReturnCmFolderObject() {
    NodeRef mockNodeRef = new NodeRef("Mock", "Mock", "Mock");
    Date createdDate = new Date();
    Map<QName, Serializable> propertiesMap = sampleFolderPropertiesMap(createdDate, 0);
    when(nodeService.getProperties(mockNodeRef)).thenReturn(propertiesMap);
    CmFolder folder = queryTemplate.queryForObject(mockNodeRef, propertiesMapper);
    assertFolderProperties(folder, createdDate, 0);
  }

  @Test
  public void queryForList_shouldReturnPageOfThreeCmFolderObject() {
    QueryBuilder queryBuilder = mock(QueryBuilder.class);
    ResultSet resultSet = mock(ResultSet.class);
    ResultSetRow resultSetRow1 = mock(ResultSetRow.class);
    ResultSetRow resultSetRow2 = mock(ResultSetRow.class);
    ResultSetRow resultSetRow3 = mock(ResultSetRow.class);
    NodeRef mockNodeRef1 = new NodeRef("Mock1", "Mock1", "Mock1");
    NodeRef mockNodeRef2 = new NodeRef("Mock2", "Mock2", "Mock2");
    NodeRef mockNodeRef3 = new NodeRef("Mock3", "Mock3", "Mock3");
    Date createdDate = new Date();
    Map<String, Serializable> resultSetValuesMap1 = createResultSetValuesMap(createdDate, 0);
    Map<String, Serializable> resultSetValuesMap2 = createResultSetValuesMap(createdDate, 1);
    Map<String, Serializable> resultSetValuesMap3 = createResultSetValuesMap(createdDate, 2);

    when(queryBuilder.build()).thenReturn("QUERY");
    when(queryBuilder.getLanguage()).thenReturn(SearchService.LANGUAGE_LUCENE);
    when(searchService.query(Mockito.any(SearchParameters.class))).thenReturn(resultSet);
    when(resultSet.length()).thenReturn(3);
    when(resultSet.iterator()).thenReturn(Arrays.asList(resultSetRow1, resultSetRow2, resultSetRow3).iterator());
    when(resultSetRow1.getNodeRef()).thenReturn(mockNodeRef1);
    when(resultSetRow2.getNodeRef()).thenReturn(mockNodeRef2);
    when(resultSetRow3.getNodeRef()).thenReturn(mockNodeRef3);
    when(serviceRegistry.getNodeService().exists(Mockito.any(NodeRef.class))).thenReturn(true);
    when(resultSetRow1.getValues()).thenReturn(resultSetValuesMap1);
    when(resultSetRow2.getValues()).thenReturn(resultSetValuesMap2);
    when(resultSetRow3.getValues()).thenReturn(resultSetValuesMap3);

    Page<CmFolder> page = queryTemplate.queryForList(queryBuilder, propertiesMapper);
    assertEquals(page.getNumberOfElements(), 3);
    for (int i = 0; i < page.getNumberOfElements() - 1; i++) {
      CmFolder folder = page.getContent().get(i);
      assertFolderProperties(folder, createdDate, i);
    }
  }

  private void assertFolderProperties(CmFolder folder, Date date, int i) {
    assertNotNull(folder);
    assertEquals(folder.getAppIcon(), "icon" + i);
    assertEquals(folder.getCmCreated(), date);
    assertEquals(folder.getCmCreator(), "creator" + i);
    assertEquals(folder.getCmDescription(), "desription" + i);
    assertEquals(folder.getCmModified(), date);
    assertEquals(folder.getCmModifier(), "modifier" + i);
    assertEquals(folder.getSysLocale(), "locale" + i);
  }

  private Map<String, Serializable> createResultSetValuesMap(Date date, int i) {
    Map<String, Serializable> resultSetValues = new HashMap<>();
    resultSetValues.put(String.format(QNAME_PATTERN, NamespaceService.CONTENT_MODEL_1_0_URI, "created"), date);
    resultSetValues.put(String.format(QNAME_PATTERN, NamespaceService.CONTENT_MODEL_1_0_URI, "creator"), "creator" + i);
    resultSetValues.put(String.format(QNAME_PATTERN, NamespaceService.CONTENT_MODEL_1_0_URI, "modifier"), "modifier" + i);
    resultSetValues.put(String.format(QNAME_PATTERN, NamespaceService.CONTENT_MODEL_1_0_URI, "modified"), date);
    resultSetValues.put(String.format(QNAME_PATTERN, NamespaceService.CONTENT_MODEL_1_0_URI, "name"), "name" + i);
    resultSetValues.put(String.format(QNAME_PATTERN, NamespaceService.CONTENT_MODEL_1_0_URI, "title"), "title" + i);
    resultSetValues.put(String.format(QNAME_PATTERN, NamespaceService.CONTENT_MODEL_1_0_URI, "description"), "desription" + i);
    resultSetValues.put(String.format(QNAME_PATTERN, NamespaceService.SYSTEM_MODEL_1_0_URI, "locale"), "locale" + i);
    resultSetValues.put(String.format(QNAME_PATTERN, NamespaceService.APP_MODEL_1_0_URI, "icon"), "icon" + i);
    return resultSetValues;
  }

  private Map<QName, Serializable> sampleFolderPropertiesMap(Date date, int i) {
    Map<QName, Serializable> folderProperties = new HashMap<>();
    folderProperties.put(QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "created", namespaceService), date);
    folderProperties.put(QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "creator", namespaceService), "creator" + i);
    folderProperties.put(QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "modifier", namespaceService), "modifier" + i);
    folderProperties.put(QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "modified", namespaceService), date);
    folderProperties.put(QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "name", namespaceService), "name" + i);
    folderProperties.put(QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "title", namespaceService), "title" + i);
    folderProperties.put(QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "description", namespaceService), "desription" + i);
    folderProperties.put(QName.createQName(NamespaceService.SYSTEM_MODEL_PREFIX, "locale", namespaceService), "locale" + i);
    folderProperties.put(QName.createQName(NamespaceService.APP_MODEL_PREFIX, "icon", namespaceService), "icon" + i);
    return folderProperties;
  }
}
