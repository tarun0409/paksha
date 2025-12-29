package com.operationt.self.paksha.dashboard.dto;


import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RenderDashboardResponse(
        UUID dashboardId,
        Instant generatedAt,
        List<WidgetRender> widgets
) {
    public record WidgetRender(UUID widgetId, String title, String type, Map<String, Object> result) {}
}
