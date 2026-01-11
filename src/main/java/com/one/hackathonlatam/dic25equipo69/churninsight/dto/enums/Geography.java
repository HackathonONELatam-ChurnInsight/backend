package com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Geography {
    SPAIN("Spain"),
    FRANCE("France"),
    GERMANY("Germany");

    private final String value;

    Geography(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Geography from(String value) {
        for (Geography g : Geography.values()) {
            if (g.value.equalsIgnoreCase(value)) {
                return g;
            }
        }
        throw new IllegalArgumentException("El campo 'geography' debe ser: France, Spain o Germany");
    }
}
