package com.fintellix.framework.collaboration.dto;

import java.io.Serializable;

public class ContentProperties implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String contentId;
	private String contentType;
	private Integer versionNumber;
	private Integer propertyId;
	private String propertyValue;
	private Integer templateId;
	private Integer isSecurityTemplate;
	private String propertyDataType;
	private Integer isMandatory;
	private Integer visibility;
	public String getContentId() {
		return contentId;
	}
	
	public void setContentId(String contentId) {
		this.contentId = contentId;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public Integer getVersionNumber() {
		return versionNumber;
	}
	
	public void setVersionNumber(Integer versionNumber) {
		this.versionNumber = versionNumber;
	}
	
	public Integer getPropertyId() {
		return propertyId;
	}
	
	public void setPropertyId(Integer propertyId) {
		this.propertyId = propertyId;
	}
	
	public String getPropertyValue() {
		return propertyValue;
	}
	
	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
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
	
	public String getPropertyDataType() {
		return propertyDataType;
	}
	
	public void setPropertyDataType(String propertyDataType) {
		this.propertyDataType = propertyDataType;
	}
	
	public Integer getIsMandatory() {
		return isMandatory;
	}
	
	public void setIsMandatory(Integer isMandatory) {
		this.isMandatory = isMandatory;
	}
	
	public Integer getVisibility() {
		return visibility;
	}
	
	public void setVisibility(Integer visibility) {
		this.visibility = visibility;
	}


	@Override
	public String toString() {
		return "ContentProperties [contentId=" + contentId + ", contentType=" + contentType + ", versionNumber="
				+ versionNumber + ", propertyId=" + propertyId + ", propertyValue=" + propertyValue + ", templateId="
				+ templateId + ", isSecurityTemplate=" + isSecurityTemplate + ", propertyDataType=" + propertyDataType
				+ ", isMandatory=" + isMandatory + ", visibility=" + visibility + "]";
	}
	

}
