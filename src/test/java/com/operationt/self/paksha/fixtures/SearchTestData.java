package com.operationt.self.paksha.fixtures;

import com.operationt.self.paksha.item.entity.ItemEntity;
import com.operationt.self.paksha.item.entity.ItemTagEntity;
import com.operationt.self.paksha.item.repo.ItemRepository;
import com.operationt.self.paksha.item.repo.ItemTagRepository;
import com.operationt.self.paksha.tag.entity.TagEntity;
import com.operationt.self.paksha.tag.entity.TagKind;
import com.operationt.self.paksha.tag.entity.TagValueType;
import com.operationt.self.paksha.tag.repo.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

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

    public record Created(
            UUID owner,
            List<UUID> itemIds,
            List<UUID> tagIds
    ) {}

    private Created created;

    public UUID owner() {
        return owner;
    }
    private final List<UUID> tagsToUnassociate = new ArrayList<>();

    public List<UUID> getTagsToUnassociate() {
        return tagsToUnassociate;
    }

    public List<UUID> getItems() {
        return created.itemIds();
    }

    public void cleanup() {
        created.itemIds().forEach(id -> linkRepo.findByItemId(id).forEach(linkRepo::delete));

        created.itemIds().forEach(itemRepo::deleteById);
        created.tagIds().forEach(tagRepo::deleteById);
        tagsToUnassociate.clear();
    }

    public void seedBasicScenario() {
        // Tags
        TagEntity personal = singleTag(owner, tagRepo, "personal_test");
        TagEntity finished = singleTag(owner, tagRepo, "finished_test");
        TagEntity learning = singleTag(owner, tagRepo, "learning_test");

        TagEntity unassociateOne = singleTag(owner, tagRepo, "tagToUnassociate_1");
        TagEntity unassociateTwo = singleTag(owner, tagRepo, "tagToUnassociate_2");

        TagEntity points5 = functionalNumber(owner, tagRepo, "points_test", new BigDecimal("5"));
        TagEntity points3 = functionalNumber(owner, tagRepo, "points_test", new BigDecimal("3"));
        TagEntity monthDec = functionalString(owner, tagRepo, "month_test", "december");
        TagEntity archivedTrue = functionalBool(owner, tagRepo, "archived_test", true);
        TagEntity archivedFalse = functionalBool(owner, tagRepo, "archived_test", false);
        TagEntity urgency = functionalString(owner, tagRepo, "urgency_test", "casual");
        TagEntity day2025_12_01 = functionalDate(owner, tagRepo, "created_day_test", LocalDate.parse("2025-12-01"));
        TagEntity day2025_11_01 = functionalDate(owner, tagRepo, "created_day_test", LocalDate.parse("2025-11-01"));

        // Items
        ItemEntity a = item(owner, itemRepo, "testA", 10);
        ItemEntity b = item(owner, itemRepo, "testB", 20);
        ItemEntity c = item(owner, itemRepo, "testC", 30);
        ItemEntity thelPathriSingh = item(owner, itemRepo, "thelPathriSingh", 40);

        // Links
        link(linkRepo, a, personal);
        link(linkRepo, a, finished);
        link(linkRepo, a, points5);
        link(linkRepo, a, monthDec);
        link(linkRepo, a, archivedTrue);
        link(linkRepo, a, day2025_12_01);
        link(linkRepo, a, urgency);
        link(linkRepo, a, points3);

        link(linkRepo, b, personal);
        // b missing finished
        link(linkRepo, b, points5);
        link(linkRepo, b, archivedFalse);
        link(linkRepo, b, day2025_11_01);

        link(linkRepo, c, personal);
        link(linkRepo, c, finished);
        link(linkRepo, c, learning);

        link(linkRepo, thelPathriSingh, unassociateOne);
        link(linkRepo, thelPathriSingh, unassociateTwo);
        // c missing monthDec

        this.created = new Created(
                owner,
                List.of(a.getId(), b.getId(), c.getId(), thelPathriSingh.getId()),
                List.of(personal.getId(),
                        finished.getId(),
                        learning.getId(),
                        points5.getId(),
                        points3.getId(),
                        monthDec.getId(),
                        archivedTrue.getId(),
                        archivedFalse.getId(),
                        day2025_12_01.getId(),
                        day2025_11_01.getId(),
                        urgency.getId(),
                        unassociateOne.getId(),
                        unassociateTwo.getId())
        );
        tagsToUnassociate.add(unassociateOne.getId());
        tagsToUnassociate.add(unassociateTwo.getId());
    }

    // ---------- creators ----------

    private static ItemEntity item(UUID owner, ItemRepository repo, String title, int updatedAtSeconds) {
        ItemEntity it = new ItemEntity();
        it.setId(UUID.randomUUID());
        it.setOwnerUserId(owner);
        it.setTitle(title);
        it.setBody(null);
        Instant base = Instant.parse("2025-12-01T00:00:00Z");
        it.setCreatedAt(base);
        it.setUpdatedAt(base.plusSeconds(updatedAtSeconds));
        return repo.save(it);
    }

    private static TagEntity singleTag(UUID owner, TagRepository repo, String key) {
        TagEntity t = new TagEntity();
        t.setId(UUID.randomUUID());
        t.setOwnerUserId(owner);
        t.setKind(TagKind.SINGLE);
        t.setKey(key.toLowerCase(Locale.ROOT));
        t.setValueType(null);
        t.setValueCanonical(null);
        t.setCreatedAt(Instant.now());

        t.setValueString(null);
        t.setValueNumber(null);
        t.setValueBool(null);
        t.setValueDate(null);
        t.setValueJson(null);

        return repo.save(t);
    }

    private static TagEntity functionalString(UUID owner, TagRepository repo, String key, String value) {
        TagEntity t = new TagEntity();
        t.setId(UUID.randomUUID());
        t.setOwnerUserId(owner);
        t.setKind(TagKind.FUNCTIONAL);
        t.setKey(key.toLowerCase(Locale.ROOT));
        t.setValueType(TagValueType.STRING);

        String canonical = value.trim().toLowerCase(Locale.ROOT);
        t.setValueCanonical(canonical);
        t.setValueString(value.trim());

        t.setValueNumber(null);
        t.setValueBool(null);
        t.setValueDate(null);
        t.setValueJson(null);

        t.setCreatedAt(Instant.now());
        return repo.save(t);
    }

    private static TagEntity functionalNumber(UUID owner, TagRepository repo, String key, BigDecimal value) {
        TagEntity t = new TagEntity();
        t.setId(UUID.randomUUID());
        t.setOwnerUserId(owner);
        t.setKind(TagKind.FUNCTIONAL);
        t.setKey(key.toLowerCase(Locale.ROOT));
        t.setValueType(TagValueType.NUMBER);

        t.setValueNumber(value);
        t.setValueCanonical(value.stripTrailingZeros().toPlainString());

        t.setValueString(null);
        t.setValueBool(null);
        t.setValueDate(null);
        t.setValueJson(null);

        t.setCreatedAt(Instant.now());
        return repo.save(t);
    }

    private static TagEntity functionalBool(UUID owner, TagRepository repo, String key, boolean value) {
        TagEntity t = new TagEntity();
        t.setId(UUID.randomUUID());
        t.setOwnerUserId(owner);
        t.setKind(TagKind.FUNCTIONAL);
        t.setKey(key.toLowerCase(Locale.ROOT));
        t.setValueType(TagValueType.BOOL);

        t.setValueBool(value);
        t.setValueCanonical(Boolean.toString(value));

        t.setValueString(null);
        t.setValueNumber(null);
        t.setValueDate(null);
        t.setValueJson(null);

        t.setCreatedAt(Instant.now());
        return repo.save(t);
    }

    private static TagEntity functionalDate(UUID owner, TagRepository repo, String key, LocalDate value) {
        TagEntity t = new TagEntity();
        t.setId(UUID.randomUUID());
        t.setOwnerUserId(owner);
        t.setKind(TagKind.FUNCTIONAL);
        t.setKey(key.toLowerCase(Locale.ROOT));
        t.setValueType(TagValueType.DATE);

        t.setValueDate(value);
        t.setValueCanonical(value.toString());

        t.setValueString(null);
        t.setValueNumber(null);
        t.setValueBool(null);
        t.setValueJson(null);

        t.setCreatedAt(Instant.now());
        return repo.save(t);
    }

    private static void link(ItemTagRepository repo, ItemEntity item, TagEntity tag) {
        ItemTagEntity l = new ItemTagEntity();
        l.setItemId(item.getId());
        l.setTagId(tag.getId());
        repo.save(l);
    }
}

