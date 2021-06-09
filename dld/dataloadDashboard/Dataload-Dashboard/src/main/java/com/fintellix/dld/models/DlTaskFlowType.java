package com.fintellix.dld.models;

import java.io.Serializable;

public class DlTaskFlowType implements Serializable{
	
	private static final long serialVersionUID = 1L; 

	private String clientCode;
	private String taskRepository;
	private String flowType;
	private String taskName;
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
	public Integer getVersionNo() {
		return versionNo;
	}
	public void setVersionNo(Integer versionNo) {
		this.versionNo = versionNo;
	}
	
	
	
	
	
	
	
	
	
}
