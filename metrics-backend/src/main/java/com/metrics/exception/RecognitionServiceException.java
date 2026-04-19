package com.metrics.exception;

public class RecognitionServiceException extends RuntimeException {

    public RecognitionServiceException(String message) {
        super(message);
    }

    public RecognitionServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
