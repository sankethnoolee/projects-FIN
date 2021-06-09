package com.fintellix.framework.collaboration.dto;

import java.io.Serializable;

public class DirectoryTemplateLink implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String directoryId;
	private Integer templateId;
	private Integer isSecurityTemplate;
	public String getDirectoryId() {
		return directoryId;
	}
	
	public void setDirectoryId(String directoryId) {
		this.directoryId = directoryId;
	}
	
	public Integer getTemplateId() {
		return templateId;
	}
	
	public void setTemplateId(Integer templateId) {
		this.templateId = templateId;
	}
	
	public Integer getIsSecurityTemplate() {
		return isSecurityTemplate;
	}
	
	public void setIsSecurityTemplate(Integer isSecurityTemplate) {
		this.isSecurityTemplate = isSecurityTemplate;
	}

	@Override
	public String toString() {
		return "DirectoryTemplateLink [directoryId=" + directoryId + ", templateId=" + templateId
				+ ", isSecurityTemplate=" + isSecurityTemplate + "]";
	}
	
	
	

}
