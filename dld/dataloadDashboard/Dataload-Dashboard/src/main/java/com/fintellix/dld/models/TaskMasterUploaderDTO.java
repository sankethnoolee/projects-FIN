package com.fintellix.dld.models;

import java.io.Serializable;
import java.util.List;

public class TaskMasterUploaderDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String task_Repository;	
	private String task_Name;	
	private String task_Type;	
	private String description;	
	private String task_Technical_Name;
	private String sub_Task_Technical_Name;
	private List<String> task_Flows;	
	private String status;
	private List<TaskFrequencyExclusionOffset> task_Frequency_Exclusions_Offset;
	private List<TaskFrequencyOffset> task_Frequency_Offset;
	private String isValidationRequired;
	
	
	public String getIsValidationRequired() {
		return isValidationRequired;
	}
	public void setIsValidationRequired(String isValidationRequired) {
		this.isValidationRequired = isValidationRequired;
	}
	
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((task_Repository == null) ? 0 : task_Repository
						.hashCode());
		result = prime
				* result
				+ ((task_Name == null) ? 0 : task_Name
						.hashCode());
		result = prime
				* result
				+ ((task_Type == null) ? 0 : task_Type
						.hashCode());
		
		return result;
	}
	
	public List<TaskFrequencyExclusionOffset> getTask_Frequency_Exclusions_Offset() {
		return task_Frequency_Exclusions_Offset;
	}

	public void setTask_Frequency_Exclusions_Offset(List<TaskFrequencyExclusionOffset> task_Frequency_Exclusions_Offset) {
		this.task_Frequency_Exclusions_Offset = task_Frequency_Exclusions_Offset;
	}

	public List<TaskFrequencyOffset> getTask_Frequency_Offset() {
		return task_Frequency_Offset;
	}

	public void setTask_Frequency_Offset(List<TaskFrequencyOffset> task_Frequency_Offset) {
		this.task_Frequency_Offset = task_Frequency_Offset;
	}
	
	public List<String> getTask_Flows() {
		return task_Flows;
	}

	public void setTask_Flows(List<String> task_Flows) {
		this.task_Flows = task_Flows;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaskMasterUploaderDTO other = (TaskMasterUploaderDTO) obj;
		if (task_Repository == null) {
			if (other.task_Repository != null)
				return false;
		} else if (!task_Repository.equals(other.task_Repository))
			return false;
		
		if (task_Name == null) {
			if (other.task_Name != null)
				return false;
		} else if (!task_Name.equals(other.task_Name))
			return false;
		
		if (task_Type == null) {
			if (other.task_Type != null)
				return false;
		} else if (!task_Type.equals(other.task_Type))
			return false;
		
		return true;
	}

	
	
	
	public String getTask_Repository() {
		return task_Repository;
	}
	public void setTask_Repository(String task_Repository) {
		this.task_Repository = task_Repository;
	}
	public String getTask_Name() {
		return task_Name;
	}
	public void setTask_Name(String task_Name) {
		this.task_Name = task_Name;
	}
	public String getTask_Type() {
		return task_Type;
	}
	public void setTask_Type(String task_Type) {
		this.task_Type = task_Type;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getTask_Technical_Name() {
		return task_Technical_Name;
	}
	public void setTask_Technical_Name(String task_Technical_Name) {
		this.task_Technical_Name = task_Technical_Name;
	}
	public String getSub_Task_Technical_Name() {
		return sub_Task_Technical_Name;
	}
	public void setSub_Task_Technical_Name(String sub_Task_Technical_Name) {
		this.sub_Task_Technical_Name = sub_Task_Technical_Name;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	
	

}
