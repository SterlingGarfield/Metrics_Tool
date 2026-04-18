package com.metrics.exception;

public class FeatureNotReadyException extends RuntimeException {

    public FeatureNotReadyException(String message) {
        super(message);
    }

    public FeatureNotReadyException(String message, Throwable cause) {
        super(message, cause);
    }
}
