package com.operationt.self.paksha.dashboard.repo;

import com.operationt.self.paksha.dashboard.entity.DashboardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DashboardRepository extends JpaRepository<DashboardEntity, UUID> {
    List<DashboardEntity> findByOwnerUserId(UUID ownerUserId);
}
