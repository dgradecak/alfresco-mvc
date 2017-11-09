package com.gradecak.alfresco.querytemplate.mapper;

import org.alfresco.service.ServiceRegistry;

import com.gradecak.alfresco.querytemplate.BeanPropertiesMapper;
import com.gradecak.alfresco.querytemplate.domain.CmFolder;

public class CmFolderPropertiesMapper extends BeanPropertiesMapper<CmFolder> {

	public CmFolderPropertiesMapper(ServiceRegistry serviceRegistry) {
		super(serviceRegistry);
	}

	@Override
	protected void configureMappedObject(CmFolder mappedObject) {
	}
}
