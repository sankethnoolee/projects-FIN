package com.fintellix.framework.collaboration.dto;

import java.io.Serializable;

public class ContentSecurity implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 598344082056633034L;
	private String contentSecurityId;
	private String contentTypeId;
	private String contentId;
	private Integer userId;
	private Integer solutionId;
	private Integer orgId;
	private String securityTemplateName;
	
	public String getContentSecurityId() {
		return contentSecurityId;
	}
	
	public void setContentSecurityId(String contentSecurityId) {
		this.contentSecurityId = contentSecurityId;
	}
	
	public String getContentTypeId() {
		return contentTypeId;
	}
	
	public void setContentTypeId(String contentTypeId) {
		this.contentTypeId = contentTypeId;
	}
	
	public String getContentId() {
		return contentId;
	}
	
	public void setContentId(String contentId) {
		this.contentId = contentId;
	}
	
	public Integer getUserId() {
		return userId;
	}
	
	public void setUserId(Integer userId) {
		this.userId = userId;
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
	
	public String getSecurityTemplateName() {
		return securityTemplateName;
	}
	
	public void setSecurityTemplateName(String securityTemplateName) {
		this.securityTemplateName = securityTemplateName;
	}

	@Override
	public String toString() {
		return "ContentSecurity [contentSecurityId=" + contentSecurityId
				+ ", contentTypeId=" + contentTypeId + ", contentId="
				+ contentId + ", userId=" + userId + ", solutionId="
				+ solutionId + ", orgId=" + orgId + ", securityTemplateName="
				+ securityTemplateName + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((contentId == null) ? 0 : contentId.hashCode());
		result = prime
				* result
				+ ((contentSecurityId == null) ? 0 : contentSecurityId
						.hashCode());
		result = prime * result
				+ ((contentTypeId == null) ? 0 : contentTypeId.hashCode());
		result = prime * result + ((orgId == null) ? 0 : orgId.hashCode());
		result = prime
				* result
				+ ((securityTemplateName == null) ? 0 : securityTemplateName
						.hashCode());
		result = prime * result
				+ ((solutionId == null) ? 0 : solutionId.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
		ContentSecurity other = (ContentSecurity) obj;
		if (contentId == null) {
			if (other.contentId != null)
				return false;
		} else if (!contentId.equals(other.contentId))
			return false;
		if (contentSecurityId == null) {
			if (other.contentSecurityId != null)
				return false;
		} else if (!contentSecurityId.equals(other.contentSecurityId))
			return false;
		if (contentTypeId == null) {
			if (other.contentTypeId != null)
				return false;
		} else if (!contentTypeId.equals(other.contentTypeId))
			return false;
		if (orgId == null) {
			if (other.orgId != null)
				return false;
		} else if (!orgId.equals(other.orgId))
			return false;
		if (securityTemplateName == null) {
			if (other.securityTemplateName != null)
				return false;
		} else if (!securityTemplateName.equals(other.securityTemplateName))
			return false;
		if (solutionId == null) {
			if (other.solutionId != null)
				return false;
		} else if (!solutionId.equals(other.solutionId))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}
	

}
