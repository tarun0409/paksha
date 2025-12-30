package com.operationt.self.paksha.dashboard.controller;

import com.operationt.self.paksha.dashboard.dto.DashboardCreateRequest;
import com.operationt.self.paksha.dashboard.dto.DashboardResponse;
import com.operationt.self.paksha.dashboard.dto.RenderDashboardResponse;
import com.operationt.self.paksha.dashboard.service.DashboardService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboards")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // TODO replace with auth principal
    private UUID owner() { return UUID.fromString("00000000-0000-0000-0000-000000000001"); }

    @PostMapping
    public DashboardResponse create(@Valid @RequestBody DashboardCreateRequest req) {
        return dashboardService.create(owner(), req);
    }

    @GetMapping
    public List<DashboardResponse> list(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset
    ) {
        return dashboardService.list(owner(), limit, offset);
    }

    @PostMapping("/{dashboardId}/render")
    public RenderDashboardResponse render(@PathVariable UUID dashboardId) {
        return dashboardService.render(owner(), dashboardId);
    }
}

