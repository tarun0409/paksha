package com.operationt.self.paksha.dashboard.repo;

import com.operationt.self.paksha.dashboard.entity.WidgetEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WidgetRepository extends JpaRepository<WidgetEntity, UUID> {
    List<WidgetEntity> findByDashboardId(UUID dashboardId);
    Page<WidgetEntity> findByDashboardId(UUID dashboardId, Pageable pageable);
}

