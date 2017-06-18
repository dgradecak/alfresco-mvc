package com.gradecak.alfresco.mvc.sample.domain;

import com.gradecak.alfresco.querytemplate.AbstractPersistable;

public class CmFolder extends AbstractPersistable {

	private String cmTitle;
	private String cmDescription;

	public String getCmTitle() {
		return cmTitle;
	}

	public void setCmTitle(String cmTitle) {
		this.cmTitle = cmTitle;
	}

	public String getCmDescription() {
		return cmDescription;
	}

	public void setCmDescription(String cmDescription) {
		this.cmDescription = cmDescription;
	}
}