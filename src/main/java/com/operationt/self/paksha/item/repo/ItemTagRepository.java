package com.operationt.self.paksha.item.repo;


import com.operationt.self.paksha.item.entity.ItemTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ItemTagRepository extends JpaRepository<ItemTagEntity, ItemTagEntity.PK> {
    List<ItemTagEntity> findByItemId(UUID itemId);
    List<ItemTagEntity> findByTagId(UUID tagId);
    List<ItemTagEntity> findByItemIdIn(Collection<UUID> itemIds);

}
