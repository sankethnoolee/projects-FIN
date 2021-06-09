package com.fintellix.validationrestservice.definition;

import java.io.Serializable;

public class RefMetadataInfo implements Serializable {
    private Integer periodId;
    private Integer orgId;
    private String orgCode;

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

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((periodId == null) ? 0 : periodId.hashCode());
        result = prime * result + ((orgCode == null) ? 0 : orgCode.hashCode());
        result = prime * result + ((orgId == null) ? 0 : orgId.hashCode());
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
        RefMetadataInfo other = (RefMetadataInfo) obj;
        if (periodId == null) {
            if (other.periodId != null)
                return false;
        } else if (!periodId.equals(other.periodId))
            return false;
        if (orgCode == null) {
            if (other.orgCode != null)
                return false;
        } else if (!orgCode.equals(other.orgCode))
            return false;
        if (orgId == null) {
            return other.orgId == null;
        } else return orgId.equals(other.orgId);
    }

    @Override
    public String toString() {
        return "RefMetadataInfo{" +
                "periodId=" + periodId +
                ", orgId=" + orgId +
                ", orgCode='" + orgCode + '\'' +
                '}';
    }
}
