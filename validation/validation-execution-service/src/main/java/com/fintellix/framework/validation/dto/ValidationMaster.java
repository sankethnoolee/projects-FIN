package com.fintellix.framework.validation.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


public class ValidationMaster implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer validationId;
	private Integer sequenceNo;
	private String isActive;
	private Integer solutionId;
	private String validationCode;
	private String validationName;
	private String entityCode;
	private String validationDesc;
	private String validationExpression;
	private String entityType;
	private String validationType;
	private Date startDate;
	private Date endDate;
	private Integer userId;
	private Date lastModificationDate;
	private String status;
	private List<ValidationReportLink> validationReportLinkList;
	public Integer getValidationId() {
		return validationId;
	}
	public void setValidationId(Integer validationId) {
		this.validationId = validationId;
	}
	public Integer getSequenceNo() {
		return sequenceNo;
	}
	public void setSequenceNo(Integer sequenceNo) {
		this.sequenceNo = sequenceNo;
	}
	public String getIsActive() {
		return isActive;
	}
	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}
	public Integer getSolutionId() {
		return solutionId;
	}
	public void setSolutionId(Integer solutionId) {
		this.solutionId = solutionId;
	}
	public String getValidationCode() {
		return validationCode;
	}
	public void setValidationCode(String validationCode) {
		this.validationCode = validationCode;
	}
	public String getValidationName() {
		return validationName;
	}
	public void setValidationName(String validationName) {
		this.validationName = validationName;
	}
	public String getEntityCode() {
		return entityCode;
	}
	public void setEntityCode(String entityCode) {
		this.entityCode = entityCode;
	}
	public String getValidationDesc() {
		return validationDesc;
	}
	public void setValidationDesc(String validationDesc) {
		this.validationDesc = validationDesc;
	}
	public String getValidationExpression() {
		return validationExpression;
	}
	public void setValidationExpression(String validationExpression) {
		this.validationExpression = validationExpression;
	}
	public String getEntityType() {
		return entityType;
	}
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	public String getValidationGroup() {
		return validationType;
	}
	public void setValidationGroup(String validationGroup) {
		this.validationType = validationGroup;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public Date getLastModificationDate() {
		return lastModificationDate;
	}
	public void setLastModificationDate(Date lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<ValidationReportLink> getValidationReportLinkList() {
		return validationReportLinkList;
	}
	public void setValidationReportLinkList(List<ValidationReportLink> validationReportLinkList) {
		this.validationReportLinkList = validationReportLinkList;
	} 
	public String getValidationType() {
		return validationType;
	}
	public void setValidationType(String validationType) {
		this.validationType = validationType;
	}
	@Override
	public String toString() {
		return "ValidationMaster [validationId=" + validationId + ", sequenceNo=" + sequenceNo + ", isActive="
				+ isActive + ", solutionId=" + solutionId + ", validationCode=" + validationCode + ", validationName="
				+ validationName + ", entityCode=" + entityCode + ", validationDesc=" + validationDesc
				+ ", validationExpression=" + validationExpression + ", entityType=" + entityType + ", validationType="
				+ validationType + ", startDate=" + startDate + ", endDate=" + endDate + ", userId=" + userId
				+ ", lastModificationDate=" + lastModificationDate + ", status=" + status
				+ ", validationReportLinkList=" + validationReportLinkList + "]";
	}

}
