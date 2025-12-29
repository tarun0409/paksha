package com.operationt.self.paksha.query.dto;

import java.util.UUID;

public record SearchResultRow(
        UUID id,
        String title,
        String body
) {}

