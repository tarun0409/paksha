package com.operationt.self.paksha.item.controller;

import com.operationt.self.paksha.item.dto.ItemCreateRequest;
import com.operationt.self.paksha.item.dto.ItemResponse;
import com.operationt.self.paksha.item.dto.ItemSearchRequest;
import com.operationt.self.paksha.item.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    private UUID owner() {
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    @PostMapping
    public ItemResponse create(@Valid @RequestBody ItemCreateRequest req) {
        return itemService.create(owner(), req);
    }

    @GetMapping
    public List<ItemResponse> get(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset
    ) {
        return itemService.get(owner(), limit, offset);
    }

    @GetMapping("/search")
    public List<ItemResponse> search(@Valid @RequestBody ItemSearchRequest req) {
        return itemService.search(req, owner());
    }

    @PutMapping("/{itemId}/tag/{tagId}")
    public ItemResponse associateTag(@PathVariable UUID itemId, @PathVariable UUID tagId) {
        return itemService.associateTag(owner(), itemId, tagId);
    }

    @DeleteMapping("/{itemId}/tag/{tagId}")
    public ItemResponse unassociateTag(@PathVariable UUID itemId, @PathVariable UUID tagId) {
        return itemService.unassociateTag(owner(), itemId, tagId);
    }
}

