package com.fintellix.validationrestservice.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Deepak Moudgil
 */
@ResponseStatus(reason = "Failed to process download!")
public class DownloadFailureException extends BaseValidationException {

    public DownloadFailureException() {
    }

    public DownloadFailureException(String message) {
        super(message);
    }
}
