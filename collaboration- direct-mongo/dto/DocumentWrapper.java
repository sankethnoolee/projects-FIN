package com.fintellix.framework.collaboration.dto;

import java.io.Serializable;

public class DocumentWrapper implements Serializable {

	private static final long serialVersionUID = 6643484341807167841L;
	
	private String entityId;
	private String entityName;
	private String entityType;
	private Integer privilegeId;
	private String privilegeName;
	private String entityPath;
	private String createdBy;
	private String lastModifiedBy;
	private Long lastModified;
	private String sortName;
	private Long createdTime;
	private String versionNumber;
	private String resultOrigin;
	private Integer isPrivate;
	
	public Integer getIsPrivate() {
		return isPrivate;
	}
	public void setIsPrivate(Integer isPrivate) {
		this.isPrivate = isPrivate;
	}
	public String getEntityId() {
		return entityId;
	}
	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}
	public String getEntityName() {
		return entityName;
	}
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	public String getEntityType() {
		return entityType;
	}
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	public Integer getPrivilegeId() {
		return privilegeId;
	}
	public void setPrivilegeId(Integer privilegeId) {
		this.privilegeId = privilegeId;
	}
	public String getPrivilegeName() {
		return privilegeName;
	}
	public void setPrivilegeName(String privilegeName) {
		this.privilegeName = privilegeName;
	}
	public String getEntityPath() {
		return entityPath;
	}
	public void setEntityPath(String entityPath) {
		this.entityPath = entityPath;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
	public Long getLastModified() {
		return lastModified;
	}
	public void setLastModified(Long lastModified) {
		this.lastModified = lastModified;
	}
	
	public String getSortName() {
		return sortName;
	}
	public void setSortName(String sortName) {
		this.sortName = sortName;
	}
	public Long getCreatedTime() {
		return createdTime;
	}
	public void setCreatedTime(Long createdTime) {
		this.createdTime = createdTime;
	}
	public String getVersionNumber() {
		return versionNumber;
	}
	public void setVersionNumber(String versionNumber) {
		this.versionNumber = versionNumber;
	}
	
	public String getResultOrigin() {
		return resultOrigin;
	}
	public void setResultOrigin(String resultOrigin) {
		this.resultOrigin = resultOrigin;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((createdBy == null) ? 0 : createdBy.hashCode());
		result = prime * result
				+ ((createdTime == null) ? 0 : createdTime.hashCode());
		result = prime * result
				+ ((entityId == null) ? 0 : entityId.hashCode());
		result = prime * result
				+ ((entityName == null) ? 0 : entityName.hashCode());
		result = prime * result
				+ ((entityPath == null) ? 0 : entityPath.hashCode());
		result = prime * result
				+ ((entityType == null) ? 0 : entityType.hashCode());
		result = prime * result
				+ ((isPrivate == null) ? 0 : isPrivate.hashCode());
		result = prime * result
				+ ((lastModified == null) ? 0 : lastModified.hashCode());
		result = prime * result
				+ ((lastModifiedBy == null) ? 0 : lastModifiedBy.hashCode());
		result = prime * result
				+ ((privilegeId == null) ? 0 : privilegeId.hashCode());
		result = prime * result
				+ ((privilegeName == null) ? 0 : privilegeName.hashCode());
		result = prime * result
				+ ((resultOrigin == null) ? 0 : resultOrigin.hashCode());
		result = prime * result
				+ ((sortName == null) ? 0 : sortName.hashCode());
		result = prime * result
				+ ((versionNumber == null) ? 0 : versionNumber.hashCode());
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
		DocumentWrapper other = (DocumentWrapper) obj;
		if (createdBy == null) {
			if (other.createdBy != null)
				return false;
		} else if (!createdBy.equals(other.createdBy))
			return false;
		if (createdTime == null) {
			if (other.createdTime != null)
				return false;
		} else if (!createdTime.equals(other.createdTime))
			return false;
		if (entityId == null) {
			if (other.entityId != null)
				return false;
		} else if (!entityId.equals(other.entityId))
			return false;
		if (entityName == null) {
			if (other.entityName != null)
				return false;
		} else if (!entityName.equals(other.entityName))
			return false;
		if (entityPath == null) {
			if (other.entityPath != null)
				return false;
		} else if (!entityPath.equals(other.entityPath))
			return false;
		if (entityType == null) {
			if (other.entityType != null)
				return false;
		} else if (!entityType.equals(other.entityType))
			return false;
		if (isPrivate == null) {
			if (other.isPrivate != null)
				return false;
		} else if (!isPrivate.equals(other.isPrivate))
			return false;
		if (lastModified == null) {
			if (other.lastModified != null)
				return false;
		} else if (!lastModified.equals(other.lastModified))
			return false;
		if (lastModifiedBy == null) {
			if (other.lastModifiedBy != null)
				return false;
		} else if (!lastModifiedBy.equals(other.lastModifiedBy))
			return false;
		if (privilegeId == null) {
			if (other.privilegeId != null)
				return false;
		} else if (!privilegeId.equals(other.privilegeId))
			return false;
		if (privilegeName == null) {
			if (other.privilegeName != null)
				return false;
		} else if (!privilegeName.equals(other.privilegeName))
			return false;
		if (resultOrigin == null) {
			if (other.resultOrigin != null)
				return false;
		} else if (!resultOrigin.equals(other.resultOrigin))
			return false;
		if (sortName == null) {
			if (other.sortName != null)
				return false;
		} else if (!sortName.equals(other.sortName))
			return false;
		if (versionNumber == null) {
			if (other.versionNumber != null)
				return false;
		} else if (!versionNumber.equals(other.versionNumber))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "DocumentWrapper [entityId=" + entityId + ", entityName="
				+ entityName + ", entityType=" + entityType + ", privilegeId="
				+ privilegeId + ", privilegeName=" + privilegeName
				+ ", entityPath=" + entityPath + ", createdBy=" + createdBy
				+ ", lastModifiedBy=" + lastModifiedBy + ", lastModified="
				+ lastModified + ", sortName=" + sortName + ", createdTime="
				+ createdTime + ", versionNumber=" + versionNumber
				+ ", resultOrigin=" + resultOrigin + ", isPrivate=" + isPrivate
				+ "]";
	}
	
	
	
}
