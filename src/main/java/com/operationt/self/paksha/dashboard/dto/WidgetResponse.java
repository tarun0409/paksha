package com.operationt.self.paksha.dashboard.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.operationt.self.paksha.dashboard.entity.WidgetType;
import com.operationt.self.paksha.query.dto.WidgetQuerySpec;

import java.util.UUID;

public record WidgetResponse(
        UUID id,
        UUID dashboardId,
        String title,
        WidgetType type,
        WidgetQuerySpec querySpec,
        JsonNode layout
) {}

