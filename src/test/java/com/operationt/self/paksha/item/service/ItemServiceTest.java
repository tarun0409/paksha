package com.operationt.self.paksha.item.service;

import com.operationt.self.paksha.fixtures.SearchTestData;
import com.operationt.self.paksha.item.dto.ItemResponse;
import com.operationt.self.paksha.item.dto.ItemSearchRequest;
import com.operationt.self.paksha.tag.entity.TagValueType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ItemServiceTest {

    @Autowired
    ItemService itemService;

    @Autowired
    SearchTestData searchTestData;

    @AfterEach
    void cleanup() {
        searchTestData.cleanup();
    }

    // ---------------------- TESTS ----------------------

    @Test
    void oneSingleTagSearch() {
        List<UUID> itemIds = searchTestData.createItems(List.of("A","B","C"));
        searchTestData.associateSingleTags(Map.of(
                itemIds.getFirst(),List.of("personal_test", "finished_test"),
                itemIds.get(1), List.of("personal_test"),
                itemIds.get(2), List.of("personal_test", "finished_test", "learning_test")));

        var req = new ItemSearchRequest(
                List.of("finished_test"),
                List.of(),
                null,
                null
        );
        List<ItemResponse> res = itemService.search(req, searchTestData.owner());
        List<UUID> ids = res.stream().map(ItemResponse::id).toList();

        assertThat(ids).containsExactlyInAnyOrder(
                itemIds.get(0),
                itemIds.get(2)
        );
    }


    @Test
    void oneFunctionalTagSearch_pointsGte() {
        List<UUID> itemIds = searchTestData.createItems(List.of("A","B","C"));
        searchTestData.associateFunctionalTags(Map.of(
                itemIds.getFirst(), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 5)),
                itemIds.get(1), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 6)),
                itemIds.get(2), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 3))));
        var req = new ItemSearchRequest(
                List.of(),
                List.of(new ItemSearchRequest.FunctionalFilter("points_test", ItemSearchRequest.Op.GTE, ItemSearchRequest.ValueType.NUMBER, 5)),
                null,
                null
        );

        List<ItemResponse> res = itemService.search(req, searchTestData.owner());
        List<UUID> ids = res.stream().map(ItemResponse::id).toList();
        assertThat(ids).containsExactlyInAnyOrder(
                itemIds.getFirst(),
                itemIds.get(1)
        );
    }

    @Test
    void multipleSingleTagSearch_allMustMatch() {
        List<UUID> itemIds = searchTestData.createItems(List.of("A","B","C"));
        searchTestData.associateSingleTags(Map.of(
                itemIds.getFirst(),List.of("personal_test", "finished_test"),
                itemIds.get(1), List.of("personal_test"),
                itemIds.get(2), List.of("personal_test", "finished_test", "learning_test")));

        var req = new ItemSearchRequest(List.of("personal_test", "finished_test"), List.of(), null, null);

        List<ItemResponse> res = itemService.search(req, searchTestData.owner());
        List<UUID> ids = res.stream().map(ItemResponse::id).toList();

        assertThat(ids).containsExactlyInAnyOrder(itemIds.getFirst(),
                itemIds.get(2));
    }

    @Test
    void multipleFunctionalTagSearch_allMustMatch() {
        List<UUID> itemIds = searchTestData.createItems(List.of("A","B","C"));

        searchTestData.associateFunctionalTags(Map.of(
                itemIds.getFirst(), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 5)),
                itemIds.get(1), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 6), new SearchTestData.FunctionalTag(TagValueType.STRING, "month_test", "december")),
                itemIds.get(2), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 3))));

        var req = new ItemSearchRequest(
                List.of(),
                List.of(
                        new ItemSearchRequest.FunctionalFilter("points_test", ItemSearchRequest.Op.GTE, ItemSearchRequest.ValueType.NUMBER, 5),
                        new ItemSearchRequest.FunctionalFilter("month_test", ItemSearchRequest.Op.EQ, ItemSearchRequest.ValueType.STRING, "december")
                ),
                null, null
        );

        List<ItemResponse> res = itemService.search(req, searchTestData.owner());
        List<UUID> ids = res.stream().map(ItemResponse::id).toList();

        assertThat(ids).containsExactlyInAnyOrder(itemIds.get(1));
    }

    @Test
    void oneSingleAndOneFunctionalTagSearch() {
        List<UUID> itemIds = searchTestData.createItems(List.of("A","B","C"));

        searchTestData.associateSingleTags(Map.of(
                itemIds.getFirst(),List.of("personal_test","finished_test"),
                itemIds.get(1), List.of("personal_test"),
                itemIds.get(2), List.of("personal_test", "finished_test")));
        searchTestData.associateFunctionalTags(Map.of(
                itemIds.getFirst(), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 5)),
                itemIds.get(1), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 6), new SearchTestData.FunctionalTag(TagValueType.STRING, "month_test", "december")),
                itemIds.get(2), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 3))));

        var req = new ItemSearchRequest(
                List.of("finished_test"),
                List.of(new ItemSearchRequest.FunctionalFilter("points_test", ItemSearchRequest.Op.EQ, ItemSearchRequest.ValueType.NUMBER, 5)),
                null, null
        );

        List<ItemResponse> res = itemService.search(req, searchTestData.owner());
        List<UUID> ids = res.stream().map(ItemResponse::id).toList();

        assertThat(ids).containsExactlyInAnyOrder(itemIds.getFirst());
    }

    @Test
    void multipleSingleAndMultipleFunctionalTagSearch() {
        List<UUID> itemIds = searchTestData.createItems(List.of("A","B","C"));

        searchTestData.associateSingleTags(Map.of(
                itemIds.getFirst(),List.of("personal_test"),
                itemIds.get(1), List.of("personal_test","finished_test"),
                itemIds.get(2), List.of("personal_test", "finished_test")));
        searchTestData.associateFunctionalTags(Map.of(
                itemIds.getFirst(), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 5)),
                itemIds.get(1), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 6),
                        new SearchTestData.FunctionalTag(TagValueType.STRING, "month_test", "december"),
                        new SearchTestData.FunctionalTag(TagValueType.STRING, "urgency_test", "casual")),
                itemIds.get(2), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 3))));


        var req = new ItemSearchRequest(
                List.of("personal_test", "finished_test"),
                List.of(
                        new ItemSearchRequest.FunctionalFilter("points_test", ItemSearchRequest.Op.GTE, ItemSearchRequest.ValueType.NUMBER, 5),
                        new ItemSearchRequest.FunctionalFilter("month_test", ItemSearchRequest.Op.EQ, ItemSearchRequest.ValueType.STRING, "december"),
                        new ItemSearchRequest.FunctionalFilter("urgency_test", ItemSearchRequest.Op.EQ, ItemSearchRequest.ValueType.STRING, "casual")
                ),
                null, null
        );

        List<ItemResponse> res = itemService.search(req, searchTestData.owner());
        List<UUID> ids = res.stream().map(ItemResponse::id).toList();

        assertThat(ids).containsExactlyInAnyOrder(itemIds.get(1));
    }

    @Test
    void functionalType_string_eq() {

        List<UUID> itemIds = searchTestData.createItems(List.of("A","B","C"));
        searchTestData.associateFunctionalTags(Map.of(
                itemIds.getFirst(), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 5)),
                itemIds.get(1), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 6), new SearchTestData.FunctionalTag(TagValueType.STRING, "month_test", "december")),
                itemIds.get(2), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 3),new SearchTestData.FunctionalTag(TagValueType.STRING, "month_test", "january"))));

        var req = new ItemSearchRequest(
                List.of(),
                List.of(new ItemSearchRequest.FunctionalFilter("month_test", ItemSearchRequest.Op.EQ, ItemSearchRequest.ValueType.STRING, "DECEMBER")),
                null, null
        );

        List<ItemResponse> res = itemService.search(req, searchTestData.owner());
        List<UUID> ids = res.stream().map(ItemResponse::id).toList();
        assertThat(ids).containsExactlyInAnyOrder(itemIds.get(1));
    }

    @Test
    void functionalType_number_lte() {
        List<UUID> itemIds = searchTestData.createItems(List.of("A","B","C"));
        searchTestData.associateFunctionalTags(Map.of(
                itemIds.getFirst(), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 5)),
                itemIds.get(1), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 6), new SearchTestData.FunctionalTag(TagValueType.STRING, "month_test", "december")),
                itemIds.get(2), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 3),new SearchTestData.FunctionalTag(TagValueType.STRING, "month_test", "january"))));
        var req = new ItemSearchRequest(
                List.of(),
                List.of(new ItemSearchRequest.FunctionalFilter("points_test", ItemSearchRequest.Op.LTE, ItemSearchRequest.ValueType.NUMBER, 5)),
                null, null
        );

        List<ItemResponse> res = itemService.search(req, searchTestData.owner());
        List<UUID> ids = res.stream().map(ItemResponse::id).toList();
        assertThat(ids).containsExactlyInAnyOrder(itemIds.get(0), itemIds.get(2));
    }

    @Test
    void functionalType_bool_eq() {
        List<UUID> itemIds = searchTestData.createItems(List.of("A","B","C"));
        searchTestData.associateFunctionalTags(Map.of(
                itemIds.getFirst(), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 5)),
                itemIds.get(1), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 6), new SearchTestData.FunctionalTag(TagValueType.BOOL, "archived_test", false)),
                itemIds.get(2), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 3),new SearchTestData.FunctionalTag(TagValueType.BOOL, "archived_test", true))));
        var req = new ItemSearchRequest(
                List.of(),
                List.of(new ItemSearchRequest.FunctionalFilter("archived_test", ItemSearchRequest.Op.EQ, ItemSearchRequest.ValueType.BOOL, true)),
                null, null
        );

        List<ItemResponse> res = itemService.search(req, searchTestData.owner());
        List<UUID> ids = res.stream().map(ItemResponse::id).toList();
        assertThat(ids).containsExactlyInAnyOrder(itemIds.get(2));
    }

    @Test
    void functionalType_date_gte() {
        List<UUID> itemIds = searchTestData.createItems(List.of("A","B","C"));
        searchTestData.associateFunctionalTags(Map.of(
                itemIds.getFirst(), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 5)),
                itemIds.get(1), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 6), new SearchTestData.FunctionalTag(TagValueType.DATE, "created_day_test", LocalDate.parse("2025-12-11"))),
                itemIds.get(2), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 3),new SearchTestData.FunctionalTag(TagValueType.DATE, "created_day_test", LocalDate.parse("2025-11-11")))));

        var req = new ItemSearchRequest(
                List.of(),
                List.of(new ItemSearchRequest.FunctionalFilter("created_day_test", ItemSearchRequest.Op.GTE, ItemSearchRequest.ValueType.DATE, "2025-12-01")),
                null, null
        );

        List<ItemResponse> res = itemService.search(req, searchTestData.owner());
        List<UUID> ids = res.stream().map(ItemResponse::id).toList();
        assertThat(ids).containsExactlyInAnyOrder(itemIds.get(1));
    }


    @Test
    void associateItemTag() {
        List<UUID> itemIds = searchTestData.createItems(List.of("A"));
        List<UUID> singleTags = searchTestData.createSingleTags(List.of("tag_to_associate_1"));
        List<UUID> functionalTags = searchTestData.createFunctionalTags(List.of(new SearchTestData.FunctionalTag(TagValueType.BOOL, "archived_test", false)));
        ItemResponse res = itemService.associateTag(searchTestData.owner(), itemIds.getFirst(), singleTags.getFirst());
        Assertions.assertEquals(1, res.tags().size());
        res = itemService.associateTag(searchTestData.owner(),
                itemIds.getFirst(),
                functionalTags.getFirst());
        Assertions.assertEquals(2, res.tags().size());
    }
    @Test
    void unassociateItemTag() {
        List<UUID> itemIds = searchTestData.createItems(List.of("A"));

        Map<UUID, List<UUID>> sTags = searchTestData.associateSingleTags(Map.of(
                itemIds.getFirst(),List.of("personal_test")));
        Map<UUID, List<UUID>> fTags = searchTestData.associateFunctionalTags(Map.of(
                itemIds.getFirst(), List.of(new SearchTestData.FunctionalTag(TagValueType.NUMBER, "points_test", 5))));

        ItemResponse res = itemService.unassociateTag(searchTestData.owner(),
                itemIds.getFirst(),
                sTags.get(itemIds.getFirst()).getFirst());
        Assertions.assertEquals(1, res.tags().size());
        res = itemService.unassociateTag(searchTestData.owner(),
                itemIds.getFirst(),
                fTags.get(itemIds.getFirst()).getFirst());
        Assertions.assertEquals(0, res.tags().size());

    }
}
