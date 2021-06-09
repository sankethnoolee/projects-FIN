package com.fintellix.framework.collaboration.dto;

import java.io.Serializable;

public class File implements Serializable{
	
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

	public String getDirectoryId() {
		return directoryId;
	}

	public void setDirectoryId(String directoryId) {
		this.directoryId = directoryId;
	}

	@Override
	public String toString() {
		return "File [fileId=" + fileId + ", fileName=" + fileName + ", fileDesc=" + fileDesc + ", createdTime="
				+ createdTime + ", modifiedTime=" + modifiedTime + ", creatorName=" + creatorId + ", lastModifiedBy="
				+ lastModifiedById + ", solutionId=" + solutionId + ", orgId=" + orgId + ", packageLocation="
				+ packageLocation + ", directoryId=" + directoryId + ", versionNumber=" + versionNumber + ", active="
				+ active + "]";
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
	
	

}
