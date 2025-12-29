package com.operationt.self.paksha.dashboard.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.operationt.self.paksha.query.dto.WidgetQuerySpec;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "widgets")
public class WidgetEntity {
    @Id
    private UUID id;

    @Column(name = "dashboard_id", nullable = false)
    private UUID dashboardId;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private WidgetType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "query_spec", nullable = false, columnDefinition = "jsonb")
    private WidgetQuerySpec querySpec;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "layout", columnDefinition = "jsonb")
    private JsonNode layout; // keep layout flexible

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // getters/setters
}

