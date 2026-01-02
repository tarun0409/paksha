package com.operationt.self.paksha.common;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtils {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;

    private PaginationUtils() {
    }

    public static int sanitizeLimit(Integer limit) {
        if (limit == null || limit <= 0) return DEFAULT_LIMIT;
        return Math.min(limit, MAX_LIMIT);
    }

    public static int sanitizeOffset(Integer offset) {
        if (offset == null || offset < 0) return 0;
        return offset;
    }

    public static Pageable buildPageable(Integer limit, Integer offset, Sort sort) {
        int size = sanitizeLimit(limit);
        int off = sanitizeOffset(offset);
        Sort effectiveSort = sort == null ? Sort.unsorted() : sort;
        return new OffsetPageRequest(size, off, effectiveSort);
    }
}
