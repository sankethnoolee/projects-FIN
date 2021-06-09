package com.fintellix.validationrestservice.vo;

public class RequestResponse {
    private Boolean hasError = false;
    private String message;
    private QueryWithColumnDetail model;
    private Integer errorCode;

    public Boolean getHasError() {
        return hasError;
    }

    public void setHasError(Boolean hasError) {
        this.hasError = hasError;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public QueryWithColumnDetail getModel() {
        return model;
    }

    public void setModel(QueryWithColumnDetail model) {
        this.model = model;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }
}
