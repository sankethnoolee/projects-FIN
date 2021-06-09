package com.fintellix.dld.models;

import java.io.Serializable;
import java.sql.Date;

public class StagingDetails implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String clientCode;
	private String taskRepository;
	private String taskName;
	private String frequency;
	private Integer offset;
	private String isExclusionIndicator;
	private String ownerName;
	private String entityName;
	private String taskStatus;
	private String linkType;
	private String flowType;
	private String ownerDesc;
	private String ownerContactDetails;
	private String taskDesc;
	private String taskType;
	private String sourceEntityName;
	private String targetEntityName;
	private Integer runCount;
	private String entityDesc;
	private String entityType;
	private Date businessDate;
	private Date runDate;
	private java.util.Date startDateTime;
	private java.util.Date endDateTime;
	private String runDetails;
	private String taskTechnicalName;
	private String entityTechnicalName;
	private String entityOwner;
	private Integer solutionId;
	
	public String getOwnerDesc() {
		return ownerDesc;
	}
	public void setOwnerDesc(String ownerDesc) {
		this.ownerDesc = ownerDesc;
	}
	public String getOwnerContactDetails() {
		return ownerContactDetails;
	}
	public void setOwnerContactDetails(String ownerContactDetails) {
		this.ownerContactDetails = ownerContactDetails;
	}
	public String getTaskDesc() {
		return taskDesc;
	}
	public void setTaskDesc(String taskDesc) {
		this.taskDesc = taskDesc;
	}
	public String getSourceEntityName() {
		return sourceEntityName;
	}
	public void setSourceEntityName(String sourceEntityName) {
		this.sourceEntityName = sourceEntityName;
	}
	public String getTargetEntityName() {
		return targetEntityName;
	}
	public void setTargetEntityName(String targetEntityName) {
		this.targetEntityName = targetEntityName;
	}
	public Integer getRunCount() {
		return runCount;
	}
	public void setRunCount(Integer runCount) {
		this.runCount = runCount;
	}
	public String getEntityDesc() {
		return entityDesc;
	}
	public void setEntityDesc(String entityDesc) {
		this.entityDesc = entityDesc;
	}
	public String getEntityType() {
		return entityType;
	}
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	public String getClientCode() {
		return clientCode;
	}
	public void setClientCode(String clientCode) {
		this.clientCode = clientCode;
	}
	public String getTaskRepository() {
		return taskRepository;
	}
	public void setTaskRepository(String taskRepository) {
		this.taskRepository = taskRepository;
	}
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	public String getFrequency() {
		return frequency;
	}
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	public Integer getOffset() {
		return offset;
	}
	public void setOffset(Integer offset) {
		this.offset = offset;
	}
	public String getIsExclusionIndicator() {
		return isExclusionIndicator;
	}
	public void setIsExclusionIndicator(String isExclusionIndicator) {
		this.isExclusionIndicator = isExclusionIndicator;
	}
	public String getOwnerName() {
		return ownerName;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	public String getEntityName() {
		return entityName;
	}
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	public String getTaskStatus() {
		return taskStatus;
	}
	public void setTaskStatus(String taskStatus) {
		this.taskStatus = taskStatus;
	}
	public String getLinkType() {
		return linkType;
	}
	public void setLinkType(String linkType) {
		this.linkType = linkType;
	}
	public String getFlowType() {
		return flowType;
	}
	public void setFlowType(String flowType) {
		this.flowType = flowType;
	}
	
	@Override
	public String toString() {
		return "StagingDetails [clientCode=" + clientCode + ", taskRepository="
				+ taskRepository + ", taskName=" + taskName + ", frequency="
				+ frequency + ", offset=" + offset + ", isExclusionIndicator="
				+ isExclusionIndicator + ", ownerName=" + ownerName
				+ ", entityName=" + entityName + ", taskStatus=" + taskStatus
				+ ", linkType=" + linkType + ", flowType=" + flowType + "]";
	}
	public Date getBusinessDate() {
		return businessDate;
	}
	public void setBusinessDate(Date businessDate) {
		this.businessDate = businessDate;
	}
	public Date getRunDate() {
		return runDate;
	}
	public void setRunDate(Date runDate) {
		this.runDate = runDate;
	}
	public String getTaskType() {
		return taskType;
	}
	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}
	public java.util.Date getStartDateTime() {
		return startDateTime;
	}
	public void setStartDateTime(java.util.Date startDateTime) {
		this.startDateTime = startDateTime;
	}
	public java.util.Date getEndDateTime() {
		return endDateTime;
	}
	public void setEndDateTime(java.util.Date endDateTime) {
		this.endDateTime = endDateTime;
	}
	public String getRunDetails() {
		return runDetails;
	}
	public void setRunDetails(String runDetails) {
		this.runDetails = runDetails;
	}
	public String getTaskTechnicalName() {
		return taskTechnicalName;
	}
	public void setTaskTechnicalName(String taskTechnicalName) {
		this.taskTechnicalName = taskTechnicalName;
	}
	public String getEntityTechnicalName() {
		return entityTechnicalName;
	}
	public void setEntityTechnicalName(String entityTechnicalName) {
		this.entityTechnicalName = entityTechnicalName;
	}
	public String getEntityOwner() {
		return entityOwner;
	}
	public void setEntityOwner(String entityOwner) {
		this.entityOwner = entityOwner;
	}
	public Integer getSolutionId() {
		return solutionId;
	}
	public void setSolutionId(Integer solutionId) {
		this.solutionId = solutionId;
	}

}
