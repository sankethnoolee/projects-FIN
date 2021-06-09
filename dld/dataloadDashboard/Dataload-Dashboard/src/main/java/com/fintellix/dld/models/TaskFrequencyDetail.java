package com.fintellix.dld.models;

import java.io.Serializable;

public class TaskFrequencyDetail implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer frequencyTypeId;
	private String solutionId;
	private String frequencyType;
	private String taskName;
	private String taskRepository;
	private Integer graceDays;
	private Integer versionNo;
	
	public Integer getFrequencyTypeId() {
		return frequencyTypeId;
	}
	public void setFrequencyTypeId(Integer frequencyTypeId) {
		this.frequencyTypeId = frequencyTypeId;
	}
	public String getSolutionId() {
		return solutionId;
	}
	public void setSolutionId(String solutionId) {
		this.solutionId = solutionId;
	}
	public String getFrequencyType() {
		return frequencyType;
	}
	public void setFrequencyType(String frquencyType) {
		this.frequencyType = frquencyType;
	}
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	public Integer getGraceDays() {
		return graceDays;
	}
	public void setGraceDays(Integer graceDays) {
		this.graceDays = graceDays;
	}
	@Override
	public String toString() {
		return "TaskFrequencyDetail [frequencyTypeId=" + frequencyTypeId + ", solutionId=" + solutionId
				+ ", frequencyType=" + frequencyType + ", taskName=" + taskName + ", graceDays=" + graceDays + "]";
	}
	public String getTaskRepository() {
		return taskRepository;
	}
	public void setTaskRepository(String taskRepository) {
		this.taskRepository = taskRepository;
	}
	public Integer getVersionNo() {
		return versionNo;
	}
	public void setVersionNo(Integer versionNo) {
		this.versionNo = versionNo;
	}	

}
