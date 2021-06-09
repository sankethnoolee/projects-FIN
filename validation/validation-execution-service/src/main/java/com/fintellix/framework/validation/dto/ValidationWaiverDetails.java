package com.fintellix.framework.validation.dto;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "VALIDATION_WAIVER")
public class ValidationWaiverDetails {	
	
	@EmbeddedId
	ValidationWaiverId validationWaiverId;

	@Column(name = "WAIVER_TITLE")
	private String waiverTitle;

	@Column(name = "ORG_ID")
	private Integer orgId;

	@Column(name = "SOLUTION_ID")
	private Integer solutionId;

	@Column(name = "REG_REPORT_ID")
	private Integer regReportId;

	@Column(name = "IS_ACTIVE")
	private Integer isActive;
	
	@Column(name = "IS_ACTIVE_VERSION")
	private Integer isActiveVersion;

	@Column(name = "IS_DELETED")
	private Integer isDeleted;

	@Column(name = "WAIVER_INFO")
	private String waiverInfo;

	@Column(name = "CREATED_TIME")
	private Long createdTime;

	@Column(name = "LAST_MODIFIED_TIME")
	private Long lastModifiedTime;

	@Column(name = "CREATED_BY")
	private Integer createdBy;

	@Column(name = "LAST_MODIFIED_BY")
	private Integer lastModifiedBy;

	public ValidationWaiverId getValidationWaiverId() {
		return validationWaiverId;
	}

	public void setValidationWaiverId(ValidationWaiverId validationWaiverId) {
		this.validationWaiverId = validationWaiverId;
	}

	public String getWaiverTitle() {
		return waiverTitle;
	}

	public void setWaiverTitle(String waiverTitle) {
		this.waiverTitle = waiverTitle;
	}

	public Integer getOrgId() {
		return orgId;
	}

	public void setOrgId(Integer orgId) {
		this.orgId = orgId;
	}

	public Integer getSolutionId() {
		return solutionId;
	}

	public void setSolutionId(Integer solutionId) {
		this.solutionId = solutionId;
	}

	public Integer getRegReportId() {
		return regReportId;
	}

	public void setRegReportId(Integer regReportId) {
		this.regReportId = regReportId;
	}

	public Integer getIsActive() {
		return isActive;
	}

	public void setIsActive(Integer isActive) {
		this.isActive = isActive;
	}

	public Integer getIsActiveVersion() {
		return isActiveVersion;
	}

	public void setIsActiveVersion(Integer isActiveVersion) {
		this.isActiveVersion = isActiveVersion;
	}

	public Integer getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Integer isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getWaiverInfo() {
		return waiverInfo;
	}

	public void setWaiverInfo(String waiverInfo) {
		this.waiverInfo = waiverInfo;
	}

	public Long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Long createdTime) {
		this.createdTime = createdTime;
	}

	public Long getLastModifiedTime() {
		return lastModifiedTime;
	}

	public void setLastModifiedTime(Long lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}

	public Integer getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Integer createdBy) {
		this.createdBy = createdBy;
	}

	public Integer getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(Integer lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdBy == null) ? 0 : createdBy.hashCode());
		result = prime * result + ((createdTime == null) ? 0 : createdTime.hashCode());
		result = prime * result + ((isActive == null) ? 0 : isActive.hashCode());
		result = prime * result + ((isActiveVersion == null) ? 0 : isActiveVersion.hashCode());
		result = prime * result + ((isDeleted == null) ? 0 : isDeleted.hashCode());
		result = prime * result + ((lastModifiedBy == null) ? 0 : lastModifiedBy.hashCode());
		result = prime * result + ((lastModifiedTime == null) ? 0 : lastModifiedTime.hashCode());
		result = prime * result + ((orgId == null) ? 0 : orgId.hashCode());
		result = prime * result + ((regReportId == null) ? 0 : regReportId.hashCode());
		result = prime * result + ((solutionId == null) ? 0 : solutionId.hashCode());
		result = prime * result + ((validationWaiverId == null) ? 0 : validationWaiverId.hashCode());
		result = prime * result + ((waiverInfo == null) ? 0 : waiverInfo.hashCode());
		result = prime * result + ((waiverTitle == null) ? 0 : waiverTitle.hashCode());
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
		ValidationWaiverDetails other = (ValidationWaiverDetails) obj;
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
		if (isActive == null) {
			if (other.isActive != null)
				return false;
		} else if (!isActive.equals(other.isActive))
			return false;
		if (isActiveVersion == null) {
			if (other.isActiveVersion != null)
				return false;
		} else if (!isActiveVersion.equals(other.isActiveVersion))
			return false;
		if (isDeleted == null) {
			if (other.isDeleted != null)
				return false;
		} else if (!isDeleted.equals(other.isDeleted))
			return false;
		if (lastModifiedBy == null) {
			if (other.lastModifiedBy != null)
				return false;
		} else if (!lastModifiedBy.equals(other.lastModifiedBy))
			return false;
		if (lastModifiedTime == null) {
			if (other.lastModifiedTime != null)
				return false;
		} else if (!lastModifiedTime.equals(other.lastModifiedTime))
			return false;
		if (orgId == null) {
			if (other.orgId != null)
				return false;
		} else if (!orgId.equals(other.orgId))
			return false;
		if (regReportId == null) {
			if (other.regReportId != null)
				return false;
		} else if (!regReportId.equals(other.regReportId))
			return false;
		if (solutionId == null) {
			if (other.solutionId != null)
				return false;
		} else if (!solutionId.equals(other.solutionId))
			return false;
		if (validationWaiverId == null) {
			if (other.validationWaiverId != null)
				return false;
		} else if (!validationWaiverId.equals(other.validationWaiverId))
			return false;
		if (waiverInfo == null) {
			if (other.waiverInfo != null)
				return false;
		} else if (!waiverInfo.equals(other.waiverInfo))
			return false;
		if (waiverTitle == null) {
			if (other.waiverTitle != null)
				return false;
		} else if (!waiverTitle.equals(other.waiverTitle))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ValidationWaiverDetails [validationWaiverId=" + validationWaiverId + ", waiverTitle=" + waiverTitle
				+ ", orgId=" + orgId + ", solutionId=" + solutionId + ", regReportId=" + regReportId + ", isActive="
				+ isActive + ", isActiveVersion=" + isActiveVersion + ", isDeleted=" + isDeleted + ", waiverInfo="
				+ waiverInfo + ", createdTime=" + createdTime + ", lastModifiedTime=" + lastModifiedTime
				+ ", createdBy=" + createdBy + ", lastModifiedBy=" + lastModifiedBy + "]";
	}

	
	
	
}
