package com.operationt.self.paksha.dashboard.service;

import com.operationt.self.paksha.dashboard.entity.WidgetEntity;
import com.operationt.self.paksha.query.service.ItemQueryService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class WidgetEngine {

    private final ItemQueryService itemQueryService;

    public WidgetEngine(ItemQueryService itemQueryService) {
        this.itemQueryService = itemQueryService;
    }

    public Map<String, Object> render(UUID ownerUserId, WidgetEntity widget) {
        return switch (widget.getType()) {
            case KPI -> {
                long v = itemQueryService.computeKpiCount(ownerUserId, widget.getQuerySpec());
                yield Map.of("value", v);
            }
            case PIE, BAR -> {
                var series = itemQueryService.computeGroupByFunctionalKey(ownerUserId, widget.getQuerySpec())
                        .stream()
                        .map(e -> Map.of("label", e.getKey(), "value", e.getValue()))
                        .toList();
                yield Map.of("series", series);
            }
            case LIST -> Map.of("items", java.util.List.of()); // later: list query
            case TEXT -> Map.of("text", ""); // store text in querySpec or separate column
        };
    }
}

