package com.operationt.self.paksha.item.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "item_tags")
@IdClass(ItemTagEntity.PK.class)
public class ItemTagEntity {

    @Id
    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Id
    @Column(name = "tag_id", nullable = false)
    private UUID tagId;

    public static class PK implements Serializable {
        public UUID itemId;
        public UUID tagId;
        public PK() {}
        public PK(UUID itemId, UUID tagId) { this.itemId = itemId; this.tagId = tagId; }

        @Override public boolean equals(Object o) { return o instanceof PK pk && pk.itemId.equals(itemId) && pk.tagId.equals(tagId); }
        @Override public int hashCode() { return java.util.Objects.hash(itemId, tagId); }
    }

    // getters/setters
}
