package com.metrics.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EstimationMethod {
    UCP("ucp"),
    FUNCTION_POINT("function_point");

    private final String value;

    EstimationMethod(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static EstimationMethod fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (EstimationMethod estimationMethod : values()) {
            if (estimationMethod.value.equals(value.trim())) {
                return estimationMethod;
            }
        }
        throw new IllegalArgumentException("estimationMethod must be one of: ucp, function_point");
    }

    @Override
    public String toString() {
        return value;
    }
}
