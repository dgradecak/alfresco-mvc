package com.gradecak.alfresco.querytemplate;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;

public class CmFolderPropertiesMapper extends BeanPropertiesMapper<CmFolder>
		implements BeanPropertiesMapperConfigurer<CmFolder> {

	public CmFolderPropertiesMapper(final NamespaceService namespaceService, final DictionaryService dictionaryService,
			final boolean reportNamespaceException) {
		super(namespaceService, dictionaryService, reportNamespaceException);
	}

	public void configure(NodeRef nodeRef, CmFolder mappedObject) {
		mappedObject.setRef(nodeRef.getId());
	};
}
