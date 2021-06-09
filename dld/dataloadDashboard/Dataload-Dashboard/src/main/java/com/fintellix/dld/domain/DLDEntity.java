package com.fintellix.dld.domain;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;


@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@NodeEntity
public class DLDEntity {


	@GraphId
	private Long id;
	
	private String entityName;
	private String clientCode;
	private String entityType;
	private String entityDetail;
	private String entityDescription;
	private String entityOwnerName;
	
	@Relationship(type = "BELONGS_TO_CLIENT", direction = Relationship.OUTGOING)
	private ClientContent client;
	
	
	public DLDEntity() {
	}

	public DLDEntity(String entityName,String clientCode) {

		this.entityName = entityName;
		this.clientCode = clientCode;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public String getClientCode() {
		return clientCode;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getEntityDetail() {
		return entityDetail;
	}

	public void setEntityDetail(String entityDetail) {
		this.entityDetail = entityDetail;
	}

	public String getEntityDescription() {
		return entityDescription;
	}

	public void setEntityDescription(String entityDescription) {
		this.entityDescription = entityDescription;
	}

	public String getEntityOwnerName() {
		return entityOwnerName;
	}

	public void setEntityOwnerName(String entityOwnerName) {
		this.entityOwnerName = entityOwnerName;
	}

	@Override
	public String toString() {
		return "DLDEntity [id=" + id + ", entityName=" + entityName + ", clientCode=" + clientCode + ", entityType="
				+ entityType + ", entityDetail=" + entityDetail + ", entityDescription=" + entityDescription
				+ ", entityOwnerName=" + entityOwnerName + ", entityOwner=" + "]";
	}

	public ClientContent getClient() {
		return client;
	}

	public void setClient(ClientContent client) {
		this.client = client;
	}
	
}
