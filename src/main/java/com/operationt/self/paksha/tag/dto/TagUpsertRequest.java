package com.operationt.self.paksha.tag.dto;

import com.operationt.self.paksha.tag.entity.TagKind;
import com.operationt.self.paksha.tag.entity.TagValueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TagUpsertRequest(
        @NotNull TagKind kind,
        @NotBlank String key,
        TagValueType valueType, // required for FUNCTIONAL
        Object value         // required for FUNCTIONAL
) {
}

