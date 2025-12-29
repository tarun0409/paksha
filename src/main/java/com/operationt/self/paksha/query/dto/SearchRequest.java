package com.operationt.self.paksha.query.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SearchRequest(
        @NotNull @Valid FilterSpec filter,
        List<SortSpec> sort,
        Integer page,
        Integer size
) {}

