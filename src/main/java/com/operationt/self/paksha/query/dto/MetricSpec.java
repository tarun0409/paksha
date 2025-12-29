package com.operationt.self.paksha.query.dto;

import jakarta.validation.constraints.NotNull;

public record MetricSpec(
        @NotNull Fn fn,
        String key // needed for SUM/AVG etc (e.g., points)
) {
    public enum Fn { COUNT, SUM, AVG }
}
