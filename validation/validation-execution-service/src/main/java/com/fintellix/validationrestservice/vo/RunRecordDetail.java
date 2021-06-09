package com.fintellix.validationrestservice.vo;

public class RunRecordDetail {
    private boolean validationResult;
    private String dimensionsCSV;
    private boolean hasError;
    private String replacedExpression;

    public RunRecordDetail() {
    }
    
    public RunRecordDetail(boolean validationResult, String dimensionsCSV, boolean hasError, String replacedExpression) {
        this.validationResult = validationResult;
        this.dimensionsCSV = dimensionsCSV;
        this.hasError = hasError;
        this.replacedExpression = replacedExpression;
    }

    public boolean isValidationResult() {
        return validationResult;
    }

    public void setValidationResult(boolean validationResult) {
        this.validationResult = validationResult;
    }

    public String getDimensionsCSV() {
        return dimensionsCSV;
    }

    public void setDimensionsCSV(String dimensionsCSV) {
        this.dimensionsCSV = dimensionsCSV;
    }

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public String getReplacedExpression() {
        return replacedExpression;
    }

    public void setReplacedExpression(String replacedExpression) {
        this.replacedExpression = replacedExpression;
    }

    @Override
    public String toString() {
        return "RunRecordDetail{" +
                "validationResult=" + validationResult +
                ", dimensionsCSV='" + dimensionsCSV + '\'' +
                ", hasError=" + hasError +
                ", replacedExpression='" + replacedExpression + '\'' +
                '}';
    }
}
