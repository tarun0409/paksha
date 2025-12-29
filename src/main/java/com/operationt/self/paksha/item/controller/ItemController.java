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
    public List<ItemResponse> get() {
        return itemService.get(owner());
    }

    @GetMapping("/search")
    public List<ItemResponse> search(@Valid @RequestBody ItemSearchRequest req) {
        return itemService.search(req, owner());
    }
}

