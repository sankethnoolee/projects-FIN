package com.fintellix.dld.models;

import java.io.Serializable;

public class DlTaskRepository implements Serializable{

	private static final long serialVersionUID = 1L;
	private String clientCode;
	private String repositoryName;
	private String description;
	public String getClientCode() {
		return clientCode;
	}
	public void setClientCode(String clientCode) {
		this.clientCode = clientCode;
	}
	public String getRepositoryName() {
		return repositoryName;
	}
	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
