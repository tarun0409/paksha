package com.operationt.self.paksha.item.service;

import com.operationt.self.paksha.item.dto.ItemCreateRequest;
import com.operationt.self.paksha.item.dto.ItemResponse;
import com.operationt.self.paksha.item.dto.ItemSearchRequest;
import com.operationt.self.paksha.item.entity.ItemEntity;
import com.operationt.self.paksha.item.entity.ItemTagEntity;
import com.operationt.self.paksha.item.repo.ItemRepository;
import com.operationt.self.paksha.item.repo.ItemSearchRepository;
import com.operationt.self.paksha.item.repo.ItemTagRepository;
import com.operationt.self.paksha.tag.dto.TagResponse;
import com.operationt.self.paksha.tag.service.TagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class ItemService {

    private final ItemRepository itemRepo;
    private final ItemTagRepository linkRepo;
    private final TagService tagService;
    private final ItemSearchRepository itemSearchRepo;

    public ItemService(ItemRepository itemRepo, ItemTagRepository linkRepo, TagService tagService, ItemSearchRepository itemSearchRepo) {
        this.itemRepo = itemRepo;
        this.linkRepo = linkRepo;
        this.tagService = tagService;
        this.itemSearchRepo = itemSearchRepo;
    }

    private Map<UUID, List<TagResponse>> getTagsForItems(UUID owner, List<UUID> itemIds) {
        List<ItemTagEntity> linkEntities = linkRepo.findByItemIdIn(itemIds);
        Map<UUID, List<UUID>> itemToTagsIds = new HashMap<>();
        for(ItemTagEntity it : linkEntities) {
            List<UUID> tagIds = itemToTagsIds.computeIfAbsent(it.getItemId(), x -> new ArrayList<>());
            tagIds.add(it.getTagId());
        }
        Map<UUID, List<TagResponse>> itemToTags = new HashMap<>();
        itemToTagsIds.forEach((key, tagIds) -> itemToTags.put(key, tagService.getByIds(owner, tagIds)));
        return itemToTags;
    }
    private List<ItemResponse> getItemResponses(List<ItemEntity> items, UUID owner) {
        List<UUID> itemIds = items.stream().map(ItemEntity::getId).toList();
        Map<UUID, List<TagResponse>> itemToTags = getTagsForItems(owner, itemIds);

        return items.stream().map(e -> {
            List<TagResponse> tags = itemToTags.getOrDefault(e.getId(), Collections.emptyList());
            return new ItemResponse(e.getId(), e.getTitle(), e.getBody(), tags, e.getCreatedAt(), e.getUpdatedAt());
        }).toList();
    }

    public List<ItemResponse> getByItemIds(List<UUID> itemIds, UUID owner) {
        var items = itemRepo.findByIdIn(itemIds);
        return getItemResponses(items, owner);
    }



    public List<ItemResponse> get(UUID ownerUserId) {
        List<ItemEntity> entities = itemRepo.findAll();
        return getItemResponses(entities, ownerUserId);
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> search(ItemSearchRequest req, UUID ownerUserId) {

        List<UUID> ids = itemSearchRepo.searchItemsIds(ownerUserId, req);
        return getByItemIds(ids, ownerUserId);
    }


    @Transactional
    public ItemResponse create(UUID ownerUserId, ItemCreateRequest req) {
        var now = Instant.now();

        // 1) Validate all tagIds exist and belong to user
        List<UUID> tagIds = req.tagIds() == null ? List.of() : req.tagIds();
        var tags = tagService.getByIds(ownerUserId, tagIds);
        if (tags.size() != new HashSet<>(tagIds).size()) {
            throw new IllegalArgumentException("Some tagIds are invalid or do not belong to the user.");
        }

        // 2) Create item
        ItemEntity item = new ItemEntity();
        item.setId(UUID.randomUUID());
        item.setOwnerUserId(ownerUserId);
        item.setTitle(req.title().trim());
        item.setBody(req.body());
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        itemRepo.save(item);

        for (UUID tagId : new HashSet<>(tagIds)) { // dedupe
            ItemTagEntity link = new ItemTagEntity();
            link.setItemId(item.getId());
            link.setTagId(tagId);
            linkRepo.save(link);
        }

        return new ItemResponse(item.getId(), item.getTitle(), item.getBody(), tags, item.getCreatedAt(), item.getUpdatedAt());
    }
}

