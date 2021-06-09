package com.fintellix.framework.collaboration.dto;

import java.io.Serializable;

public class DocumentTemplate implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2088322405217805502L;
	private Integer templateId;
	private String templateName;
	private String templateDesc;
	private Integer isSecurityTemplate;
	public Integer getTemplateId() {
		return templateId;
	}
	
	public void setTemplateId(Integer templateId) {
		this.templateId = templateId;
	}
	
	public String getTemplateName() {
		return templateName;
	}
	
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	
	public String getTemplateDesc() {
		return templateDesc;
	}
	
	public void setTemplateDesc(String templateDesc) {
		this.templateDesc = templateDesc;
	}
	
	@Override
	public String toString() {
		return "DocumentTemplate [templateId=" + templateId + ", templateName=" + templateName + ", templateDesc="
				+ templateDesc + " ,isSecurityTemplate="+isSecurityTemplate+"]";
	}

	public Integer getIsSecurityTemplate() {
		return isSecurityTemplate;
	}

	public void setIsSecurityTemplate(Integer isSecurityTemplate) {
		this.isSecurityTemplate = isSecurityTemplate;
	}


}
