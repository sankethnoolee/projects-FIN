package com.fintellix.framework.validation.dto;

import java.io.Serializable;
import java.util.Date;

public class ValidationReturnResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer runId;
    private Integer solutionId;
    private Integer periodId;
    private Integer regReportId;
    private Integer regReportVersionNumber;
    private Integer versionNumber;
    private Integer orgId;
    private Date startDate;
    private Date endDate;
    private String status;


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

    public Integer getRegReportId() {
        return regReportId;
    }

    public void setRegReportId(Integer regReportId) {
        this.regReportId = regReportId;
    }

    public Integer getRegReportVersionNumber() {
        return regReportVersionNumber;
    }

    public void setRegReportVersionNumber(Integer regReportVersionNumber) {
        this.regReportVersionNumber = regReportVersionNumber;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ValidationReturnResult [runId=" + runId + ", solutionId=" + solutionId + ", periodId=" + periodId
                + ", regReportId=" + regReportId + ", regReportVersionNumber=" + regReportVersionNumber
                + ", versionNumber=" + versionNumber + ", orgId=" + orgId + ", startDate=" + startDate + ", endDate="
                + endDate + ", status=" + status + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
        result = prime * result + ((orgId == null) ? 0 : orgId.hashCode());
        result = prime * result + ((periodId == null) ? 0 : periodId.hashCode());
        result = prime * result + ((regReportId == null) ? 0 : regReportId.hashCode());
        result = prime * result + ((regReportVersionNumber == null) ? 0 : regReportVersionNumber.hashCode());
        result = prime * result + ((runId == null) ? 0 : runId.hashCode());
        result = prime * result + ((solutionId == null) ? 0 : solutionId.hashCode());
        result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((versionNumber == null) ? 0 : versionNumber.hashCode());
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
        ValidationReturnResult other = (ValidationReturnResult) obj;
        if (endDate == null) {
            if (other.endDate != null)
                return false;
        } else if (!endDate.equals(other.endDate))
            return false;
        if (orgId == null) {
            if (other.orgId != null)
                return false;
        } else if (!orgId.equals(other.orgId))
            return false;
        if (periodId == null) {
            if (other.periodId != null)
                return false;
        } else if (!periodId.equals(other.periodId))
            return false;
        if (regReportId == null) {
            if (other.regReportId != null)
                return false;
        } else if (!regReportId.equals(other.regReportId))
            return false;
        if (regReportVersionNumber == null) {
            if (other.regReportVersionNumber != null)
                return false;
        } else if (!regReportVersionNumber.equals(other.regReportVersionNumber))
            return false;
        if (runId == null) {
            if (other.runId != null)
                return false;
        } else if (!runId.equals(other.runId))
            return false;
        if (solutionId == null) {
            if (other.solutionId != null)
                return false;
        } else if (!solutionId.equals(other.solutionId))
            return false;
        if (startDate == null) {
            if (other.startDate != null)
                return false;
        } else if (!startDate.equals(other.startDate))
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        if (versionNumber == null) {
            if (other.versionNumber != null)
                return false;
        } else if (!versionNumber.equals(other.versionNumber))
            return false;
        return true;
    }

}
