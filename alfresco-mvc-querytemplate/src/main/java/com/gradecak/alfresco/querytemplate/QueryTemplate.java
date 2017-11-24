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
import java.util.HashMap;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.util.Assert;

/**
 * 
 * This class is thread safe and it is advised to use it as singleton.
 */
public class QueryTemplate {

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryTemplate.class);

  private final int defaultMaxItems;
  private final int defaultPagesize;
  private final ServiceRegistry serviceRegistry;

  @Autowired
  public QueryTemplate(final ServiceRegistry serviceRegistry) {
    this(serviceRegistry, 100, 20);
  }
  
  @Autowired
  public QueryTemplate(final ServiceRegistry serviceRegistry, final int maxItems, final int pagesize) {
    this.serviceRegistry = serviceRegistry;
    this.defaultMaxItems = maxItems;
    this.defaultPagesize = pagesize;
  }

  public <T extends Persistable<NodeRef>> T queryForObject(final NodeRef nodeRef, final NodePropertiesMapper<T> mapper) {
    Assert.notNull(mapper);
    Assert.notNull(nodeRef);

    Map<QName, Serializable> properties = serviceRegistry.getNodeService().getProperties(nodeRef);
    return mapper.mapNodeProperties(nodeRef, properties);
  }

  public <T extends Persistable<NodeRef>> T queryForObject(final QueryBuilder query, final NodePropertiesMapper<T> mapper) {
    return queryForObject(query.build(), mapper, query.getLanguage());
  }

  public <T extends Persistable<NodeRef>> T queryForObject(final String query, final NodePropertiesMapper<T> mapper, final String language) throws IncorrectResultSizeException {
    Assert.notNull(mapper);
    Assert.hasText(query);

    List<T> list = queryForList(query.toString(), mapper, 2, 0, 1, StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, language).getContent();
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

  public <T extends Persistable<NodeRef>> Page<T> queryForList(final QueryBuilder query, final NodePropertiesMapper<T> mapper) {
    return queryForList(query.build(), mapper, defaultMaxItems, 0, defaultPagesize, StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, query.getLanguage());
  }

  public <T extends Persistable<NodeRef>> Page<T> queryForList(final String query, final NodePropertiesMapper<T> mapper, final int maxItems, final int page, final int pageSize, final StoreRef store,
      final String searchLanguage) {
    return queryForList(query, mapper, maxItems, page, pageSize, store, searchLanguage, QueryConsistency.TRANSACTIONAL);
  }

  public <T extends Persistable<NodeRef>> Page<T> queryForList(final String query, final NodePropertiesMapper<T> mapper, final int maxItems, final int page, final int pageSize, final StoreRef store,
      final String searchLanguage, final QueryConsistency queryConsistency) {
	
	PageRequest pageable = new PageRequest(page, pageSize, new Sort(Direction.ASC, "@{http://www.alfresco.org/model/content/1.0}created"));
    return queryForList(query, mapper, pageable, store, searchLanguage, queryConsistency);
  }

  public <T extends Persistable<NodeRef>> Page<T> queryForList(final String query, final NodePropertiesMapper<T> mapper, final Pageable pageable, final StoreRef store, final String searchLanguage, final QueryConsistency queryConsistency) {
    Assert.notNull(query);
    Assert.notNull(mapper);
    Assert.notNull(store);
    Assert.notNull(pageable);
    Assert.notNull(queryConsistency);
    Assert.hasText(searchLanguage);
  
    SearchParameters sp = new SearchParameters();
    sp.addStore(store);
    sp.setLanguage(searchLanguage);
    
    Sort sort = pageable.getSort();
    if(sort != null) {
      for (Order order : sort) {
    	  sp.addSort(order.getProperty(), order.isAscending());  	
    }
    }
    
    // sp.setMaxItems(maxItems);
    sp.setLimit(pageable.getPageSize());
    sp.setLimitBy(LimitBy.FINAL_SIZE);
    sp.setQuery(query.toString());
    // if (skipCount > 0) {
    // sp.setSkipCount(skipCount);
    // }
  
    return queryForList(sp, mapper, pageable);
  }

  public <T extends Persistable<NodeRef>> Page<T> queryForList(final SearchParameters sp, final NodePropertiesMapper<T> mapper, final Pageable pageable) {
    Assert.notNull(sp);
    Assert.notNull(mapper);
  
    final int ps = pageable.getPageSize() > 0 ? pageable.getPageSize() : defaultPagesize;
    final int p = pageable.getPageNumber() > 1 ? pageable.getPageNumber() - 1 : 0;
  
    List<T> list = new ArrayList<T>();
    ResultSet results = null;
    try {
      results = serviceRegistry.getSearchService().query(sp);
      if (results != null && results.length() != 0) {
        final int startIndex = (p * ps) - 1;
        int count = 0;
  
        for (ResultSetRow resultSetRow : results) {
  
          if (count > startIndex && list.size() < ps) {
            NodeRef nodeRef = resultSetRow.getNodeRef();
            if (serviceRegistry.getNodeService().exists(nodeRef)) {
              Map<QName, Serializable> properties = new HashMap<>();
              for (Map.Entry<String, Serializable> entry : resultSetRow.getValues().entrySet()) {
                properties.put(QName.createQName(entry.getKey()), entry.getValue());
              }
              list.add(mapper.mapNodeProperties(nodeRef, properties));
            }
          }
  
          count++;
        }
      }
    } finally {
      if (results != null) {
        results.close();
      }
    }

    return new PageImpl<T>(list, pageable, results.getNumberFound());
  }
}
