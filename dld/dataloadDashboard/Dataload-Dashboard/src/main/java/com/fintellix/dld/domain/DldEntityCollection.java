package com.fintellix.dld.domain;

import java.util.Set;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@NodeEntity
public class DldEntityCollection {
	
	@GraphId
	private Long id;
	
	@Relationship(type = "HAS_A", direction = Relationship.OUTGOING)
	private Set<DLDEntity> entities;
	
/*	@Relationship(type = "DEPENDENT_ON", direction = Relationship.INCOMING)
	private DldEntityCollection entity = new DldEntityCollection();*/

	public Set<DLDEntity> getEntities() {
		return entities;
	}

	public void setEntities(Set<DLDEntity> entities) {
		this.entities = entities;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

/*	public DldEntityCollection getEntity() {
		return entity;
	}

	public void setEntity(DldEntityCollection entity) {
		this.entity = entity;
	}*/

}
