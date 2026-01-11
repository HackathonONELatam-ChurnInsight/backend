package com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    MALE("Male"),
    FEMALE("Female");

    private final String value;

    Gender(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Gender from(String value) {
        for (Gender g : Gender.values()) {
            if (g.value.equalsIgnoreCase(value)) {
                return g;
            }
        }
        throw new IllegalArgumentException("El campo 'gender' debe ser: Male o Female");
    }
}
