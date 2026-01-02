package com.operationt.self.paksha.item.service;

import com.operationt.self.paksha.fixtures.SearchTestData;
import com.operationt.self.paksha.item.dto.ItemResponse;
import com.operationt.self.paksha.item.dto.ItemSearchRequest;
import com.operationt.self.paksha.item.repo.ItemSearchRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.UUID;
import static org.junit.Assert.assertEquals;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ItemServiceTest {

    @Autowired
    ItemSearchRepository searchRepo;

    @Autowired
    ItemService itemService;

    @Autowired
    SearchTestData searchTestData;

    @BeforeEach
    void seed() {
        searchTestData.seedBasicScenario();
    }

    @AfterEach
    void cleanup() {
        searchTestData.cleanup();
    }

    // ---------------------- TESTS ----------------------

    @Test
    void oneSingleTagSearch() {
        var req = new ItemSearchRequest(
                List.of("finished_test"),
                List.of(),
                null,
                null
        );

        List<UUID> result = searchRepo.searchItemsIds(searchTestData.owner(), req);

        // In our fixture, items A and C have finished, but C is missing monthDec etc.
        // In seedBasicScenario: A and C have finished.
        // Since ORDER BY updated_at DESC: C first, then A.
        assertThat(result).containsExactly(
                searchTestData.getItems().get(2), // C
                searchTestData.getItems().get(0)  // A
        );
    }


    @Test
    void oneFunctionalTagSearch_pointsGte() {
        var req = new ItemSearchRequest(
                List.of(),
                List.of(new ItemSearchRequest.FunctionalFilter("points_test", ItemSearchRequest.Op.GTE, ItemSearchRequest.ValueType.NUMBER, 5)),
                null,
                null
        );

        List<UUID> result = searchRepo.searchItemsIds(searchTestData.owner(), req);

        // In fixture, A and B have points=5; C doesn't.
        assertThat(result).containsExactly(
                searchTestData.getItems().get(1), // B updatedAt 20
                searchTestData.getItems().get(0)  // A updatedAt 10
        );
    }

    @Test
    void multipleSingleTagSearch_allMustMatch() {

        var req = new ItemSearchRequest(List.of("personal_test", "finished_test"), List.of(), null, null);

        List<UUID> result = searchRepo.searchItemsIds(searchTestData.owner(), req);

        // ORDER BY updated_at DESC in query: c (30) then a (10)
        assertThat(result).containsExactly(searchTestData.getItems().get(2), searchTestData.getItems().get(0));
    }

    @Test
    void multipleFunctionalTagSearch_allMustMatch() {

        var req = new ItemSearchRequest(
                List.of(),
                List.of(
                        new ItemSearchRequest.FunctionalFilter("points_test", ItemSearchRequest.Op.GTE, ItemSearchRequest.ValueType.NUMBER, 5),
                        new ItemSearchRequest.FunctionalFilter("month_test", ItemSearchRequest.Op.EQ, ItemSearchRequest.ValueType.STRING, "december")
                ),
                null, null
        );

        List<UUID> result = searchRepo.searchItemsIds(searchTestData.owner(), req);

        assertThat(result).containsExactly(searchTestData.getItems().getFirst());
    }

    @Test
    void oneSingleAndOneFunctionalTagSearch() {

        var req = new ItemSearchRequest(
                List.of("finished_test"),
                List.of(new ItemSearchRequest.FunctionalFilter("points_test", ItemSearchRequest.Op.EQ, ItemSearchRequest.ValueType.NUMBER, 5)),
                null, null
        );

        List<UUID> result = searchRepo.searchItemsIds(searchTestData.owner(), req);

        assertThat(result).containsExactly(searchTestData.getItems().getFirst());
    }

    @Test
    void multipleSingleAndMultipleFunctionalTagSearch() {
        var req = new ItemSearchRequest(
                List.of("personal_test", "finished_test"),
                List.of(
                        new ItemSearchRequest.FunctionalFilter("points_test", ItemSearchRequest.Op.GTE, ItemSearchRequest.ValueType.NUMBER, 5),
                        new ItemSearchRequest.FunctionalFilter("month_test", ItemSearchRequest.Op.EQ, ItemSearchRequest.ValueType.STRING, "december"),
                        new ItemSearchRequest.FunctionalFilter("urgency_test", ItemSearchRequest.Op.EQ, ItemSearchRequest.ValueType.STRING, "casual")
                ),
                null, null
        );

        List<UUID> result = searchRepo.searchItemsIds(searchTestData.owner(), req);

        assertThat(result).containsExactly(searchTestData.getItems().getFirst());
    }

    // -------- One test case for each functional type (STRING/NUMBER/BOOL/DATE) --------

    @Test
    void functionalType_string_eq() {

        var req = new ItemSearchRequest(
                List.of(),
                List.of(new ItemSearchRequest.FunctionalFilter("month_test", ItemSearchRequest.Op.EQ, ItemSearchRequest.ValueType.STRING, "DECEMBER")),
                null, null
        );

        List<UUID> result = searchRepo.searchItemsIds(searchTestData.owner(), req);
        assertThat(result).containsExactly(searchTestData.getItems().getFirst());
    }

    @Test
    void functionalType_number_lte() {

        var req = new ItemSearchRequest(
                List.of(),
                List.of(new ItemSearchRequest.FunctionalFilter("points_test", ItemSearchRequest.Op.LTE, ItemSearchRequest.ValueType.NUMBER, 3)),
                null, null
        );

        List<UUID> result = searchRepo.searchItemsIds(searchTestData.owner(), req);
        assertThat(result).containsExactly(searchTestData.getItems().getFirst());
    }

    @Test
    void functionalType_bool_eq() {
        var req = new ItemSearchRequest(
                List.of(),
                List.of(new ItemSearchRequest.FunctionalFilter("archived_test", ItemSearchRequest.Op.EQ, ItemSearchRequest.ValueType.BOOL, true)),
                null, null
        );

        List<UUID> result = searchRepo.searchItemsIds(searchTestData.owner(), req);
        assertThat(result).containsExactly(searchTestData.getItems().getFirst());
    }

    @Test
    void functionalType_date_gte() {
        var req = new ItemSearchRequest(
                List.of(),
                List.of(new ItemSearchRequest.FunctionalFilter("created_day_test", ItemSearchRequest.Op.GTE, ItemSearchRequest.ValueType.DATE, "2025-12-01")),
                null, null
        );

        List<UUID> result = searchRepo.searchItemsIds(searchTestData.owner(), req);
        assertThat(result).containsExactly(searchTestData.getItems().getFirst());
    }

    @Test
    void appliesLimitAndOffset() {
        var req = new ItemSearchRequest(
                List.of("finished_test"),
                List.of(),
                1,
                1
        );

        List<UUID> result = searchRepo.searchItemsIds(searchTestData.owner(), req);

        assertThat(result).containsExactly(searchTestData.getItems().getFirst());
    }
    @Test
    void unassociateItemTag() {
        List<UUID> tagsToUnassociate = searchTestData.getTagsToUnassociate();
        ItemResponse res = itemService.unassociateTag(searchTestData.owner(),
                searchTestData.getItems().get(3),
                tagsToUnassociate.getFirst());
        assertEquals(1, res.tags().size());
        res = itemService.unassociateTag(searchTestData.owner(),
                searchTestData.getItems().get(3),
                tagsToUnassociate.get(1));
        assertEquals(0,res.tags().size());

    }
}
