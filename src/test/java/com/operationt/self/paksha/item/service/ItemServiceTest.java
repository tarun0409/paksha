package com.operationt.self.paksha.item.service;

import com.operationt.self.paksha.item.dto.ItemResponse;
import com.operationt.self.paksha.item.dto.ItemSearchRequest;
import com.operationt.self.paksha.item.entity.ItemEntity;
import com.operationt.self.paksha.item.entity.ItemTagEntity;
import com.operationt.self.paksha.item.repo.ItemRepository;
import com.operationt.self.paksha.item.repo.ItemSearchRepository;
import com.operationt.self.paksha.item.repo.ItemTagRepository;
import com.operationt.self.paksha.tag.dto.TagResponse;
import com.operationt.self.paksha.tag.entity.TagKind;
import com.operationt.self.paksha.tag.entity.TagValueType;
import com.operationt.self.paksha.tag.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepo;

    @Mock
    private ItemTagRepository linkRepo;

    @Mock
    private TagService tagService;

    @Mock
    private ItemSearchRepository itemSearchRepo;

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemService(itemRepo, linkRepo, tagService, itemSearchRepo);
    }

    @Test
    void searchReturnsResponsesWithTagsForAllFoundItems() {
        UUID owner = UUID.randomUUID();
        UUID itemId1 = UUID.randomUUID();
        UUID itemId2 = UUID.randomUUID();
        UUID tagId1 = UUID.randomUUID();
        UUID tagId2 = UUID.randomUUID();
        UUID tagId3 = UUID.randomUUID();

        ItemSearchRequest req = new ItemSearchRequest(List.of("work"), null, 10, 0);

        when(itemSearchRepo.searchItemsIds(owner, req)).thenReturn(List.of(itemId1, itemId2));

        ItemEntity item1 = buildItem(itemId1, owner, "First", "Body 1", Instant.parse("2024-01-01T00:00:00Z"));
        ItemEntity item2 = buildItem(itemId2, owner, "Second", "Body 2", Instant.parse("2024-02-01T00:00:00Z"));
        when(itemRepo.findByIdIn(List.of(itemId1, itemId2))).thenReturn(List.of(item1, item2));

        ItemTagEntity link1 = link(itemId1, tagId1);
        ItemTagEntity link2 = link(itemId1, tagId2);
        ItemTagEntity link3 = link(itemId2, tagId3);
        when(linkRepo.findByItemIdIn(List.of(itemId1, itemId2))).thenReturn(List.of(link1, link2, link3));

        Map<UUID, TagResponse> tagLookup = new HashMap<>();
        tagLookup.put(tagId1, new TagResponse(tagId1, owner, TagKind.SINGLE, "work", TagValueType.STRING, null, null, Instant.now()));
        tagLookup.put(tagId2, new TagResponse(tagId2, owner, TagKind.SINGLE, "priority", TagValueType.STRING, null, null, Instant.now()));
        tagLookup.put(tagId3, new TagResponse(tagId3, owner, TagKind.FUNCTIONAL, "status", TagValueType.STRING, "open", "open", Instant.now()));

        when(tagService.getByIds(eq(owner), anyList())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<UUID> ids = invocation.getArgument(1);
            return ids.stream().map(tagLookup::get).toList();
        });

        List<ItemResponse> responses = itemService.search(req, owner);

        assertThat(responses).hasSize(2);

        ItemResponse first = responses.getFirst();
        assertThat(first.id()).isEqualTo(itemId1);
        assertThat(first.title()).isEqualTo("First");
        assertThat(first.body()).isEqualTo("Body 1");
        assertThat(first.tags()).containsExactly(tagLookup.get(tagId1), tagLookup.get(tagId2));

        ItemResponse second = responses.get(1);
        assertThat(second.id()).isEqualTo(itemId2);
        assertThat(second.tags()).containsExactly(tagLookup.get(tagId3));

        verify(itemSearchRepo).searchItemsIds(owner, req);
        verify(itemRepo).findByIdIn(List.of(itemId1, itemId2));
    }

    @Test
    void searchReturnsEmptyListWhenNothingFound() {
        UUID owner = UUID.randomUUID();
        ItemSearchRequest req = new ItemSearchRequest(null, null, null, null);

        when(itemSearchRepo.searchItemsIds(owner, req)).thenReturn(List.of());
        when(itemRepo.findByIdIn(List.of())).thenReturn(List.of());
        when(linkRepo.findByItemIdIn(List.of())).thenReturn(List.of());

        List<ItemResponse> responses = itemService.search(req, owner);

        assertThat(responses).isEmpty();
        verifyNoInteractions(tagService);
    }

    @Test
    void searchLeavesTagsEmptyWhenItemsHaveNoLinks() {
        UUID owner = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        ItemSearchRequest req = new ItemSearchRequest(List.of(), List.of(), 5, 1);

        when(itemSearchRepo.searchItemsIds(owner, req)).thenReturn(List.of(itemId));

        ItemEntity entity = buildItem(itemId, owner, "Lonely", "No tags", Instant.parse("2024-03-01T00:00:00Z"));
        when(itemRepo.findByIdIn(List.of(itemId))).thenReturn(List.of(entity));
        when(linkRepo.findByItemIdIn(List.of(itemId))).thenReturn(List.of());

        List<ItemResponse> responses = itemService.search(req, owner);

        assertThat(responses).hasSize(1);
        ItemResponse response = responses.getFirst();
        assertThat(response.tags()).isEmpty();

        verify(tagService, never()).getByIds(eq(owner), anyList());
    }

    @Test
    void searchRequestsTagsPerItemWhenMultipleItemsReturned() {
        UUID owner = UUID.randomUUID();
        UUID itemId1 = UUID.randomUUID();
        UUID itemId2 = UUID.randomUUID();
        UUID tagId1 = UUID.randomUUID();
        UUID tagId2 = UUID.randomUUID();
        UUID tagId3 = UUID.randomUUID();

        ItemSearchRequest.FunctionalFilter filter =
                new ItemSearchRequest.FunctionalFilter("score", ItemSearchRequest.Op.GT, ItemSearchRequest.ValueType.NUMBER, 5);
        ItemSearchRequest req = new ItemSearchRequest(List.of("alpha", "beta"), List.of(filter), 20, 2);

        when(itemSearchRepo.searchItemsIds(owner, req)).thenReturn(List.of(itemId1, itemId2));

        ItemEntity first = buildItem(itemId1, owner, "Alpha", "Body A", Instant.parse("2024-04-01T00:00:00Z"));
        ItemEntity second = buildItem(itemId2, owner, "Beta", "Body B", Instant.parse("2024-05-01T00:00:00Z"));
        when(itemRepo.findByIdIn(List.of(itemId1, itemId2))).thenReturn(List.of(first, second));

        ItemTagEntity l1 = link(itemId1, tagId1);
        ItemTagEntity l2 = link(itemId1, tagId2);
        ItemTagEntity l3 = link(itemId2, tagId2);
        ItemTagEntity l4 = link(itemId2, tagId3);
        when(linkRepo.findByItemIdIn(List.of(itemId1, itemId2))).thenReturn(List.of(l1, l2, l3, l4));

        Map<UUID, TagResponse> tagLookup = new HashMap<>();
        tagLookup.put(tagId1, new TagResponse(tagId1, owner, TagKind.SINGLE, "alpha", TagValueType.STRING, null, null, Instant.now()));
        tagLookup.put(tagId2, new TagResponse(tagId2, owner, TagKind.FUNCTIONAL, "score", TagValueType.NUMBER, 7, "7", Instant.now()));
        tagLookup.put(tagId3, new TagResponse(tagId3, owner, TagKind.SINGLE, "beta", TagValueType.STRING, null, null, Instant.now()));

        when(tagService.getByIds(eq(owner), anyList())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<UUID> ids = invocation.getArgument(1);
            return ids.stream().map(tagLookup::get).toList();
        });

        List<ItemResponse> responses = itemService.search(req, owner);

        assertThat(responses).hasSize(2);
        assertThat(responses.getFirst().tags()).containsExactly(tagLookup.get(tagId1), tagLookup.get(tagId2));
        assertThat(responses.get(1).tags()).containsExactly(tagLookup.get(tagId2), tagLookup.get(tagId3));

        var captured = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(tagService, times(2)).getByIds(eq(owner), captured.capture());
        assertThat(captured.getAllValues())
                .containsExactlyInAnyOrder(List.of(tagId1, tagId2), List.of(tagId2, tagId3));
    }

    private static ItemEntity buildItem(UUID id, UUID owner, String title, String body, Instant timestamp) {
        ItemEntity e = new ItemEntity();
        e.setId(id);
        e.setOwnerUserId(owner);
        e.setTitle(title);
        e.setBody(body);
        e.setCreatedAt(timestamp);
        e.setUpdatedAt(timestamp);
        return e;
    }

    private static ItemTagEntity link(UUID itemId, UUID tagId) {
        ItemTagEntity link = new ItemTagEntity();
        link.setItemId(itemId);
        link.setTagId(tagId);
        return link;
    }
}
