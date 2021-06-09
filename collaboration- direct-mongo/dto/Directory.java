package com.fintellix.framework.collaboration.dto;

import java.io.Serializable;

public class Directory implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4247179912001705557L;

	private String directoryId;
	private String parentDirectoryId;
	private String directoryName;
	private String directoryDesc;
	private Long createdTime;
	private Long modifiedTime;
	private Integer creator;
	private Integer lastModifiedBy;
	private Integer solutionId;
	private Integer orgId;
	private String packageLocation;
	private Integer spaceId;
	private Integer isPrivate;
	
	public String getDirectoryId() {
		return directoryId;
	}
	
	public void setDirectoryId(String directoryId) {
		this.directoryId = directoryId;
	}
	
	public String getParentDirectoryId() {
		return parentDirectoryId;
	}
	
	public void setParentDirectoryId(String parentDirectoryId) {
		this.parentDirectoryId = parentDirectoryId;
	}
	
	public String getDirectoryName() {
		return directoryName;
	}
	
	public void setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
	}
	
	public String getDirectoryDesc() {
		return directoryDesc;
	}
	
	public void setDirectoryDesc(String directoryDesc) {
		this.directoryDesc = directoryDesc;
	}
	
	public Long getCreatedTime() {
		return createdTime;
	}
	
	public void setCreatedTime(Long createdTime) {
		this.createdTime = createdTime;
	}
	
	public Long getModifiedTime() {
		return modifiedTime;
	}
	
	public void setModifiedTime(Long modifiedTime) {
		this.modifiedTime = modifiedTime;
	}
	
	
	public Integer getSolutionId() {
		return solutionId;
	}
	
	public void setSolutionId(Integer solutionId) {
		this.solutionId = solutionId;
	}
	
	public Integer getOrgId() {
		return orgId;
	}
	
	public void setOrgId(Integer orgId) {
		this.orgId = orgId;
	}

	public String getPackageLocation() {
		return packageLocation;
	}

	public void setPackageLocation(String packageLocation) {
		this.packageLocation = packageLocation;
	}

	public Integer getCreator() {
		return creator;
	}

	public void setCreator(Integer creator) {
		this.creator = creator;
	}

	public Integer getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(Integer lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Integer getSpaceId() {
		return spaceId;
	}

	public void setSpaceId(Integer spaceId) {
		this.spaceId = spaceId;
	}

	public Integer getIsPrivate() {
		return isPrivate;
	}

	public void setIsPrivate(Integer isPrivate) {
		this.isPrivate = isPrivate;
	}

	@Override
	public String toString() {
		return "Directory [directoryId=" + directoryId + ", parentDirectoryId="
				+ parentDirectoryId + ", directoryName=" + directoryName
				+ ", directoryDesc=" + directoryDesc + ", createdTime="
				+ createdTime + ", modifiedTime=" + modifiedTime + ", creator="
				+ creator + ", lastModifiedBy=" + lastModifiedBy
				+ ", solutionId=" + solutionId + ", orgId=" + orgId
				+ ", packageLocation=" + packageLocation + ", spaceId="
				+ spaceId + ", isPrivate=" + isPrivate + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((createdTime == null) ? 0 : createdTime.hashCode());
		result = prime * result + ((creator == null) ? 0 : creator.hashCode());
		result = prime * result
				+ ((directoryDesc == null) ? 0 : directoryDesc.hashCode());
		result = prime * result
				+ ((directoryId == null) ? 0 : directoryId.hashCode());
		result = prime * result
				+ ((directoryName == null) ? 0 : directoryName.hashCode());
		result = prime * result
				+ ((isPrivate == null) ? 0 : isPrivate.hashCode());
		result = prime * result
				+ ((lastModifiedBy == null) ? 0 : lastModifiedBy.hashCode());
		result = prime * result
				+ ((modifiedTime == null) ? 0 : modifiedTime.hashCode());
		result = prime * result + ((orgId == null) ? 0 : orgId.hashCode());
		result = prime * result
				+ ((packageLocation == null) ? 0 : packageLocation.hashCode());
		result = prime
				* result
				+ ((parentDirectoryId == null) ? 0 : parentDirectoryId
						.hashCode());
		result = prime * result
				+ ((solutionId == null) ? 0 : solutionId.hashCode());
		result = prime * result + ((spaceId == null) ? 0 : spaceId.hashCode());
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
		Directory other = (Directory) obj;
		if (createdTime == null) {
			if (other.createdTime != null)
				return false;
		} else if (!createdTime.equals(other.createdTime))
			return false;
		if (creator == null) {
			if (other.creator != null)
				return false;
		} else if (!creator.equals(other.creator))
			return false;
		if (directoryDesc == null) {
			if (other.directoryDesc != null)
				return false;
		} else if (!directoryDesc.equals(other.directoryDesc))
			return false;
		if (directoryId == null) {
			if (other.directoryId != null)
				return false;
		} else if (!directoryId.equals(other.directoryId))
			return false;
		if (directoryName == null) {
			if (other.directoryName != null)
				return false;
		} else if (!directoryName.equals(other.directoryName))
			return false;
		if (isPrivate == null) {
			if (other.isPrivate != null)
				return false;
		} else if (!isPrivate.equals(other.isPrivate))
			return false;
		if (lastModifiedBy == null) {
			if (other.lastModifiedBy != null)
				return false;
		} else if (!lastModifiedBy.equals(other.lastModifiedBy))
			return false;
		if (modifiedTime == null) {
			if (other.modifiedTime != null)
				return false;
		} else if (!modifiedTime.equals(other.modifiedTime))
			return false;
		if (orgId == null) {
			if (other.orgId != null)
				return false;
		} else if (!orgId.equals(other.orgId))
			return false;
		if (packageLocation == null) {
			if (other.packageLocation != null)
				return false;
		} else if (!packageLocation.equals(other.packageLocation))
			return false;
		if (parentDirectoryId == null) {
			if (other.parentDirectoryId != null)
				return false;
		} else if (!parentDirectoryId.equals(other.parentDirectoryId))
			return false;
		if (solutionId == null) {
			if (other.solutionId != null)
				return false;
		} else if (!solutionId.equals(other.solutionId))
			return false;
		if (spaceId == null) {
			if (other.spaceId != null)
				return false;
		} else if (!spaceId.equals(other.spaceId))
			return false;
		return true;
	}
	
	
	
}
