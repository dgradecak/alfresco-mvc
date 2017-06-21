package com.gradecak.alfresco.mvc.data.support;

import java.io.InputStream;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.gradecak.alfresco.mvc.data.mapper.BeanEntityMapper;

@NoRepositoryBean
public interface AlfrescoNodeRepository<T extends Persistable<NodeRef>> extends PagingAndSortingRepository<T, NodeRef> {

  BeanEntityMapper<T> getBeanEntityMapper();

  <S extends T> S save(NodeRef parentRef, S entity, QName type, InputStream is);
}
