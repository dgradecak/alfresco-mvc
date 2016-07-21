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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.gradecak.alfresco.querytemplate.PaginationParams.Direction;;

public class QueryTemplate {

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryTemplate.class);

  private int defaultMaxItems = 100;
  private int defaultPagesize = 20;
  private ServiceRegistry serviceRegistry;

  public QueryTemplate() {}

  public QueryTemplate(final ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }

  public QueryTemplate(final ServiceRegistry serviceRegistry, final int maxItems, final int defaultPagesize) {
    this.serviceRegistry = serviceRegistry;
    this.defaultMaxItems = maxItems;
    this.defaultPagesize = defaultPagesize;
  }

  public <T> T queryForObject(final NodeRef nodeRef, final NodePropertiesMapper<T> mapper) {
    Assert.notNull(mapper);
    Assert.notNull(nodeRef);

    Map<QName, Serializable> properties = serviceRegistry.getNodeService().getProperties(nodeRef);
    return mapper.mapNodeProperties(nodeRef, properties);
  }

  public <T> T queryForObject(final QueryBuilder query, final NodePropertiesMapper<T> mapper) {
    return queryForObject(query.build(), mapper, query.getLanguage());
  }

  public <T> T queryForObject(final String query, final NodePropertiesMapper<T> mapper, final String language) throws IncorrectResultSizeException {
    Assert.notNull(mapper);
    Assert.hasText(query);

    List<T> list = queryForList(query.toString(), mapper, 2, 0, 1, StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, language).dataList;
    int size = list.size();
    switch (size) {
    case 0:
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("No result found. Returning null.");
      }
      return null;
    case 1:
      return list.get(0);
    default:
      LOGGER.error(IncorrectResultSizeException.DEFAULT_MESSAGE_ID, size);
      throw new IncorrectResultSizeException(size);
    }
  }

  public <T> List<T> queryForList(final QueryBuilder query, final NodePropertiesMapper<T> mapper) {
    return queryForList(query.build(), mapper, defaultMaxItems, 0, defaultPagesize, StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, query.getLanguage()).dataList;
  }

  public <T> CountData<T> queryForList(final String query, final NodePropertiesMapper<T> mapper, final PaginationParams pagination, final StoreRef store,
      final String searchLanguage) {
    return queryForList(query, mapper, pagination.getLimit(), pagination.getPage(), pagination.getLimit(), pagination.getSort(), pagination.getDir(), store, searchLanguage, QueryConsistency.TRANSACTIONAL);
  }
  
  public <T> CountData<T> queryForList(final String query, final NodePropertiesMapper<T> mapper, final int maxItems, final int page, final int pageSize, final StoreRef store,
      final String searchLanguage) {
    return queryForList(query, mapper, maxItems, page, pageSize, store, searchLanguage, QueryConsistency.TRANSACTIONAL);
  }

  public <T> CountData<T> queryForList(final String query, final NodePropertiesMapper<T> mapper, final int maxItems, final int page, final int pageSize, final StoreRef store,
      final String searchLanguage, final QueryConsistency queryConsistency) {
    return queryForList(query, mapper, maxItems, page, pageSize, "@{http://www.alfresco.org/model/content/1.0}created", Direction.ASC, store, searchLanguage, queryConsistency);
  }
  
  public <T> CountData<T> queryForList(final String query, final NodePropertiesMapper<T> mapper, final int maxItems, final int page, final int pageSize, final String sort, final Direction dir, final StoreRef store,
      final String searchLanguage, final QueryConsistency queryConsistency) {
    Assert.notNull(query);
    Assert.notNull(mapper);
    Assert.isTrue(maxItems > 0, "maxItems must be a positive integer");
    Assert.notNull(store);
    Assert.notNull(queryConsistency);
    Assert.hasText(searchLanguage);

    SearchParameters sp = new SearchParameters();
    sp.addStore(store);
    sp.setLanguage(searchLanguage);
    sp.addSort(sort, Direction.ASC.equals(dir));
    // sp.setMaxItems(maxItems);
    sp.setLimit(maxItems);
    sp.setLimitBy(LimitBy.FINAL_SIZE);
    sp.setQuery(query.toString());
    // if (skipCount > 0) {
    // sp.setSkipCount(skipCount);
    // }

    return queryForList(sp, mapper, page, pageSize);
  }

  public <T> CountData<T> queryForList(final SearchParameters sp, final NodePropertiesMapper<T> mapper, final int page, final int pageSize) {
    Assert.notNull(sp);
    Assert.notNull(mapper);
    
    final int ps = pageSize > 0 ? pageSize : defaultPagesize;
    final int p = page > 1 ? page - 1 : 0; 

    List<T> list = new ArrayList<T>();
    ResultSet results = null;
    boolean hasMore = false;
    try {
      results = serviceRegistry.getSearchService().query(sp);
      if (results != null && results.length() != 0) {        
        final int startIndex =  (p * ps) -1;
        int count = 0;
        
        for (ResultSetRow resultSetRow : results) {

          if (count > startIndex && list.size() < ps) {
            NodeRef nodeRef = resultSetRow.getNodeRef();
            if (serviceRegistry.getNodeService().exists(nodeRef)) {
              if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Adding a row to the result list. NodeRef: " + nodeRef);
              }
              list.add(queryForObject(nodeRef, mapper));
            }
          }

          count++;
        }
        hasMore = results.getNumberFound() >= sp.getLimit() ;
      }
    } finally {
      if (results != null) {
        results.close();
      }
    }

    return new CountData<T>(new Long(results.length()), list, hasMore);
  }

  public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }

  public void setDefaultMaxItems(int maxItems) {
    this.defaultMaxItems = maxItems;
  }

  public void setDefaultPagesize(int defaultPagesize) {
    this.defaultPagesize = defaultPagesize;
  }
}
