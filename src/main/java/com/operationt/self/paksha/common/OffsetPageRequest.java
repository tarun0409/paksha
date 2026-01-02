package com.operationt.self.paksha.common;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class OffsetPageRequest implements Pageable {
    private final int limit;
    private final int offset;
    private final Sort sort;

    public OffsetPageRequest(int limit, int offset, Sort sort) {
        if (limit <= 0) throw new IllegalArgumentException("limit must be positive");
        if (offset < 0) throw new IllegalArgumentException("offset must be non-negative");
        this.limit = limit;
        this.offset = offset;
        this.sort = sort == null ? Sort.unsorted() : sort;
    }

    @Override
    public int getPageNumber() {
        return offset / limit;
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetPageRequest(limit, offset + limit, sort);
    }

    @Override
    public Pageable previousOrFirst() {
        int newOffset = offset - limit;
        return new OffsetPageRequest(limit, Math.max(newOffset, 0), sort);
    }

    @Override
    public Pageable first() {
        return new OffsetPageRequest(limit, 0, sort);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        if (pageNumber < 0) return first();
        return new OffsetPageRequest(limit, pageNumber * limit, sort);
    }

    @Override
    public boolean hasPrevious() {
        return offset > 0;
    }
}
