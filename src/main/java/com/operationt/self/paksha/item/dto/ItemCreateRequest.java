package com.operationt.self.paksha.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ItemCreateRequest(
        @NotBlank String title,
        String body,
        @NotNull List<UUID> tagIds
) {}

