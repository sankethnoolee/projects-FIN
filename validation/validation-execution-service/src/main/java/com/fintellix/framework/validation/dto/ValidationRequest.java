package com.fintellix.framework.validation.dto;

import java.io.Serializable;
import java.util.Date;

public class ValidationRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer runId;
    private Integer solutionId;
    private Integer periodId;
    private Integer orgId;
    private Date requestStartDate;
    private Date requestEndDate;
    private String entityType;
    private Integer userId;
    private String requestStatus;
    private String orgCode;
    private String payload;
    private String entityCode;

    public Integer getRunId() {
        return runId;
    }

    public void setRunId(Integer runId) {
        this.runId = runId;
    }

    public Integer getSolutionId() {
        return solutionId;
    }

    public void setSolutionId(Integer solutionId) {
        this.solutionId = solutionId;
    }

    public Integer getPeriodId() {
        return periodId;
    }

    public void setPeriodId(Integer periodId) {
        this.periodId = periodId;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }

    public Date getRequestStartDate() {
        return requestStartDate;
    }

    public void setRequestStartDate(Date requestStartDate) {
        this.requestStartDate = requestStartDate;
    }

    public Date getRequestEndDate() {
        return requestEndDate;
    }

    public void setRequestEndDate(Date requestEndDate) {
        this.requestEndDate = requestEndDate;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getEntityCode() {
        return entityCode;
    }

    public void setEntityCode(String entityCode) {
        this.entityCode = entityCode;
    }

    @Override
    public String toString() {
        return "ValidationRequest{" +
                "runId=" + runId +
                ", solutionId=" + solutionId +
                ", periodId=" + periodId +
                ", orgId=" + orgId +
                ", requestStartDate=" + requestStartDate +
                ", requestEndDate=" + requestEndDate +
                ", entityType='" + entityType + '\'' +
                ", userId=" + userId +
                ", requestStatus='" + requestStatus + '\'' +
                ", orgCode='" + orgCode + '\'' +
                ", payload='" + payload + '\'' +
                ", entityCode='" + entityCode + '\'' +
                '}';
    }
}
