package com.operationt.self.paksha.item.repo;

import com.operationt.self.paksha.item.entity.ItemEntity;
import com.operationt.self.paksha.item.entity.ItemTagEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<ItemEntity, UUID> {
    List<ItemEntity> findByIdIn(Collection<UUID> itemIds);
    Page<ItemEntity> findByOwnerUserId(UUID ownerUserId, Pageable pageable);
}
