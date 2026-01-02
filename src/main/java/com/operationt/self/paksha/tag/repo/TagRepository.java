package com.operationt.self.paksha.tag.repo;
import com.operationt.self.paksha.tag.entity.TagEntity;
import com.operationt.self.paksha.tag.entity.TagKind;
import com.operationt.self.paksha.tag.entity.TagValueType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<TagEntity, UUID> {

    Optional<TagEntity> findByOwnerUserIdAndKindAndKey(UUID ownerUserId, TagKind kind, String key);

    Optional<TagEntity> findByOwnerUserIdAndKindAndKeyAndValueTypeAndValueCanonical(
            UUID ownerUserId,
            TagKind kind,
            String key,
            TagValueType valueType,
            String valueCanonical
    );
    List<TagEntity> findAllByIdInAndOwnerUserId(List<UUID> ids, UUID ownerUserId);
    Page<TagEntity> findByOwnerUserId(UUID ownerUserId, Pageable pageable);

}

