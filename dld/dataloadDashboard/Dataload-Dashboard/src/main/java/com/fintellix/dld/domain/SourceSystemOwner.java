package com.fintellix.dld.domain;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@NodeEntity(label="SourceSystem")
public class SourceSystemOwner extends AbstractEntityOwner {
	@GraphId
	private Long id;
	
	private String dataSourceName;
	private String contactDetails;
	private Integer displayOrder;
	
	public SourceSystemOwner(String ownerName, String ownerDesc, Boolean isExternalSource, String dataSourceName,String contactDetails, Integer displayOrder) {
		super(ownerName, ownerDesc, isExternalSource);
		this.dataSourceName =dataSourceName;
		this.contactDetails =contactDetails;
		this.displayOrder =displayOrder;
	}
	
	public SourceSystemOwner() {
		// TODO Auto-generated constructor stub
	}
	
/*	@Relationship(type = "BELONGS_TO", direction = Relationship.INCOMING)
	private Set <DLDEntity> entitiesBelongingtoSources = new HashSet<DLDEntity>();
	
	public Set<DLDEntity> getEntities() {
		return entitiesBelongingtoSources;
	}

	public void addEntity(DLDEntity entity) {
		this.entitiesBelongingtoSources.add(entity);
	}*/
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getDataSourceName() {
		return dataSourceName;
	}

	public String getContactDetails() {
		return contactDetails;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	@Override
	public String toString() {
		return "SourceSystemOwner [id=" + id + ", dataSourceName=" + dataSourceName + ", contactDetails="
				+ contactDetails + ", displayOrder=" + displayOrder + "]";
	}
	
	
	
	
	
	
	
	
}
