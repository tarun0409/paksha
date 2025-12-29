package com.operationt.self.paksha.dashboard.controller;

import com.operationt.self.paksha.dashboard.dto.WidgetResponse;
import com.operationt.self.paksha.dashboard.dto.WidgetUpsertRequest;
import com.operationt.self.paksha.dashboard.service.WidgetService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class WidgetController {

    private final WidgetService widgetService;

    public WidgetController(WidgetService widgetService) {
        this.widgetService = widgetService;
    }

    private UUID owner() { return UUID.fromString("00000000-0000-0000-0000-000000000001"); }

    @PostMapping("/api/dashboards/{dashboardId}/widgets")
    public WidgetResponse create(@PathVariable UUID dashboardId, @Valid @RequestBody WidgetUpsertRequest req) {
        return widgetService.create(owner(), dashboardId, req);
    }

    @GetMapping("/api/dashboards/{dashboardId}/widgets")
    public List<WidgetResponse> list(@PathVariable UUID dashboardId) {
        return widgetService.list(owner(), dashboardId);
    }

    @PostMapping("/api/widgets/{widgetId}/render")
    public Map<String, Object> renderOne(@PathVariable UUID widgetId) {
        return widgetService.render(owner(), widgetId);
    }
}

