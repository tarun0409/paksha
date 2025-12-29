package com.operationt.self.paksha.query.service;

import com.operationt.self.paksha.query.dto.SearchRequest;
import com.operationt.self.paksha.query.dto.SearchResultRow;
import com.operationt.self.paksha.query.dto.WidgetQuerySpec;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ItemQueryService {

    private final JPAQueryFactory qf;

    public ItemQueryService(EntityManager em) {
        this.qf = new JPAQueryFactory(em);
    }

    @Transactional(readOnly = true)
    public long computeKpiCount(UUID ownerUserId, WidgetQuerySpec spec) {
        // TODO: validate spec in a separate validator (keys exist, types match)
        // TODO: build query that selects distinct item ids matching all filters, then count
        return 0L;
    }

    @Transactional(readOnly = true)
    public List<Map.Entry<String, Number>> computeGroupByFunctionalKey(UUID ownerUserId, WidgetQuerySpec spec) {
        // groupBy FUNCTIONAL_KEY: group by tag value for that key
        return List.of();
    }

    @Transactional(readOnly = true)
    public List<SearchResultRow> searchItems(UUID ownerUserId, SearchRequest req) {
        // TODO: build list query + pagination
        return List.of();
    }
}

