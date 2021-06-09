package com.fintellix.dld.domain;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@NodeEntity (label="DataRepository")
public class DataRepositoryOwner extends AbstractEntityOwner{

	@Override
	public String toString() {
		return "DataRepositoryOwner [id=" + id + ", solutionName=" + solutionName + ", contactDetails=" + contactDetails
				+ ", displayOrder=" + displayOrder + "]";
	}

	@GraphId
	private Long id;
	
	private String solutionName;
	private String contactDetails;
	private Integer displayOrder;
	
	public DataRepositoryOwner(){
		
	}
	
	public DataRepositoryOwner(String ownerName, String ownerDesc, Boolean isExternalSource, String solutionName,String contactDetails, Integer displayOrder) {
		super(ownerName, ownerDesc, isExternalSource);
		this.solutionName =solutionName;
		this.contactDetails =contactDetails;
		this.displayOrder =displayOrder;
	}

	/*@Relationship(type = "BELONGS_TO", direction = Relationship.INCOMING)
	private List <DLDEntity> entitiesBelongingtoDataRepo = new ArrayList<DLDEntity>();*/
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getSolutionName() {
		return solutionName;
	}

	public String getContactDetails() {
		return contactDetails;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}
	
/*	public void addDLDEntitie(DLDEntity dldentity){
		entitiesBelongingtoDataRepo.add(dldentity);
	}
	
	public void addDLDEntities(Collection<? extends DLDEntity> dldentities){
		entitiesBelongingtoDataRepo.addAll(dldentities);
	}

	public List<DLDEntity> getEntitiesBelongingtoDataRepo() {
		return entitiesBelongingtoDataRepo;
	}*/
	
}
