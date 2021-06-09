package com.fintellix.dld.models;

import java.util.Date;

public class DlTaskMaster {
	
	
	private String clientCode;
	private String taskRepository;
	private String taskName;
	private String taskType;
	private String taskDescription;
	private String technicalTaskName;
	private String technicalSubTaskName;
	private Integer versionNo;
	private Date startDate;
	private Date enddate;
	private String isActive;
	private String isValidationRequired;
	
	
	public String getIsValidationRequired() {
		return isValidationRequired;
	}
	public void setIsValidationRequired(String isValidationRequired) {
		this.isValidationRequired = isValidationRequired;
	}
	public Integer getVersionNo() {
		return versionNo;
	}
	public void setVersionNo(Integer versionNo) {
		this.versionNo = versionNo;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEnddate() {
		return enddate;
	}
	public void setEnddate(Date enddate) {
		this.enddate = enddate;
	}
	public String getIsActive() {
		return isActive;
	}
	public void setIsActive(String isActive) {
		this.isActive = isActive;
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
	public String getTaskType() {
		return taskType;
	}
	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}
	public String getTaskDescription() {
		return taskDescription;
	}
	public void setTaskDescription(String taskDescription) {
		this.taskDescription = taskDescription;
	}
	public String getTechnicalTaskName() {
		return technicalTaskName;
	}
	public void setTechnicalTaskName(String technicalTaskName) {
		this.technicalTaskName = technicalTaskName;
	}
	public String getTechnicalSubTaskName() {
		return technicalSubTaskName;
	}
	public void setTechnicalSubTaskName(String technicalSubTaskName) {
		this.technicalSubTaskName = technicalSubTaskName;
	}
	@Override
	public String toString() {
		return "TaskMaster [clientCode=" + clientCode + ", taskRepository=" + taskRepository + ", taskName=" + taskName
				+ ", taskType=" + taskType + ", taskDescription=" + taskDescription + ", technicalTaskName="
				+ technicalTaskName + ", technicalSubTaskName=" + technicalSubTaskName + "]";
	}
}
