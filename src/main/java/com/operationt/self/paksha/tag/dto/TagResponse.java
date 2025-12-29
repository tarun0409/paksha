package com.operationt.self.paksha.tag.dto;


import com.operationt.self.paksha.tag.entity.TagKind;
import com.operationt.self.paksha.tag.entity.TagValueType;

import java.time.Instant;
import java.util.UUID;

public record TagResponse(
        UUID id,
        UUID ownerUserId,
        TagKind kind,
        String key,
        TagValueType valueType,
        Object value,
        String valueCanonical,
        Instant createdAt
) {}
