package com.fintellix.framework.collaboration.dto;

import java.io.Serializable;

public class ShareDetailsForUpload implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 598344082056633034L;
	private String solutionName;
	private String organisationName;
	private String directoryPath;
	private String contentType;
	private String contentName;
	private String shareWith;
	private Integer sharedOrganisationName;
	private Integer roleOrUserId;
	private String privilege;
	public String getSolutionName() {
		return solutionName;
	}
	public void setSolutionName(String solutionName) {
		this.solutionName = solutionName;
	}
	public String getOrganisationName() {
		return organisationName;
	}
	public void setOrganisationName(String organisationName) {
		this.organisationName = organisationName;
	}
	public String getDirectoryPath() {
		return directoryPath;
	}
	public void setDirectoryPath(String directoryPath) {
		this.directoryPath = directoryPath;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getContentName() {
		return contentName;
	}
	public void setContentName(String contentName) {
		this.contentName = contentName;
	}
	public String getShareWith() {
		return shareWith;
	}
	public void setShareWith(String shareWith) {
		this.shareWith = shareWith;
	}
	public Integer getSharedOrganisationName() {
		return sharedOrganisationName;
	}
	public void setSharedOrganisationName(Integer sharedOrganisationName) {
		this.sharedOrganisationName = sharedOrganisationName;
	}
	public Integer getRoleOrUserId() {
		return roleOrUserId;
	}
	public void setRoleOrUserId(Integer roleOrUserId) {
		this.roleOrUserId = roleOrUserId;
	}
	public String getPrivilege() {
		return privilege;
	}
	public void setPrivilege(String privilege) {
		this.privilege = privilege;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((contentName == null) ? 0 : contentName.hashCode());
		result = prime * result
				+ ((contentType == null) ? 0 : contentType.hashCode());
		result = prime * result
				+ ((directoryPath == null) ? 0 : directoryPath.hashCode());
		result = prime
				* result
				+ ((organisationName == null) ? 0 : organisationName.hashCode());
		result = prime * result
				+ ((privilege == null) ? 0 : privilege.hashCode());
		result = prime * result
				+ ((roleOrUserId == null) ? 0 : roleOrUserId.hashCode());
		result = prime * result
				+ ((shareWith == null) ? 0 : shareWith.hashCode());
		result = prime
				* result
				+ ((sharedOrganisationName == null) ? 0
						: sharedOrganisationName.hashCode());
		result = prime * result
				+ ((solutionName == null) ? 0 : solutionName.hashCode());
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
		ShareDetailsForUpload other = (ShareDetailsForUpload) obj;
		if (contentName == null) {
			if (other.contentName != null)
				return false;
		} else if (!contentName.equals(other.contentName))
			return false;
		if (contentType == null) {
			if (other.contentType != null)
				return false;
		} else if (!contentType.equals(other.contentType))
			return false;
		if (directoryPath == null) {
			if (other.directoryPath != null)
				return false;
		} else if (!directoryPath.equals(other.directoryPath))
			return false;
		if (organisationName == null) {
			if (other.organisationName != null)
				return false;
		} else if (!organisationName.equals(other.organisationName))
			return false;
		if (privilege == null) {
			if (other.privilege != null)
				return false;
		} else if (!privilege.equals(other.privilege))
			return false;
		if (roleOrUserId == null) {
			if (other.roleOrUserId != null)
				return false;
		} else if (!roleOrUserId.equals(other.roleOrUserId))
			return false;
		if (shareWith == null) {
			if (other.shareWith != null)
				return false;
		} else if (!shareWith.equals(other.shareWith))
			return false;
		if (sharedOrganisationName == null) {
			if (other.sharedOrganisationName != null)
				return false;
		} else if (!sharedOrganisationName.equals(other.sharedOrganisationName))
			return false;
		if (solutionName == null) {
			if (other.solutionName != null)
				return false;
		} else if (!solutionName.equals(other.solutionName))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "ShareDetailsForUpload [solutionName=" + solutionName
				+ ", organisationName=" + organisationName + ", directoryPath="
				+ directoryPath + ", contentType=" + contentType
				+ ", contentName=" + contentName + ", shareWith=" + shareWith
				+ ", sharedOrganisationName=" + sharedOrganisationName
				+ ", roleOrUserId=" + roleOrUserId + ", privilege=" + privilege
				+ "]";
	}
	
	
}
