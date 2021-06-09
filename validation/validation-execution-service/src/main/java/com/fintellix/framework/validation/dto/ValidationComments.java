package com.fintellix.framework.validation.dto;

import java.io.Serializable;
import java.util.Date;

public class ValidationComments implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer periodId;
    private Integer regReportId;
    private Integer versionNumber;
    private Integer validationId;
    private String occurrence;
    private Integer orgId;
    private String comment;
    private String commentHistory;
    private Date lastModificationDate;
    private Integer lastModifiedByUserId;
    private String isMigrated;

    public ValidationComments() {
    }

    public ValidationComments(Integer periodId, Integer regReportId, Integer versionNumber, Integer validationId,
                              String occurrence, Integer orgId, String comment, Date lastModificationDate,
                              Integer lastModifiedByUserId, String isMigrated) {
        this.periodId = periodId;
        this.regReportId = regReportId;
        this.versionNumber = versionNumber;
        this.validationId = validationId;
        this.occurrence = occurrence;
        this.orgId = orgId;
        this.comment = comment;
        this.lastModificationDate = lastModificationDate;
        this.lastModifiedByUserId = lastModifiedByUserId;
        this.isMigrated = isMigrated;
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

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public Integer getValidationId() {
        return validationId;
    }

    public void setValidationId(Integer validationId) {
        this.validationId = validationId;
    }

    public String getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(String occurrence) {
        this.occurrence = occurrence;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentHistory() {
        return commentHistory;
    }

    public void setCommentHistory(String commentHistory) {
        this.commentHistory = commentHistory;
    }

    public Date getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public Integer getLastModifiedByUserId() {
        return lastModifiedByUserId;
    }

    public void setLastModifiedByUserId(Integer lastModifiedByUserId) {
        this.lastModifiedByUserId = lastModifiedByUserId;
    }

    public String getIsMigrated() {
        return isMigrated;
    }

    public void setIsMigrated(String isMigrated) {
        this.isMigrated = isMigrated;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((comment == null) ? 0 : comment.hashCode());
        result = prime * result + ((commentHistory == null) ? 0 : commentHistory.hashCode());
        result = prime * result + ((occurrence == null) ? 0 : occurrence.hashCode());
        result = prime * result + ((orgId == null) ? 0 : orgId.hashCode());
        result = prime * result + ((periodId == null) ? 0 : periodId.hashCode());
        result = prime * result + ((regReportId == null) ? 0 : regReportId.hashCode());
        result = prime * result + ((validationId == null) ? 0 : validationId.hashCode());
        result = prime * result + ((versionNumber == null) ? 0 : versionNumber.hashCode());
        result = prime * result + ((lastModificationDate == null) ? 0 : lastModificationDate.hashCode());
        result = prime * result + ((lastModifiedByUserId == null) ? 0 : lastModifiedByUserId.hashCode());
        result = prime * result + ((isMigrated == null) ? 0 : isMigrated.hashCode());
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
        ValidationComments other = (ValidationComments) obj;
        if (comment == null) {
            if (other.comment != null)
                return false;
        } else if (!comment.equals(other.comment))
            return false;
        if (commentHistory == null) {
            if (other.commentHistory != null)
                return false;
        } else if (!commentHistory.equals(other.commentHistory))
            return false;
        if (occurrence == null) {
            if (other.occurrence != null)
                return false;
        } else if (!occurrence.equals(other.occurrence))
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
        if (validationId == null) {
            if (other.validationId != null)
                return false;
        } else if (!validationId.equals(other.validationId))
            return false;
        if (versionNumber == null) {
            if (other.versionNumber != null)
                return false;
        } else if (!versionNumber.equals(other.versionNumber))
            return false;
        if (lastModificationDate == null) {
            if (other.lastModificationDate != null)
                return false;
        } else if (!lastModificationDate.equals(other.lastModificationDate))
            return false;
        if (lastModifiedByUserId == null) {
            if (other.lastModifiedByUserId != null)
                return false;
        } else if (!lastModifiedByUserId.equals(other.lastModifiedByUserId))
            return false;
        if (isMigrated == null) {
            if (other.isMigrated != null)
                return false;
        } else if (!isMigrated.equals(other.isMigrated))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ValidationComments [periodId=" + periodId + ", regReportId=" + regReportId + ", versionNumber="
                + versionNumber + ", validationId=" + validationId + ", occurrence=" + occurrence + ", orgId=" + orgId
                + ", comment=" + comment + ", commentHistory=" + commentHistory + ", lastModificationDate="
                + lastModificationDate + ", lastModifiedByUserId=" + lastModifiedByUserId
                + ", isMigrated=" + isMigrated + "]";
    }
}
