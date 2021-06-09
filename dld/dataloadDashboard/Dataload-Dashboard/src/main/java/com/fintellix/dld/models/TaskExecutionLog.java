package com.fintellix.dld.models;

import java.io.Serializable;
import java.util.Date;

public class TaskExecutionLog implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String sequenceNumber;
	private String clientCode;
	private String taskRepository;
	private String taskName;
	private String flowType;
	private Integer flowSequenceNumber;
	private String taskStatus;
	private String technicalTaskName;
	private String technicalSubTaskName;
	private String runDetails;
	private Integer sourceCount;
	private Integer targetCount;
	private Integer targetInsertedCount;
	private Integer targetUpdatedCount;
	private Integer targetRejectedRecord;
	private Date startDate;
	private Date endDate;
	private Date runPeriodDate;
	private Date businessDate;
	public String getSequenceNumber() {
		return sequenceNumber;
	}
	public void setSequenceNumber(String sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
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
	public String getFlowType() {
		return flowType;
	}
	public void setFlowType(String flowType) {
		this.flowType = flowType;
	}
	public Integer getFlowSequenceNumber() {
		return flowSequenceNumber;
	}
	public void setFlowSequenceNumber(Integer flowSequenceNumber) {
		this.flowSequenceNumber = flowSequenceNumber;
	}
	public String getTaskStatus() {
		return taskStatus;
	}
	public void setTaskStatus(String taskStatus) {
		this.taskStatus = taskStatus;
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
	public String getRunDetails() {
		return runDetails;
	}
	public void setRunDetails(String runDetails) {
		this.runDetails = runDetails;
	}
	public Integer getSourceCount() {
		return sourceCount;
	}
	public void setSourceCount(Integer sourceCount) {
		this.sourceCount = sourceCount;
	}
	public Integer getTargetCount() {
		return targetCount;
	}
	public void setTargetCount(Integer targetCount) {
		this.targetCount = targetCount;
	}
	public Integer getTargetInsertedCount() {
		return targetInsertedCount;
	}
	public void setTargetInsertedCount(Integer targetInsertedCount) {
		this.targetInsertedCount = targetInsertedCount;
	}
	public Integer getTargetUpdatedCount() {
		return targetUpdatedCount;
	}
	public void setTargetUpdatedCount(Integer targetUpdatedCount) {
		this.targetUpdatedCount = targetUpdatedCount;
	}
	public Integer getTargetRejectedRecord() {
		return targetRejectedRecord;
	}
	public void setTargetRejectedRecord(Integer targetRejectedRecord) {
		this.targetRejectedRecord = targetRejectedRecord;
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
	public Date getRunPeriodDate() {
		return runPeriodDate;
	}
	public void setRunPeriodDate(Date runPeriodDate) {
		this.runPeriodDate = runPeriodDate;
	}
	public Date getBusinessDate() {
		return businessDate;
	}
	public void setBusinessDate(Date businessDate) {
		this.businessDate = businessDate;
	}
	@Override
	public String toString() {
		return "TaskExecutionLog [sequenceNumber=" + sequenceNumber + ", clientCode=" + clientCode + ", taskRepository="
				+ taskRepository + ", taskName=" + taskName + ", flowType=" + flowType + ", flowSequenceNumber="
				+ flowSequenceNumber + ", taskStatus=" + taskStatus + ", technicalTaskName=" + technicalTaskName
				+ ", technicalSubTaskName=" + technicalSubTaskName + ", runDetails=" + runDetails + ", sourceCount="
				+ sourceCount + ", targetCount=" + targetCount + ", targetInsertedCount=" + targetInsertedCount
				+ ", targetUpdatedCount=" + targetUpdatedCount + ", targetRejectedRecord=" + targetRejectedRecord
				+ ", startDate=" + startDate + ", endDate=" + endDate + ", runPeriodDate=" + runPeriodDate
				+ ", businessDate=" + businessDate + "]";
	}
	
	
	
}
