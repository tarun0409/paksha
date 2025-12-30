package com.operationt.self.paksha.tag.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.operationt.self.paksha.common.PaginationUtils;
import com.operationt.self.paksha.tag.dto.TagResponse;
import com.operationt.self.paksha.tag.dto.TagUpsertRequest;
import com.operationt.self.paksha.tag.entity.TagEntity;
import com.operationt.self.paksha.tag.entity.TagKind;
import com.operationt.self.paksha.tag.entity.TagValueType;
import com.operationt.self.paksha.tag.repo.TagRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class TagService {

    private final TagRepository tagRepo;
    private final ObjectMapper objectMapper;

    public TagService(TagRepository tagRepo, ObjectMapper objectMapper) {
        this.tagRepo = tagRepo;
        this.objectMapper = objectMapper;
    }
    public TagResponse get(UUID owner, UUID key) {
        return tagRepo.findById(key)
                .map(entity -> toResponse(entity))
                .orElseThrow(() -> new IllegalArgumentException("Invalid ID"));
    }
    public List<TagResponse> getByIds(UUID owner, List<UUID> keys) {
        return tagRepo.findAllByIdInAndOwnerUserId(keys, owner).stream()
                .map(this::toResponse).toList();
    }

    public List<TagResponse> get(UUID owner, Integer limit, Integer offset) {
        Pageable pageable = PaginationUtils.buildPageable(limit, offset, Sort.by("createdAt").descending());
        return tagRepo.findByOwnerUserId(owner, pageable).stream().map(this::toResponse).toList();
    }

    @Transactional
    public TagResponse upsert(UUID ownerUserId, TagUpsertRequest req) {
        String key = normalizeKey(req.key());

        if (req.kind() == TagKind.SINGLE) {
            // idempotent: return existing if present
            Optional<TagEntity> existing = tagRepo.findByOwnerUserIdAndKindAndKey(ownerUserId, TagKind.SINGLE, key);
            if (existing.isPresent()) return toResponse(existing.get());

            TagEntity created = new TagEntity();
            created.setId(UUID.randomUUID());
            created.setOwnerUserId(ownerUserId);
            created.setKind(TagKind.SINGLE);
            created.setKey(key);
            created.setValueType(null);
            created.setValueCanonical(null);
            created.setCreatedAt(Instant.now());

            try {
                return toResponse(tagRepo.save(created));
            } catch (DataIntegrityViolationException e) {
                // race: fetch and return
                return toResponse(tagRepo.findByOwnerUserIdAndKindAndKey(ownerUserId, TagKind.SINGLE, key).orElseThrow());
            }
        }

        // FUNCTIONAL
        if (req.valueType() == null) throw new IllegalArgumentException("FUNCTIONAL tag requires valueType");
        if (req.value() == null) throw new IllegalArgumentException("FUNCTIONAL tag requires value");

        TagValueType vt = TagValueType.valueOf(req.valueType().name());
        String canonical = canonicalize(vt, req.value());

        Optional<TagEntity> existing = tagRepo.findByOwnerUserIdAndKindAndKeyAndValueTypeAndValueCanonical(
                ownerUserId, TagKind.FUNCTIONAL, key, vt, canonical
        );
        if (existing.isPresent()) return toResponse(existing.get());

        TagEntity created = new TagEntity();
        created.setId(UUID.randomUUID());
        created.setOwnerUserId(ownerUserId);
        created.setKind(TagKind.FUNCTIONAL);
        created.setKey(key);
        created.setValueType(vt);
        created.setValueCanonical(canonical);
        created.setCreatedAt(Instant.now());

        applyValue(created, vt, req.value());

        try {
            return toResponse(tagRepo.save(created));
        } catch (DataIntegrityViolationException e) {
            return toResponse(tagRepo.findByOwnerUserIdAndKindAndKeyAndValueTypeAndValueCanonical(
                    ownerUserId, TagKind.FUNCTIONAL, key, vt, canonical
            ).orElseThrow());
        }
    }

    private static String normalizeKey(String k) {
        return k.trim().toLowerCase(Locale.ROOT);
    }

    private String canonicalize(TagValueType vt, Object v) {
        return switch (vt) {
            case STRING -> String.valueOf(v).trim().toLowerCase(Locale.ROOT);
            case NUMBER -> toBigDecimal(v).stripTrailingZeros().toPlainString();
            case BOOL -> toBoolean(v).toString();
            case DATE -> toLocalDate(v).toString(); // yyyy-MM-dd
            case REF -> String.valueOf(v).trim();   // typically UUID string
            case JSON -> {
                try {
                    // stable canonical JSON
                    Object parsed = (v instanceof String s) ? objectMapper.readTree(s) : v;
                    yield objectMapper.writeValueAsString(parsed);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid JSON value");
                }
            }
        };
    }

    private void applyValue(TagEntity t, TagValueType vt, Object v) {
        // clear
        t.setValueString(null);
        t.setValueNumber(null);
        t.setValueBool(null);
        t.setValueDate(null);
        t.setValueJson(null);

        switch (vt) {
            case STRING -> t.setValueString(String.valueOf(v).trim());
            case NUMBER -> t.setValueNumber(toBigDecimal(v));
            case BOOL -> t.setValueBool(toBoolean(v));
            case DATE -> t.setValueDate(toLocalDate(v));
            case REF -> t.setValueString(String.valueOf(v).trim()); // store ref in value_string
            case JSON -> {
                try {
                    t.setValueJson(objectMapper.valueToTree(v));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid JSON value");
                }
            }
        }
    }

    private static BigDecimal toBigDecimal(Object v) {
        if (v instanceof Number n) return new BigDecimal(n.toString());
        if (v instanceof String s) return new BigDecimal(s.trim());
        throw new IllegalArgumentException("NUMBER must be a number or numeric string");
    }

    private static Boolean toBoolean(Object v) {
        if (v instanceof Boolean b) return b;
        if (v instanceof String s) return Boolean.parseBoolean(s.trim());
        throw new IllegalArgumentException("BOOL must be boolean or boolean string");
    }

    private static LocalDate toLocalDate(Object v) {
        if (v instanceof String s) return LocalDate.parse(s.trim());
        throw new IllegalArgumentException("DATE must be ISO yyyy-MM-dd string");
    }

    private TagResponse toResponse(TagEntity t) {
        Object value = switch (t.getKind()) {
            case SINGLE -> null;
            case FUNCTIONAL -> switch (t.getValueType()) {
                case STRING -> t.getValueString();
                case NUMBER -> t.getValueNumber();
                case BOOL -> t.getValueBool();
                case DATE -> t.getValueDate() == null ? null : t.getValueDate().toString();
                case REF -> t.getValueString();
                case JSON -> t.getValueJson();
            };
        };

        return new TagResponse(
                t.getId(),
                t.getOwnerUserId(),
                TagKind.valueOf(t.getKind().name()),
                t.getKey(),
                t.getValueType() == null ? null : TagValueType.valueOf(t.getValueType().name()),
                value,
                t.getValueCanonical(),
                t.getCreatedAt()
        );
    }
}

