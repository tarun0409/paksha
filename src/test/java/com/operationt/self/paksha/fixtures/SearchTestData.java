package com.operationt.self.paksha.fixtures;

import com.fasterxml.jackson.databind.JsonNode;
import com.operationt.self.paksha.item.entity.ItemEntity;
import com.operationt.self.paksha.item.entity.ItemTagEntity;
import com.operationt.self.paksha.item.repo.ItemRepository;
import com.operationt.self.paksha.item.repo.ItemTagRepository;
import com.operationt.self.paksha.tag.entity.TagEntity;
import com.operationt.self.paksha.tag.entity.TagKind;
import com.operationt.self.paksha.tag.entity.TagValueType;
import com.operationt.self.paksha.tag.repo.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
public final class SearchTestData {
    @Autowired
    ItemRepository itemRepo;
    @Autowired
    TagRepository tagRepo;
    @Autowired
    ItemTagRepository linkRepo;

    private final UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private SearchTestData() {}

    class Created {
        UUID owner;
        Set<UUID> itemIds;
        Set<UUID> tagIds;
        public Created() {
            this.owner = owner();
            this.itemIds = new HashSet<>();
            this.tagIds = new HashSet<>();
        }
    }
    public record FunctionalTag(
       TagValueType type,
       String key,
       Object object
    ) {}

    private Created created;

    public UUID owner() {
        return owner;
    }
    public void cleanup() {
        if(created == null) {
            return;
        }
        created.itemIds.forEach(id -> linkRepo.deleteAll(linkRepo.findByItemId(id)));

        created.itemIds.forEach(itemRepo::deleteById);
        created.tagIds.forEach(tagRepo::deleteById);
        created = null;
    }
    public List<UUID> createItems(List<String> itemKeys) {
        if(created == null) {
            created = new Created();
        }
        List<UUID> itemIds = itemKeys.stream().map(this::item).map(ItemEntity::getId).toList();
        created.itemIds.addAll(itemIds);
        return itemIds;
    }
    public Map<UUID, List<UUID>> associateFunctionalTags(Map<UUID, List<FunctionalTag>> keyMap) {
        if(created == null) {
            throw new IllegalArgumentException("Items are not created");
        }
        Map<UUID, List<UUID>> itemToTagMap = new HashMap<>();
        for (Map.Entry<UUID, List<FunctionalTag>> itemToTags : keyMap.entrySet()) {

            List<UUID> tags = itemToTags.getValue().stream()
                    .map(this::functionalTag)
                    .map(TagEntity::getId)
                    .toList();
            tags.forEach(t -> link(itemToTags.getKey(), t));
            itemToTagMap.put(itemToTags.getKey(), tags);
            created.tagIds.addAll(tags);
        }
        return itemToTagMap;
    }



    public Map<UUID, List<UUID>> associateSingleTags(Map<UUID, List<String>> keyMap) {
        if(created == null) {
            throw new IllegalArgumentException("Items are not created");
        }
        Map<UUID, List<UUID>> itemToTagMap = new HashMap<>();
        for (Map.Entry<UUID, List<String>> itemToTags : keyMap.entrySet()) {
            List<UUID> tags = itemToTags.getValue().stream()
                    .map(this::singleTag)
                    .map(TagEntity::getId)
                    .toList();
            tags.forEach(t -> link(itemToTags.getKey(), t));
            itemToTagMap.put(itemToTags.getKey(), tags);
            created.tagIds.addAll(tags);
        }
        return itemToTagMap;
    }

    public List<UUID> createSingleTags(List<String> keys) {
        if(created == null) {
            created = new Created();
        }
        List<UUID> tagIds = keys.stream().map(this::singleTag).map(TagEntity::getId).toList();
        created.tagIds.addAll(tagIds);
        return tagIds;
    }
    public List<UUID> createFunctionalTags(List<FunctionalTag> tagInfos) {
        if(created == null) {
            created = new Created();
        }
        List<UUID> tagIds = tagInfos.stream().map(this::functionalTag).map(TagEntity::getId).toList();
        created.tagIds.addAll(tagIds);
        return tagIds;
    }

    public ItemEntity item(String title) {
        ItemEntity it = new ItemEntity();
        it.setId(UUID.randomUUID());
        it.setOwnerUserId(owner);
        it.setTitle(title);
        it.setBody(null);
        Instant base = Instant.parse("2025-12-01T00:00:00Z");
        it.setCreatedAt(base);
        int secs = ThreadLocalRandom.current().nextInt(10, 100);
        it.setUpdatedAt(base.plusSeconds(secs));
        return itemRepo.save(it);
    }

    public TagEntity singleTag(String key) {
        TagEntity t = new TagEntity();
        t.setId(UUID.randomUUID());
        t.setOwnerUserId(owner);
        t.setKind(TagKind.SINGLE);
        t.setKey(key.toLowerCase(Locale.ROOT));
        t.setCreatedAt(Instant.now());

        try {
            return tagRepo.save(t);
        } catch (DataIntegrityViolationException e) {
            return tagRepo.findByOwnerUserIdAndKindAndKey(owner, TagKind.SINGLE, key).orElseThrow();
        }
    }

    private TagEntity functionalTag(FunctionalTag tagInfo) {
        TagEntity t = new TagEntity();
        t.setId(UUID.randomUUID());
        t.setOwnerUserId(owner);
        t.setKind(TagKind.FUNCTIONAL);
        t.setKey(tagInfo.key().toLowerCase(Locale.ROOT));
        t.setCreatedAt(Instant.now());
        t.setValueType(tagInfo.type());
        String canonical = null;
        switch (tagInfo.type()) {
            case STRING -> {
                canonical = ((String) tagInfo.object()).trim().toLowerCase(Locale.ROOT);
                t.setValueString(canonical);
            }
            case NUMBER -> {
                BigDecimal num = BigDecimal.valueOf((Integer)tagInfo.object()) ;
                canonical = num.stripTrailingZeros().toPlainString();
                t.setValueNumber(num);
            }
            case BOOL   -> {
                boolean value = (boolean) tagInfo.object();
                canonical = Boolean.toString(value);
                t.setValueBool(value);
            }
            case DATE   -> {
                LocalDate value = (java.time.LocalDate)tagInfo.object();
                t.setValueDate(value);
                canonical = value.toString();
            }
            case JSON   -> t.setValueJson((JsonNode) tagInfo.object()); // often String/JsonNode/Map
        }
        t.setValueCanonical(canonical);
        try {
//            System.out.println(t.getKey() + " " + t.getValueCanonical());
            return tagRepo.save(t);
        } catch (DataIntegrityViolationException e) {
            return tagRepo.findByOwnerUserIdAndKindAndKeyAndValueTypeAndValueCanonical(
                    owner, TagKind.FUNCTIONAL, t.getKey(), t.getValueType(), canonical
            ).orElseThrow();
        }
    }

    private void link(UUID itemId, UUID tagId) {
        ItemTagEntity l = new ItemTagEntity();
        l.setItemId(itemId);
        l.setTagId(tagId);
        linkRepo.save(l);
    }
}

