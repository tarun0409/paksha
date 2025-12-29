package com.operationt.self.paksha.item.dto;

import com.operationt.self.paksha.tag.dto.TagResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ItemResponse(
        UUID id,
        String title,
        String body,
        List<TagResponse> tagIds,
        Instant createdAt,
        Instant updatedAt
) {}
