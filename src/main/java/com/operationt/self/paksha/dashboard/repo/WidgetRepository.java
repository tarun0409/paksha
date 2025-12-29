package com.operationt.self.paksha.dashboard.repo;

import com.operationt.self.paksha.dashboard.entity.WidgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WidgetRepository extends JpaRepository<WidgetEntity, UUID> {
    List<WidgetEntity> findByDashboardId(UUID dashboardId);
}

