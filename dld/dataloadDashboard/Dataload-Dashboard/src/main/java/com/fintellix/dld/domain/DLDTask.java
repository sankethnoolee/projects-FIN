package com.fintellix.dld.domain; 

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@RelationshipEntity(type = "DEPENDENT_ON")
public class DLDTask {

	@Override
	public String toString() {
		return "DLDTask [id=" + id + ", taskName=" + taskName + ", clientCode=" + clientCode + ", taskRepository="
				+ taskRepository + ", taskType=" + taskType + ", taskDescription=" + taskDescription
				+ ", taskTechnicalName=" + taskTechnicalName + ", taskSubTaskTechnicalName=" + taskSubTaskTechnicalName
				+ ", fromEntity=" + fromEntity + ", toEntity=" + toEntity + "]";
	}

	@GraphId
	private Long id;
	
	private String taskName;
	private String clientCode;
	private String taskRepository;
	private String taskType;
	private String taskDescription;
	private String taskTechnicalName;
	private String taskSubTaskTechnicalName;
	
	@StartNode
	private DldEntityCollection fromEntity;
	
	@EndNode
	private DldEntityCollection toEntity;
	
	public DLDTask() {
	}
	
	public DLDTask(DldEntityCollection fromEntity, DldEntityCollection toEntity, String taskName, String taskRepo,String clientCode,String taskType) {
		this.fromEntity = fromEntity;
		this.toEntity = toEntity;
		this.clientCode =clientCode;
		this.taskName = taskName;
		this.taskRepository = taskRepo;
		this.taskType=taskType;
	}
	
	public Long getId() {
		return id;
	}

	public String getTaskName() {
		return taskName;
	}

	public String getClientCode() {
		return clientCode;
	}

	public String getTaskRepository() {
		return taskRepository;
	}

	public String getTaskType() {
		return taskType;
	}

	public String getTaskDescription() {
		return taskDescription;
	}

	public String getTaskTechnicalName() {
		return taskTechnicalName;
	}

	public String getTaskSubTaskTechnicalName() {
		return taskSubTaskTechnicalName;
	}

	public DldEntityCollection getFromEntity() {
		return fromEntity;
	}

	public DldEntityCollection getToEntity() {
		return toEntity;
	}
	
}

