package com.fintellix.validationrestservice.definition;

import java.io.Serializable;

public class ReturnMetadataInfo implements Serializable {
    private Integer periodId;
    private String returnStatus;
    private Integer orgId;
    private String orgCode;
    private Integer reportVersion;
    private Integer versionNo;

    public Integer getPeriodId() {
        return periodId;
    }

    public void setPeriodId(Integer periodId) {
        this.periodId = periodId;
    }

    public String getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(String returnStatus) {
        this.returnStatus = returnStatus;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public Integer getReportVersion() {
        return reportVersion;
    }

    public void setReportVersion(Integer reportVersion) {
        this.reportVersion = reportVersion;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((orgCode == null) ? 0 : orgCode.hashCode());
        result = prime * result + ((orgId == null) ? 0 : orgId.hashCode());
        result = prime * result + ((periodId == null) ? 0 : periodId.hashCode());
        result = prime * result + ((reportVersion == null) ? 0 : reportVersion.hashCode());
        result = prime * result + ((returnStatus == null) ? 0 : returnStatus.hashCode());
        result = prime * result + ((versionNo == null) ? 0 : versionNo.hashCode());
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
        ReturnMetadataInfo other = (ReturnMetadataInfo) obj;
        if (orgCode == null) {
            if (other.orgCode != null)
                return false;
        } else if (!orgCode.equals(other.orgCode))
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
        if (reportVersion == null) {
            if (other.reportVersion != null)
                return false;
        } else if (!reportVersion.equals(other.reportVersion))
            return false;
        if (returnStatus == null) {
            if (other.returnStatus != null)
                return false;
        } else if (!returnStatus.equals(other.returnStatus))
            return false;
        if (versionNo == null) {
            if (other.versionNo != null)
                return false;
        } else if (!versionNo.equals(other.versionNo))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ReturnMetadataInfo [periodId=" + periodId + ", returnStatus=" + returnStatus + ", orgId=" + orgId
                + ", orgCode=" + orgCode + ", reportVersion=" + reportVersion + ", versionNo=" + versionNo + "]";
    }
}