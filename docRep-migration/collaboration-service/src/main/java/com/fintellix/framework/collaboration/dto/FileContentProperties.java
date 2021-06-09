package com.fintellix.framework.collaboration.dto;

import java.io.Serializable;

public class FileContentProperties implements Serializable{
		
	
	private static final long serialVersionUID = 1L;
	
	private String fileName;
	private Integer solutionId;
	private Integer orgId;
	private String packageLocation;
	private String propertyName;
	private String propertyValue;
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public Integer getSolutionId() {
		return solutionId;
	}
	
	public void setSolutionId(Integer solutionId) {
		this.solutionId = solutionId;
	}
	
	public Integer getOrgId() {
		return orgId;
	}
	
	public void setOrgId(Integer orgId) {
		this.orgId = orgId;
	}
	
	public String getPackageLocation() {
		return packageLocation;
	}
	
	public void setPackageLocation(String packageLocation) {
		this.packageLocation = packageLocation;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
	public String getPropertyValue() {
		return propertyValue;
	}
	
	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}
	
	
	

}
