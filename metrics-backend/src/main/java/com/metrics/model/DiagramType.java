package com.metrics.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DiagramType {
    CLASS("class"),
    FLOW("flow"),
    USECASE("usecase");

    private final String value;

    DiagramType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static DiagramType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (DiagramType diagramType : values()) {
            if (diagramType.value.equals(value.trim())) {
                return diagramType;
            }
        }
        throw new IllegalArgumentException("diagramType must be one of: class, flow, usecase");
    }

    @Override
    public String toString() {
        return value;
    }
}
