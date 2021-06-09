package com.fintellix.dld.models;

import java.io.Serializable;

public class TaskEntityDetailUploaderDTO implements Serializable {

private static final long serialVersionUID = 1L;
	
String taskRepository;
String taskName;
String entityOwnerName;
String entityName;
String linkType;
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
public String getEntityOwnerName() {
	return entityOwnerName;
}
public void setEntityOwnerName(String entityOwnerName) {
	this.entityOwnerName = entityOwnerName;
}
public String getEntityName() {
	return entityName;
}
public void setEntityName(String entityName) {
	this.entityName = entityName;
}
public String getLinkType() {
	return linkType;
}
public void setLinkType(String linkType) {
	this.linkType = linkType;
}
	
	
}
