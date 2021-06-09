package com.fintellix.dld.domain;

import org.neo4j.ogm.annotation.Relationship;

public abstract class AbstractEntityOwner {
	
	private String ownerName;
	private String ownerDescription;
	private Boolean isExternalSource;
	
	public AbstractEntityOwner(){}
	
	public AbstractEntityOwner(String ownerName, String ownerDesc, Boolean isExternalSource){
		this.ownerName=ownerName;
		this.ownerDescription=ownerDesc;
		this.isExternalSource=isExternalSource;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public String getOwnerDescription() {
		return ownerDescription;
	}

	public Boolean getIsExternalSource() {
		return isExternalSource;
	}
	
	public ClientContent getClient() {
		return client;
	}

	public void setClient(ClientContent client) {
		this.client = client;
	}

	@Relationship(type = "BELONGS_TO_CLIENT", direction = Relationship.OUTGOING)
	private ClientContent client;
	

	

}
