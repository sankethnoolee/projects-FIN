package com.fintellix.validationrestservice.exception;

public class BaseValidationException extends RuntimeException{
    public BaseValidationException() {
        super();
    }

    public BaseValidationException(String message) {
        super(message);
    }

    public BaseValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseValidationException(Throwable cause) {
        super(cause);
    }

    protected BaseValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
