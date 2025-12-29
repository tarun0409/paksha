package com.operationt.self.paksha.query.dto;

import jakarta.validation.Valid;
import java.util.List;

public record FilterSpec(
        String text,
        List<String> mustHaveSingle,
        List<String> mustNotHaveSingle,
        @Valid List<FunctionalFilter> mustHaveFunctional,
        @Valid List<FunctionalFilter> mustNotHaveFunctional
) {}
