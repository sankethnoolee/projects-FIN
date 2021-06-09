package com.fintellix.dld.models;

import java.io.Serializable;

public class DlTaskFrequency implements Serializable {

	private static final long serialVersionUID = 1L;
	private String clientCode;
	private String taskRepository;
	private String taskName;
	private Integer versionNo;
	private String frequency;
	private Integer offset;
	private String isExclusionInd;
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
	public Integer getVersionNo() {
		return versionNo;
	}
	public void setVersionNo(Integer versionNo) {
		this.versionNo = versionNo;
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
	public String getIsExclusionInd() {
		return isExclusionInd;
	}
	public void setIsExclusionInd(String isExclusionInd) {
		this.isExclusionInd = isExclusionInd;
	}
	
	
	
}
