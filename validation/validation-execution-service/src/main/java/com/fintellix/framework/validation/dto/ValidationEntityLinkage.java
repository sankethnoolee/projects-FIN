package com.fintellix.framework.validation.dto;

import java.io.Serializable;

public class ValidationEntityLinkage implements Serializable {
    private Integer validationId;
    private Integer sequenceNo;
    private Integer solutionId;
    private String validationCategory;
    private Integer validationGroupId;

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

    public String getValidationCategory() {
        return validationCategory;
    }

    public void setValidationCategory(String validationCategory) {
        this.validationCategory = validationCategory;
    }

    public Integer getValidationGroupId() {
        return validationGroupId;
    }

    public void setValidationGroupId(Integer validationGroupId) {
        this.validationGroupId = validationGroupId;
    }
}
