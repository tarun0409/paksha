package com.operationt.self.paksha.tag.controller;

import com.operationt.self.paksha.tag.dto.TagResponse;
import com.operationt.self.paksha.tag.dto.TagUpsertRequest;
import com.operationt.self.paksha.tag.entity.TagKind;
import com.operationt.self.paksha.tag.service.TagService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    // TODO replace with auth principal later
    private UUID owner() {
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    /**
     * Create-or-return tag (idempotent).
     * SINGLE: {kind:SINGLE, key}
     * FUNCTIONAL: {kind:FUNCTIONAL, key, valueType, value}
     */
    @PostMapping
    public TagResponse upsert(@Valid @RequestBody TagUpsertRequest req) {
        return tagService.upsert(owner(), req);
    }

    /**
     * List tags for owner. Optional filters to make autocomplete easy.
     * Example:
     *   GET /api/tags?kind=FUNCTIONAL&key=points
     *   GET /api/tags?search=fin
     */
    @GetMapping
    public List<TagResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TagKind kind,
            @RequestParam(required = false) UUID key
    ) {
        return tagService.get(owner());
    }

    /** Get tag by id */
    @GetMapping("/{id}")
    public TagResponse get(@PathVariable UUID id) {
        return tagService.get(owner(), id);
    }
}


