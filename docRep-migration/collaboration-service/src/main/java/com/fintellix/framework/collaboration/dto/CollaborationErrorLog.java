package com.fintellix.framework.collaboration.dto;

import java.io.Serializable;

public class CollaborationErrorLog implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6027217038980528371L;
	
	private String entityName;
	private String sheetName;
	private String errorMessage;
	private String errorType;
	public String getEntityName() {
		return entityName;
	}
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	public String getSheetName() {
		return sheetName;
	}
	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getErrorType() {
		return errorType;
	}
	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((entityName == null) ? 0 : entityName.hashCode());
		result = prime * result
				+ ((errorMessage == null) ? 0 : errorMessage.hashCode());
		result = prime * result
				+ ((errorType == null) ? 0 : errorType.hashCode());
		result = prime * result
				+ ((sheetName == null) ? 0 : sheetName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CollaborationErrorLog other = (CollaborationErrorLog) obj;
		if (entityName == null) {
			if (other.entityName != null)
				return false;
		} else if (!entityName.equals(other.entityName))
			return false;
		if (errorMessage == null) {
			if (other.errorMessage != null)
				return false;
		} else if (!errorMessage.equals(other.errorMessage))
			return false;
		if (errorType == null) {
			if (other.errorType != null)
				return false;
		} else if (!errorType.equals(other.errorType))
			return false;
		if (sheetName == null) {
			if (other.sheetName != null)
				return false;
		} else if (!sheetName.equals(other.sheetName))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "CollaborationErrorLog [entityName=" + entityName
				+ ", sheetName=" + sheetName + ", errorMessage=" + errorMessage
				+ ", errorType=" + errorType + "]";
	}
	
}
