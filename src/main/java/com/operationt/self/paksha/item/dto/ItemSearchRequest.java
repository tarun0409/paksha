package com.operationt.self.paksha.item.dto;

import jakarta.validation.Valid;

import java.util.List;

public record ItemSearchRequest(
        List<String> singleTags,
        @Valid List<FunctionalFilter> functional,
        Integer limit,
        Integer offset
) {
    public record FunctionalFilter(
            String key,
            Op op,
            ValueType valueType,
            Object value
    ) {}

    public enum Op { EQ, NEQ, GT, GTE, LT, LTE }
    public enum ValueType { STRING, NUMBER, BOOL, DATE }
}

