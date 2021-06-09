package com.fintellix.framework.collaboration.dto;

import java.io.Serializable;

public class FileForUpload implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6643484341807167841L;
	
	private String fileId;
	private String fileName;
	private String fileDesc;
	private Long createdTime;
	private Long modifiedTime;
	private Integer creatorId;
	private Integer lastModifiedById;
	private Integer solutionId;
	private Integer orgId;
	private String packageLocation;
	private String directoryId;
	private String versionNumber;
	private Integer active;
	private String actualPath;
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileDesc() {
		return fileDesc;
	}
	public void setFileDesc(String fileDesc) {
		this.fileDesc = fileDesc;
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
	public Integer getCreatorId() {
		return creatorId;
	}
	public void setCreatorId(Integer creatorId) {
		this.creatorId = creatorId;
	}
	public Integer getLastModifiedById() {
		return lastModifiedById;
	}
	public void setLastModifiedById(Integer lastModifiedById) {
		this.lastModifiedById = lastModifiedById;
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
	public String getDirectoryId() {
		return directoryId;
	}
	public void setDirectoryId(String directoryId) {
		this.directoryId = directoryId;
	}
	public String getVersionNumber() {
		return versionNumber;
	}
	public void setVersionNumber(String versionNumber) {
		this.versionNumber = versionNumber;
	}
	public Integer getActive() {
		return active;
	}
	public void setActive(Integer active) {
		this.active = active;
	}
	public String getActualPath() {
		return actualPath;
	}
	public void setActualPath(String actualPath) {
		this.actualPath = actualPath;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((active == null) ? 0 : active.hashCode());
		result = prime * result
				+ ((actualPath == null) ? 0 : actualPath.hashCode());
		result = prime * result
				+ ((createdTime == null) ? 0 : createdTime.hashCode());
		result = prime * result
				+ ((creatorId == null) ? 0 : creatorId.hashCode());
		result = prime * result
				+ ((directoryId == null) ? 0 : directoryId.hashCode());
		result = prime * result
				+ ((fileDesc == null) ? 0 : fileDesc.hashCode());
		result = prime * result + ((fileId == null) ? 0 : fileId.hashCode());
		result = prime * result
				+ ((fileName == null) ? 0 : fileName.hashCode());
		result = prime
				* result
				+ ((lastModifiedById == null) ? 0 : lastModifiedById.hashCode());
		result = prime * result
				+ ((modifiedTime == null) ? 0 : modifiedTime.hashCode());
		result = prime * result + ((orgId == null) ? 0 : orgId.hashCode());
		result = prime * result
				+ ((packageLocation == null) ? 0 : packageLocation.hashCode());
		result = prime * result
				+ ((solutionId == null) ? 0 : solutionId.hashCode());
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
		FileForUpload other = (FileForUpload) obj;
		if (active == null) {
			if (other.active != null)
				return false;
		} else if (!active.equals(other.active))
			return false;
		if (actualPath == null) {
			if (other.actualPath != null)
				return false;
		} else if (!actualPath.equals(other.actualPath))
			return false;
		if (createdTime == null) {
			if (other.createdTime != null)
				return false;
		} else if (!createdTime.equals(other.createdTime))
			return false;
		if (creatorId == null) {
			if (other.creatorId != null)
				return false;
		} else if (!creatorId.equals(other.creatorId))
			return false;
		if (directoryId == null) {
			if (other.directoryId != null)
				return false;
		} else if (!directoryId.equals(other.directoryId))
			return false;
		if (fileDesc == null) {
			if (other.fileDesc != null)
				return false;
		} else if (!fileDesc.equals(other.fileDesc))
			return false;
		if (fileId == null) {
			if (other.fileId != null)
				return false;
		} else if (!fileId.equals(other.fileId))
			return false;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (lastModifiedById == null) {
			if (other.lastModifiedById != null)
				return false;
		} else if (!lastModifiedById.equals(other.lastModifiedById))
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
		if (solutionId == null) {
			if (other.solutionId != null)
				return false;
		} else if (!solutionId.equals(other.solutionId))
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
		return "FileForUpload [fileId=" + fileId + ", fileName=" + fileName
				+ ", fileDesc=" + fileDesc + ", createdTime=" + createdTime
				+ ", modifiedTime=" + modifiedTime + ", creatorId=" + creatorId
				+ ", lastModifiedById=" + lastModifiedById + ", solutionId="
				+ solutionId + ", orgId=" + orgId + ", packageLocation="
				+ packageLocation + ", directoryId=" + directoryId
				+ ", versionNumber=" + versionNumber + ", active=" + active
				+ ", actualPath=" + actualPath + "]";
	}
	
	
}
