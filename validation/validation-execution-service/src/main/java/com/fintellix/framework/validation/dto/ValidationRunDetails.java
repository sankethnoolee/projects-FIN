package com.fintellix.framework.validation.dto;

import javax.persistence.Transient;
import java.io.Serializable;

public class ValidationRunDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer runId;
    private Integer validationId;
    private Integer sequenceNumber;
    private String status;
    private String evaluatedExpression;
    private Integer totalOccurrence;
    private Integer totalFailed;
    private String validationType;
    private String dimensionsCSV;
    private String replacedExpression;

    @Transient
    private String groupFolderName;

    @Transient
    private String groupCsvName;

    public Integer getRunId() {
        return runId;
    }

    public void setRunId(Integer runId) {
        this.runId = runId;
    }

    public Integer getValidationId() {
        return validationId;
    }

    public void setValidationId(Integer validationId) {
        this.validationId = validationId;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEvaluatedExpression() {
        return evaluatedExpression;
    }

    public void setEvaluatedExpression(String evaluatedExpression) {
        this.evaluatedExpression = evaluatedExpression;
    }

    public Integer getTotalOccurrence() {
        return totalOccurrence;
    }

    public void setTotalOccurrence(Integer totalOccurrence) {
        this.totalOccurrence = totalOccurrence;
    }

    public Integer getTotalFailed() {
        return totalFailed;
    }

    public void setTotalFailed(Integer totalFailed) {
        this.totalFailed = totalFailed;
    }

    public String getValidationType() {
        return validationType;
    }

    public void setValidationType(String validationType) {
        this.validationType = validationType;
    }

    public String getDimensionsCSV() {
        return dimensionsCSV;
    }

    public void setDimensionsCSV(String dimensionsCSV) {
        this.dimensionsCSV = dimensionsCSV;
    }

    public String getReplacedExpression() {
        return replacedExpression;
    }

    public void setReplacedExpression(String replacedExpression) {
        this.replacedExpression = replacedExpression;
    }

    public String getGroupFolderName() {
        return groupFolderName;
    }

    public void setGroupFolderName(String groupFolderName) {
        this.groupFolderName = groupFolderName;
    }

    public String getGroupCsvName() {
        return groupCsvName;
    }

    public void setGroupCsvName(String groupCsvName) {
        this.groupCsvName = groupCsvName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((evaluatedExpression == null) ? 0 : evaluatedExpression.hashCode());
        result = prime * result + ((runId == null) ? 0 : runId.hashCode());
        result = prime * result + ((sequenceNumber == null) ? 0 : sequenceNumber.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((totalFailed == null) ? 0 : totalFailed.hashCode());
        result = prime * result + ((totalOccurrence == null) ? 0 : totalOccurrence.hashCode());
        result = prime * result + ((validationId == null) ? 0 : validationId.hashCode());
        result = prime * result + ((validationType == null) ? 0 : validationType.hashCode());
        result = prime * result + ((dimensionsCSV == null) ? 0 : dimensionsCSV.hashCode());
        result = prime * result + ((replacedExpression == null) ? 0 : replacedExpression.hashCode());
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
        ValidationRunDetails other = (ValidationRunDetails) obj;
        if (evaluatedExpression == null) {
            if (other.evaluatedExpression != null)
                return false;
        } else if (!evaluatedExpression.equals(other.evaluatedExpression))
            return false;
        if (runId == null) {
            if (other.runId != null)
                return false;
        } else if (!runId.equals(other.runId))
            return false;
        if (sequenceNumber == null) {
            if (other.sequenceNumber != null)
                return false;
        } else if (!sequenceNumber.equals(other.sequenceNumber))
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        if (totalFailed == null) {
            if (other.totalFailed != null)
                return false;
        } else if (!totalFailed.equals(other.totalFailed))
            return false;
        if (totalOccurrence == null) {
            if (other.totalOccurrence != null)
                return false;
        } else if (!totalOccurrence.equals(other.totalOccurrence))
            return false;
        if (validationId == null) {
            if (other.validationId != null)
                return false;
        } else if (!validationId.equals(other.validationId))
            return false;
        if (validationType == null) {
            if (other.validationType != null)
                return false;
        } else if (!validationType.equals(other.validationType))
            return false;
        if (dimensionsCSV == null) {
            if (other.dimensionsCSV != null)
                return false;
        } else if (!dimensionsCSV.equals(other.dimensionsCSV))
            return false;
        if (replacedExpression == null) {
            if (other.replacedExpression != null)
                return false;
        } else if (!replacedExpression.equals(other.replacedExpression))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ValidationRunDetails [runId=" + runId + ", validationId=" + validationId + ", sequenceNumber="
                + sequenceNumber + ", status=" + status + ", evaluatedExpression=" + evaluatedExpression
                + ", totalOccurrence=" + totalOccurrence + ", totalFailed=" + totalFailed + ", validationType="
                + validationType + ", dimensionsCSV=" + dimensionsCSV + ", replacedExpression=" + replacedExpression + "]";
    }

}
