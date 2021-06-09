package com.fintellix.dld.models;

import java.io.Serializable;

public class EntityMasterUploaderDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	private String entity_Name;
	private String owner_Name;	
	private String entity_Type;	
	private String entity_Detail;	
	private String description;
	public String getEntity_Name() {
		return entity_Name;
	}
	public void setEntity_Name(String entity_Name) {
		this.entity_Name = entity_Name;
	}
	public String getOwner_Name() {
		return owner_Name;
	}
	public void setOwner_Name(String owner_Name) {
		this.owner_Name = owner_Name;
	}
	public String getEntity_Type() {
		return entity_Type;
	}
	public void setEntity_Type(String entity_Type) {
		this.entity_Type = entity_Type;
	}
	public String getEntity_Detail() {
		return entity_Detail;
	}
	public void setEntity_Detail(String entity_Detail) {
		this.entity_Detail = entity_Detail;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((entity_Name == null) ? 0 : entity_Name
						.hashCode());
		result = prime
				* result
				+ ((owner_Name == null) ? 0 : owner_Name
						.hashCode());
		result = prime
				* result
				+ ((entity_Type == null) ? 0 : entity_Type
						.hashCode());
		
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityMasterUploaderDTO other = (EntityMasterUploaderDTO) obj;
		if (entity_Name == null) {
			if (other.entity_Name != null)
				return false;
		} else if (!entity_Name.equals(other.entity_Name))
			return false;
		
		if (owner_Name == null) {
			if (other.owner_Name != null)
				return false;
		} else if (!owner_Name.equals(other.owner_Name))
			return false;
		
		if (entity_Type == null) {
			if (other.entity_Type != null)
				return false;
		} else if (!entity_Type.equals(other.entity_Type))
			return false;
		
		return true;
	}

}
