package com.operationt.self.paksha.query.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FunctionalFilter(
        @NotBlank String key,
        @NotNull Op op,
        @NotNull Object value // validated at runtime based on tag valueType
) {
    public enum Op { EQ, NEQ, GT, GTE, LT, LTE, IN, BETWEEN, EXISTS }
}

