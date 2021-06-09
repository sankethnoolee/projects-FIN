package com.fintellix.framework.validation.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


public class ValidationLineItemLink implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer validationId;
	private Integer sequenceNo;
	private Integer solutionId;
	private Integer regReportId;
	private Integer sectionId;
	private String lineItemName;
	public Integer getValidationId() {
		return validationId;
	}
	public void setValidationId(Integer validationId) {
		this.validationId = validationId;
	}
	public Integer getSequenceNo() {
		return sequenceNo;
	}
	public void setSequenceNo(Integer sequenceNo) {
		this.sequenceNo = sequenceNo;
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
	public Integer getSectionId() {
		return sectionId;
	}
	public void setSectionId(Integer sectionId) {
		this.sectionId = sectionId;
	}
	public String getLineItemName() {
		return lineItemName;
	}
	public void setLineItemName(String lineItemName) {
		this.lineItemName = lineItemName;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lineItemName == null) ? 0 : lineItemName.hashCode());
		result = prime * result + ((regReportId == null) ? 0 : regReportId.hashCode());
		result = prime * result + ((sectionId == null) ? 0 : sectionId.hashCode());
		result = prime * result + ((sequenceNo == null) ? 0 : sequenceNo.hashCode());
		result = prime * result + ((solutionId == null) ? 0 : solutionId.hashCode());
		result = prime * result + ((validationId == null) ? 0 : validationId.hashCode());
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
		ValidationLineItemLink other = (ValidationLineItemLink) obj;
		if (lineItemName == null) {
			if (other.lineItemName != null)
				return false;
		} else if (!lineItemName.equals(other.lineItemName))
			return false;
		if (regReportId == null) {
			if (other.regReportId != null)
				return false;
		} else if (!regReportId.equals(other.regReportId))
			return false;
		if (sectionId == null) {
			if (other.sectionId != null)
				return false;
		} else if (!sectionId.equals(other.sectionId))
			return false;
		
		if (sequenceNo == null) {
			if (other.sequenceNo != null)
				return false;
		} else if (!sequenceNo.equals(other.sequenceNo))
			return false;
		if (solutionId == null) {
			if (other.solutionId != null)
				return false;
		} else if (!solutionId.equals(other.solutionId))
			return false;
		if (validationId == null) {
			if (other.validationId != null)
				return false;
		} else if (!validationId.equals(other.validationId))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "ValidationLineItemLink [validationId=" + validationId + ", sequenceNo=" + sequenceNo + ", solutionId="
				+ solutionId + ", regReportId=" + regReportId + ", sectionId=" + sectionId + ", lineItemName="
				+ lineItemName + "]";
	}
	
	
}
