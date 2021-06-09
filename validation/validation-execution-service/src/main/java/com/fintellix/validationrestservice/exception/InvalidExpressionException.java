package com.fintellix.validationrestservice.exception;

public class InvalidExpressionException extends BaseValidationException{
    public InvalidExpressionException() {
        super();
    }

    public InvalidExpressionException(String message) {
        super(message);
    }

    public InvalidExpressionException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidExpressionException(Throwable cause) {
        super(cause);
    }

    protected InvalidExpressionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
