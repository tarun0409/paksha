package com.operationt.self.paksha.dashboard.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.operationt.self.paksha.dashboard.entity.WidgetType;
import com.operationt.self.paksha.query.dto.WidgetQuerySpec;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WidgetUpsertRequest(
        @NotBlank String title,
        @NotNull WidgetType type,
        @NotNull @Valid WidgetQuerySpec querySpec,
        JsonNode layout
) {}

