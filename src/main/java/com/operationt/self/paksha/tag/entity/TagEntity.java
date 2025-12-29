package com.operationt.self.paksha.tag.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "tags")
public class TagEntity {

    @Id
    private UUID id;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TagKind kind;

    @Column(name = "\"key\"", nullable = false, length = 100)
    private String key;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type")
    private TagValueType valueType;

    @Column(name = "value_string")
    private String valueString;

    @Column(name = "value_number")
    private BigDecimal valueNumber;

    @Column(name = "value_bool")
    private Boolean valueBool;

    @Column(name = "value_date")
    private LocalDate valueDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "value_json", columnDefinition = "jsonb")
    private JsonNode valueJson;

    @Column(name = "value_canonical")
    private String valueCanonical;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}
