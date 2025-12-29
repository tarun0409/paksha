package com.operationt.self.paksha.query.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SortSpec(
        @NotBlank String field, // title, updatedAt etc
        @NotNull Dir dir
) {
    public enum Dir { ASC, DESC }
}

