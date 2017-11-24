package com.gradecak.alfresco.mvc.sample.mapper;

import org.alfresco.service.ServiceRegistry;

import com.gradecak.alfresco.mvc.sample.domain.CmFolder;
import com.gradecak.alfresco.querytemplate.BeanPropertiesMapper;

public class CmFolderPropertiesMapper extends BeanPropertiesMapper<CmFolder> {

	public CmFolderPropertiesMapper(ServiceRegistry serviceRegistry) {
		super(serviceRegistry);
	}

	@Override
	protected void configureMappedObject(CmFolder mappedObject) {
	}
}
