package com.operationt.self.paksha.dashboard.service;

import com.operationt.self.paksha.dashboard.dto.DashboardCreateRequest;
import com.operationt.self.paksha.dashboard.dto.DashboardResponse;
import com.operationt.self.paksha.dashboard.dto.RenderDashboardResponse;
import com.operationt.self.paksha.dashboard.entity.DashboardEntity;
import com.operationt.self.paksha.dashboard.repo.DashboardRepository;
import com.operationt.self.paksha.dashboard.repo.WidgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class DashboardService {

    private final DashboardRepository dashboardRepo;
    private final WidgetRepository widgetRepo;
    private final WidgetEngine widgetEngine;

    public DashboardService(DashboardRepository dashboardRepo, WidgetRepository widgetRepo, WidgetEngine widgetEngine) {
        this.dashboardRepo = dashboardRepo;
        this.widgetRepo = widgetRepo;
        this.widgetEngine = widgetEngine;
    }

    @Transactional
    public DashboardResponse create(UUID ownerUserId, DashboardCreateRequest req) {
        var now = Instant.now();
        DashboardEntity d = new DashboardEntity();
        d.setId(UUID.randomUUID());
        d.setOwnerUserId(ownerUserId);
        d.setName(req.name());
        d.setDescription(req.description());
        d.setCreatedAt(now);
        d.setUpdatedAt(now);
        dashboardRepo.save(d);
        return new DashboardResponse(d.getId(), d.getName(), d.getDescription());
    }

    @Transactional(readOnly = true)
    public List<DashboardResponse> list(UUID ownerUserId) {
        return dashboardRepo.findByOwnerUserId(ownerUserId)
                .stream()
                .map(d -> new DashboardResponse(d.getId(), d.getName(), d.getDescription()))
                .toList();
    }

    @Transactional(readOnly = true)
    public RenderDashboardResponse render(UUID ownerUserId, UUID dashboardId) {
        var widgets = widgetRepo.findByDashboardId(dashboardId);
        var rendered = widgets.stream()
                .map(w -> new RenderDashboardResponse.WidgetRender(
                        w.getId(), w.getTitle(), w.getType().name(), widgetEngine.render(ownerUserId, w)
                ))
                .toList();

        return new RenderDashboardResponse(dashboardId, Instant.now(), rendered);
    }
}

