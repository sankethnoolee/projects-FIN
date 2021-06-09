package com.fintellix.platform.framework.dataLoadDashboardApi.dto;

import java.io.Serializable;

public class RequestPayLoad implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String otp;
	private String clientCode;
	private String taskRepo;
	private String taskName;
	private String flowType;
	private String flowSeqNo;
	private String runStatus;
	private String taskTechName;
	private String taskTechSubName;
	private String runDetails;
	private String srcCnt;
	private String tgtCnt;
	private String rejectedRows;
	private String affectedRows;
	private String appliedRows;
	private String taskStartTime;
	private String taskEndTime;
	private String runPeriodId;
	private String businessPeriodId;
	
	public String getOtp() {
		return otp;
	}
	public void setOtp(String otp) {
		this.otp = otp;
	}
	public String getClientCode() {
		return clientCode;
	}
	public void setClientCode(String clientCode) {
		this.clientCode = clientCode;
	}
	public String getTaskRepo() {
		return taskRepo;
	}
	public void setTaskRepo(String taskRepo) {
		this.taskRepo = taskRepo;
	}
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	public String getFlowType() {
		return flowType;
	}
	public void setFlowType(String flowType) {
		this.flowType = flowType;
	}
	public String getFlowSeqNo() {
		return flowSeqNo;
	}
	public void setFlowSeqNo(String flowSeqNo) {
		this.flowSeqNo = flowSeqNo;
	}
	public String getRunStatus() {
		return runStatus;
	}
	public void setRunStatus(String runStatus) {
		this.runStatus = runStatus;
	}
	public String getTaskTechName() {
		return taskTechName;
	}
	public void setTaskTechName(String taskTechName) {
		this.taskTechName = taskTechName;
	}
	public String getTaskTechSubName() {
		return taskTechSubName;
	}
	public void setTaskTechSubName(String taskTechSubName) {
		this.taskTechSubName = taskTechSubName;
	}
	public String getRunDetails() {
		return runDetails;
	}
	public void setRunDetails(String runDetails) {
		this.runDetails = runDetails;
	}
	public String getSrcCnt() {
		return srcCnt;
	}
	public void setSrcCnt(String srcCnt) {
		this.srcCnt = srcCnt;
	}
	public String getTgtCnt() {
		return tgtCnt;
	}
	public void setTgtCnt(String tgtCnt) {
		this.tgtCnt = tgtCnt;
	}
	public String getRejectedRows() {
		return rejectedRows;
	}
	public void setRejectedRows(String rejectedRows) {
		this.rejectedRows = rejectedRows;
	}
	public String getAffectedRows() {
		return affectedRows;
	}
	public void setAffectedRows(String affectedRows) {
		this.affectedRows = affectedRows;
	}
	public String getAppliedRows() {
		return appliedRows;
	}
	public void setAppliedRows(String appliedRows) {
		this.appliedRows = appliedRows;
	}
	public String getTaskStartTime() {
		return taskStartTime;
	}
	public void setTaskStartTime(String taskStartTime) {
		this.taskStartTime = taskStartTime;
	}
	public String getTaskEndTime() {
		return taskEndTime;
	}
	public void setTaskEndTime(String taskEndTime) {
		this.taskEndTime = taskEndTime;
	}
	public String getRunPeriodId() {
		return runPeriodId;
	}
	public void setRunPeriodId(String runPeriodId) {
		this.runPeriodId = runPeriodId;
	}
	public String getBusinessPeriodId() {
		return businessPeriodId;
	}
	public void setBusinessPeriodId(String businessPeriodId) {
		this.businessPeriodId = businessPeriodId;
	}
	@Override
	public String toString() {
		return "RequestPayLoad [otp=" + otp + ", clientCode=" + clientCode
				+ ", taskRepo=" + taskRepo + ", taskName=" + taskName
				+ ", flowType=" + flowType + ", flowSeqNo=" + flowSeqNo
				+ ", runStatus=" + runStatus + ", taskTechName=" + taskTechName
				+ ", taskTechSubName=" + taskTechSubName + ", runDetails="
				+ runDetails + ", srcCnt=" + srcCnt + ", tgtCnt=" + tgtCnt
				+ ", rejectedRows=" + rejectedRows + ", affectedRows="
				+ affectedRows + ", appliedRows=" + appliedRows
				+ ", taskStartTime=" + taskStartTime + ", taskEndTime="
				+ taskEndTime + ", runPeriodId=" + runPeriodId
				+ ", businessPeriodId=" + businessPeriodId + "]";
	}
	

	

}
