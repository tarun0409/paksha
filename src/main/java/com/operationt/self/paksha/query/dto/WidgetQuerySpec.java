package com.operationt.self.paksha.query.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record WidgetQuerySpec(
        @NotNull @Valid FilterSpec filter,
        @NotNull @Valid MetricSpec metric,
        @NotNull @Valid GroupBySpec groupBy,
        List<SortSpec> sort,
        Integer limit
) {}

