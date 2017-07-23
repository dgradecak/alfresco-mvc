package com.gradecak.alfresco.mvc.data.rest.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQueryFactory;
import org.alfresco.repo.security.permissions.PermissionCheckedCollection.PermissionCheckedCollectionMixin;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.stereotype.Service;

import com.gradecak.alfresco.mvc.data.mapper.BeanEntityMapper;
import com.gradecak.alfresco.mvc.data.support.AlfrescoNodeConfiguration;
import com.gradecak.alfresco.querytemplate.AbstractPersistable;
import com.gradecak.alfresco.querytemplate.NodePropertiesMapper;

@Service
public class AlfrescoMvcCannedQueryService {

  @Autowired
  private ServiceRegistry serviceRegistry;

  @Autowired
  @Qualifier("fileFolderCannedQueryRegistry")
  private NamedObjectRegistry<CannedQueryFactory<NodeRef>> cannedQueryRegistry;
  
  @Autowired
  private AlfrescoNodeConfiguration alfrescoNodeConfiguration;
  
  public <T extends Persistable<NodeRef>> Page<T> cannedQuery(final NodeRef parentRef, final List<FilterProp> filterProps, final List<Pair<QName, Boolean>> sortProps, final Pageable pageable,
      final NodePropertiesMapper<T> defaultMapper, final Set<QName> assocTypes, final Set<QName> contentTypes) {
    
    PageRequest pr = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
    PagingRequest pageRequest = new PagingRequest(pr.getOffset(), pr.getPageSize());
    pageRequest.setRequestTotalCountMax(pr.getPageSize() * 2);
    
    GetChildrenCannedQueryFactory getChildrenCannedQueryFactory = (GetChildrenCannedQueryFactory) cannedQueryRegistry.getNamedObject("fileFolderGetChildrenCannedQueryFactory");

    GetChildrenCannedQuery cq = (GetChildrenCannedQuery) getChildrenCannedQueryFactory.getCannedQuery(parentRef, null, assocTypes, contentTypes, filterProps, sortProps, pageRequest);

    // execute canned query
    CannedQueryResults<NodeRef> results = cq.execute();
    PagingResults<FileInfo> pageOfNodeInfos = getPagingResults(pageRequest, results);

    List<FileInfo> nodeInfos = pageOfNodeInfos.getPage();
    Pair<Integer, Integer> totalResultCount = pageOfNodeInfos.getTotalResultCount();

    List<T> list = new ArrayList<T>(nodeInfos.size());
    for (FileInfo fileInfo : nodeInfos) {
      Map<QName, Serializable> properties = fileInfo.getProperties();
      
      QName type = fileInfo.getType();
      NodePropertiesMapper<T> bestMatchMapper = findBestMatchMapperOrDefault(type, defaultMapper);
      T mapNodeProperties = bestMatchMapper.mapNodeProperties(fileInfo.getNodeRef(), properties);
      list.add(mapNodeProperties);
    }

    return new PageImpl<T>(list, pageable, totalResultCount.getFirst().longValue());
  }
  
  private <T extends Persistable<NodeRef>> NodePropertiesMapper<T> findBestMatchMapperOrDefault(QName type, NodePropertiesMapper<T> defaultMapper) {
    BeanEntityMapper<T> bestMatch = alfrescoNodeConfiguration.findBestMatch(type);
    if(bestMatch == null) {
      return defaultMapper;
    }
    
    return bestMatch;
  }

  private PagingResults<FileInfo> getPagingResults(final PagingRequest pagingRequest, final CannedQueryResults<NodeRef> results) {
    List<NodeRef> nodeRefs = null;
    if (results.getPageCount() > 0) {
      nodeRefs = results.getPages().get(0);
    } else {
      nodeRefs = Collections.emptyList();
    }

    // set total count
    final Pair<Integer, Integer> totalCount;
    if (pagingRequest.getRequestTotalCountMax() > 0) {
      totalCount = results.getTotalResultCount();
    } else {
      totalCount = null;
    }

    final List<FileInfo> nodeInfos = new ArrayList<FileInfo>(nodeRefs.size());
    for (NodeRef nodeRef : nodeRefs) {
      nodeInfos.add(serviceRegistry.getFileFolderService().getFileInfo(nodeRef));
    }
    PermissionCheckedCollectionMixin.create(nodeInfos, nodeRefs);

    return new PagingResults<FileInfo>() {
      @Override
      public String getQueryExecutionId() {
        return results.getQueryExecutionId();
      }

      @Override
      public List<FileInfo> getPage() {
        return nodeInfos;
      }

      @Override
      public boolean hasMoreItems() {
        return results.hasMoreItems();
      }

      @Override
      public Pair<Integer, Integer> getTotalResultCount() {
        return totalCount;
      }
    };
  }
}
