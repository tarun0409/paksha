package com.operationt.self.paksha.item.repo;

import com.operationt.self.paksha.item.dto.ItemSearchRequest;
import com.operationt.self.paksha.common.PaginationUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Repository
public class ItemSearchRepository {

    private final EntityManager em;

    public ItemSearchRepository(EntityManager em) {
        this.em = em;
    }
    public List<UUID> searchItemsIds(UUID ownerUserId, ItemSearchRequest req) {
        List<String> singleKeys = normalizeKeys(req.singleTags());
        List<ItemSearchRequest.FunctionalFilter> funcs = req.functional() == null ? List.of() : req.functional();
        int limit = PaginationUtils.sanitizeLimit(req.limit());
        int offset = PaginationUtils.sanitizeOffset(req.offset());

        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        sql.append("""
      SELECT i.id
      FROM items i
      WHERE i.owner_user_id = :owner
    """);
        params.put("owner", ownerUserId);

        // ---------------- SINGLE TAGS (must contain ALL) ----------------
        if (!singleKeys.isEmpty()) {
            sql.append("""
        AND i.id IN (
          SELECT it.item_id
          FROM item_tags it
          JOIN tags t ON t.id = it.tag_id
          WHERE t.owner_user_id = :owner
            AND t.kind = 'SINGLE'
            AND t.key = ANY(:singleKeys)
          GROUP BY it.item_id
          HAVING COUNT(DISTINCT t.key) = :singleCount
        )
      """);
            params.put("singleKeys", singleKeys.toArray(new String[0])); // text[]
            params.put("singleCount", singleKeys.size());
        }

        // ---------------- FUNCTIONAL FILTERS (must satisfy ALL) ----------------
        for (int idx = 0; idx < funcs.size(); idx++) {
            ItemSearchRequest.FunctionalFilter f = funcs.get(idx);
            if (f == null) continue;

            if (f.key() == null || f.key().isBlank()) throw new IllegalArgumentException("functional.key is required");
            if (f.op() == null) throw new IllegalArgumentException("functional.op is required");
            if (f.valueType() == null) throw new IllegalArgumentException("functional.valueType is required");
            if (f.value() == null) throw new IllegalArgumentException("functional.value is required");

            String keyParam = "fKey" + idx;
            String typeParam = "fType" + idx;
            String valParam = "fVal" + idx;

            String key = f.key().trim().toLowerCase(Locale.ROOT);
            params.put(keyParam, key);
            params.put(typeParam, f.valueType().name());

            String column = switch (f.valueType()) {
                case STRING -> "t.value_canonical";
                case NUMBER -> "t.value_number";
                case BOOL -> "t.value_bool";
                case DATE -> "t.value_date";
            };

            Object boundValue = switch (f.valueType()) {
                case STRING -> String.valueOf(f.value()).trim().toLowerCase(Locale.ROOT);
                case NUMBER -> toBigDecimal(f.value());
                case BOOL -> toBoolean(f.value());
                case DATE -> toLocalDate(f.value());
            };
            params.put(valParam, boundValue);

            String opSql = switch (f.op()) {
                case EQ -> "=";
                case NEQ -> "<>";
                case GT -> ">";
                case GTE -> ">=";
                case LT -> "<";
                case LTE -> "<=";
            };

            // --- inside the functional filters loop ---

            sql.append("\nAND EXISTS (\n")
                    .append("  SELECT 1\n")
                    .append("  FROM item_tags it\n")
                    .append("  JOIN tags t ON t.id = it.tag_id\n")
                    .append("  WHERE it.item_id = i.id\n")
                    .append("    AND t.owner_user_id = :owner\n")
                    .append("    AND t.kind = 'FUNCTIONAL'\n")
                    .append("    AND t.key = :").append(keyParam).append("\n")        // ✅ fixed
                    .append("    AND t.value_type = :").append(typeParam).append("\n") // ✅ already ok
                    .append("    AND ").append(column).append(" ").append(opSql).append(" :").append(valParam).append("\n")
                    .append(")\n");


//            sql.append("\nAND EXISTS (\n")
//                    .append("""
//          SELECT 1
//          FROM item_tags it
//          JOIN tags t ON t.id = it.tag_id
//          WHERE it.item_id = i.id
//            AND t.owner_user_id = :owner
//            AND t.kind = 'FUNCTIONAL'
//            AND t.key = :
//        """).append(keyParam).append("\n")
//                    .append("    AND t.value_type = :").append(typeParam).append("\n")
//                    .append("    AND ").append(column).append(" ").append(opSql).append(" :").append(valParam).append("\n")
//                    .append(")\n");
        }

        // Optional ordering (keeps deterministic output)
        sql.append(" ORDER BY i.updated_at DESC ");

        Query q = em.createNativeQuery(sql.toString());
        params.forEach(q::setParameter);
        q.setFirstResult(offset);
        q.setMaxResults(limit);

        @SuppressWarnings("unchecked")
        List<Object> rows = q.getResultList();

        List<UUID> ids = new ArrayList<>(rows.size());
        for (Object r : rows) {
            ids.add(r instanceof UUID u ? u : UUID.fromString(String.valueOf(r)));
        }
        return ids;
    }

    // ---------------- helpers ----------------

    private static List<String> normalizeKeys(List<String> keys) {
        if (keys == null) return List.of();
        return keys.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    private static BigDecimal toBigDecimal(Object v) {
        if (v instanceof Number n) return new BigDecimal(n.toString());
        if (v instanceof String s) return new BigDecimal(s.trim());
        throw new IllegalArgumentException("NUMBER value must be number or numeric string");
    }

    private static Boolean toBoolean(Object v) {
        if (v instanceof Boolean b) return b;
        if (v instanceof String s) return Boolean.parseBoolean(s.trim());
        throw new IllegalArgumentException("BOOL value must be boolean or boolean string");
    }

    private static LocalDate toLocalDate(Object v) {
        if (v instanceof LocalDate d) return d;
        if (v instanceof String s) return LocalDate.parse(s.trim()); // yyyy-MM-dd
        throw new IllegalArgumentException("DATE value must be ISO yyyy-MM-dd string");
    }
}


