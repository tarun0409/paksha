package com.operationt.self.paksha.query.dto;

import jakarta.validation.constraints.NotNull;

public record GroupBySpec(
        @NotNull Type type,
        String key // for FUNCTIONAL_KEY
) {
    public enum Type { NONE, FUNCTIONAL_KEY }
}

