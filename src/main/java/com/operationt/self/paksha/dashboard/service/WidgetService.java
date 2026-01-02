package com.operationt.self.paksha.dashboard.service;

import com.operationt.self.paksha.dashboard.dto.WidgetResponse;
import com.operationt.self.paksha.dashboard.dto.WidgetUpsertRequest;
import com.operationt.self.paksha.dashboard.entity.WidgetEntity;
import com.operationt.self.paksha.dashboard.repo.WidgetRepository;
import com.operationt.self.paksha.common.PaginationUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class WidgetService {

    private final WidgetRepository widgetRepo;
    private final WidgetEngine widgetEngine;

    public WidgetService(WidgetRepository widgetRepo, WidgetEngine widgetEngine) {
        this.widgetRepo = widgetRepo;
        this.widgetEngine = widgetEngine;
    }

    @Transactional
    public WidgetResponse create(UUID ownerUserId, UUID dashboardId, WidgetUpsertRequest req) {
        // TODO: validate querySpec keys exist for this ownerUserId (TagDefinition lookup)
        var now = Instant.now();

        WidgetEntity w = new WidgetEntity();
        w.setId(UUID.randomUUID());
        w.setDashboardId(dashboardId);
        w.setTitle(req.title());
        w.setType(req.type());
        w.setQuerySpec(req.querySpec());
        w.setLayout(req.layout());
        w.setCreatedAt(now);
        w.setUpdatedAt(now);

        widgetRepo.save(w);
        return toDto(w);
    }

    @Transactional(readOnly = true)
    public List<WidgetResponse> list(UUID ownerUserId, UUID dashboardId, Integer limit, Integer offset) {
        Pageable pageable = PaginationUtils.buildPageable(limit, offset, Sort.by("updatedAt").descending());
        return widgetRepo.findByDashboardId(dashboardId, pageable).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> render(UUID ownerUserId, UUID widgetId) {
        var w = widgetRepo.findById(widgetId).orElseThrow();
        return widgetEngine.render(ownerUserId, w);
    }

    private WidgetResponse toDto(WidgetEntity w) {
        return new WidgetResponse(w.getId(), w.getDashboardId(), w.getTitle(), w.getType(), w.getQuerySpec(), w.getLayout());
    }
}

