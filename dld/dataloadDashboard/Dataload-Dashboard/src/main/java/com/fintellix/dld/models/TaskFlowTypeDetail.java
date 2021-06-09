package com.fintellix.dld.models;

import java.io.Serializable;
import java.util.Date;

public class TaskFlowTypeDetail implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String clientCode;
	private String taskRepository;
	private String flowType;
	private String taskName;
	private Date effectiveStartDate;
	private Date effectiveEndDate;
	private Integer versionNo;
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
	public String getFlowType() {
		return flowType;
	}
	public void setFlowType(String flowType) {
		this.flowType = flowType;
	}
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	public Date getEffectiveStartDate() {
		return effectiveStartDate;
	}
	public void setEffectiveStartDate(Date effectiveStartDate) {
		this.effectiveStartDate = effectiveStartDate;
	}
	public Date getEffectiveEndDate() {
		return effectiveEndDate;
	}
	public void setEffectiveEndDate(Date effectiveEndDate) {
		this.effectiveEndDate = effectiveEndDate;
	}
	@Override
	public String toString() {
		return "TaskFlowTypeDetail [clientCode=" + clientCode
				+ ", taskRepository=" + taskRepository + ", flowType="
				+ flowType + ", taskName=" + taskName + ", effectiveStartDate="
				+ effectiveStartDate + ", effectiveEndDate=" + effectiveEndDate
				+ "]";
	}
	public Integer getVersionNo() {
		return versionNo;
	}
	public void setVersionNo(Integer versionNo) {
		this.versionNo = versionNo;
	}
	
	}
