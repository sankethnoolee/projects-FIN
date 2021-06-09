package com.fintellix.framework.validation.dto;

import java.io.Serializable;

public class ValidationReportLink implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer validationId;
	private Integer sequenceNo;
	private Integer solutionId;
	private Integer regReportId;
	private Integer regReportSectionId;
	private Integer validationGroupId;
	private String validationCategory;
	private String commentLevel;
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
	public Integer getRegReportSectionId() {
		return regReportSectionId;
	}
	public void setRegReportSectionId(Integer regReportSectionId) {
		this.regReportSectionId = regReportSectionId;
	}
	public Integer getValidationGroupId() {
		return validationGroupId;
	}
	public void setValidationGroupId(Integer validationGroupId) {
		this.validationGroupId = validationGroupId;
	}
	public String getValidationCategory() {
		return validationCategory;
	}
	public void setValidationCategory(String validationCategory) {
		this.validationCategory = validationCategory;
	}
	public String getCommentLevel() {
		return commentLevel;
	}
	public void setCommentLevel(String commentLevel) {
		this.commentLevel = commentLevel;
	}
		
}
