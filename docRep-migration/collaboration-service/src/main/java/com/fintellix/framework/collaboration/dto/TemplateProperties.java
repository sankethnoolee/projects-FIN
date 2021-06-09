package com.fintellix.framework.collaboration.dto;

import java.io.Serializable;

public class TemplateProperties implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2473040311876454142L;
	
	private Integer propertyId;
	private Integer templateId;
	private String propertyName;
	private String propertyDesc;
	private String propertyType;
	private Integer isMandatory;
	private Integer toShow;
	public Integer getPropertyId() {
		return propertyId;
	}
	
	public void setPropertyId(Integer propertyId) {
		this.propertyId = propertyId;
	}
	
	public Integer getTemplateId() {
		return templateId;
	}
	
	public void setTemplateId(Integer templateId) {
		this.templateId = templateId;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
	public String getPropertyDesc() {
		return propertyDesc;
	}
	
	public void setPropertyDesc(String propertyDesc) {
		this.propertyDesc = propertyDesc;
	}
	

	
	public Integer getToShow() {
		return toShow;
	}
	
	public void setToShow(Integer toShow) {
		this.toShow = toShow;
	}

	@Override
	public String toString() {
		return "TemplateProperties [propertyId=" + propertyId + ", templateId=" + templateId + ", propertyName="
				+ propertyName + ", propertyDesc=" + propertyDesc + ", isMandtory=" + getIsMandatory() + ", toShow=" + toShow
				+ "]";
	}

	public String getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(String propertyType) {
		this.propertyType = propertyType;
	}

	public Integer getIsMandatory() {
		return isMandatory;
	}

	public void setIsMandatory(Integer isMandatory) {
		this.isMandatory = isMandatory;
	}
		

}
