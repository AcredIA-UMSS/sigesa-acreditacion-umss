package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.application.port.out.EvidenceSearchQueryPort;
import com.umss.sigesa.application.service.normative.NormativeSearchQueryNormalizer;
import com.umss.sigesa.domain.model.EvidenceSearchCriteria;
import com.umss.sigesa.domain.model.EvidenceSearchHit;
import com.umss.sigesa.domain.model.EvidenceSearchPage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Repository
public class EvidenceSearchJpaAdapter implements EvidenceSearchQueryPort {

    private static final String BASE_FROM = """
            FROM evidence e
            INNER JOIN evidence_version ev ON ev.id = e.latest_version_id
            LEFT JOIN subphases sp ON sp.id = e.subphase_id
            LEFT JOIN phases ph ON ph.id = sp.phase_id
            LEFT JOIN accreditation_processes ap ON ap.id = ph.process_id
            LEFT JOIN indicator ind ON ind.id = e.indicator_id
            """;

    private static final String BASE_SELECT = """
            SELECT e.id AS evidence_id,
                   e.subphase_id,
                   sp.name AS subphase_name,
                   ph.id AS phase_id,
                   ph.name AS phase_name,
                   ap.id AS process_id,
                   e.indicator_id,
                   ind.code AS indicator_code,
                   ind.title AS indicator_title,
                   ev.version_number,
                   ev.description,
                   ev.original_filename,
                   ev.created_at,
                   ev.created_by,
                   ev.blob_purged
            """ + BASE_FROM;

    private static final String BASE_COUNT = "SELECT COUNT(*) " + BASE_FROM;

    private static final String CONTEXT_VECTOR = """
            to_tsvector('spanish',
                coalesce(sp.name, '') || ' ' ||
                coalesce(ph.name, '') || ' ' ||
                coalesce(ind.code, '') || ' ' ||
                coalesce(ind.title, '')
            )
            """;

    private final EntityManager entityManager;
    private final DataSource dataSource;
    private volatile Boolean fullTextAvailable;

    public EvidenceSearchJpaAdapter(EntityManager entityManager, DataSource dataSource) {
        this.entityManager = entityManager;
        this.dataSource = dataSource;
    }

    @Override
    @Transactional(readOnly = true)
    public EvidenceSearchPage search(EvidenceSearchCriteria criteria, List<UUID> allowedProgramIds) {
        boolean useFullText = hasTextQuery(criteria) && isPostgreSql() && hasFullTextColumn();
        FilterClause filterClause = buildFilterClause(criteria, allowedProgramIds, useFullText);
        String orderClause = buildOrderClause(criteria, useFullText, filterClause.params());

        Query countQuery = entityManager.createNativeQuery(BASE_COUNT + filterClause.sql());
        bindParams(countQuery, filterClause.params());
        long total = ((Number) countQuery.getSingleResult()).longValue();

        Query dataQuery = entityManager.createNativeQuery(
                BASE_SELECT + filterClause.sql() + orderClause);
        bindParams(dataQuery, filterClause.params());
        dataQuery.setFirstResult(criteria.page() * criteria.size());
        dataQuery.setMaxResults(criteria.size());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        List<EvidenceSearchHit> items = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            items.add(mapRow(row));
        }
        return new EvidenceSearchPage(items, total, criteria.page(), criteria.size());
    }

    private static boolean hasTextQuery(EvidenceSearchCriteria criteria) {
        return criteria.query() != null && !criteria.query().isBlank();
    }

    private String buildOrderClause(EvidenceSearchCriteria criteria, boolean useFullText, Map<String, Object> params) {
        if (!useFullText) {
            return " ORDER BY ev.created_at DESC ";
        }
        String ftsQuery = resolveFtsQuery(criteria.query());
        params.put("ftsQuery", ftsQuery);
        return " ORDER BY ("
                + " ts_rank(ev.search_vector, plainto_tsquery('spanish', :ftsQuery))"
                + " + ts_rank(" + CONTEXT_VECTOR + ", plainto_tsquery('spanish', :ftsQuery))"
                + " ) DESC NULLS LAST, ev.created_at DESC ";
    }

    private static FilterClause buildFilterClause(
            EvidenceSearchCriteria criteria,
            List<UUID> allowedProgramIds,
            boolean useFullText) {
        StringBuilder sql = new StringBuilder(" WHERE 1=1 ");
        Map<String, Object> params = new HashMap<>();

        appendScopeFilters(sql, params, criteria, allowedProgramIds);
        appendTextFilter(sql, params, criteria, useFullText);

        return new FilterClause(sql.toString(), params);
    }

    private static void appendScopeFilters(
            StringBuilder sql,
            Map<String, Object> params,
            EvidenceSearchCriteria criteria,
            List<UUID> allowedProgramIds) {
        if (allowedProgramIds != null) {
            sql.append("""
                     AND (
                       (ap.career_id IN :allowedProgramIds)
                       OR (e.subphase_id IS NULL AND ind.program_id IN :allowedProgramIds)
                     )
                    """);
            params.put("allowedProgramIds", allowedProgramIds);
        }

        if (criteria.processId() != null) {
            sql.append(" AND ap.id = :processId ");
            params.put("processId", criteria.processId());
        }
        if (criteria.phaseId() != null) {
            sql.append(" AND ph.id = :phaseId ");
            params.put("phaseId", criteria.phaseId());
        }
        if (criteria.subphaseId() != null) {
            sql.append(" AND sp.id = :subphaseId ");
            params.put("subphaseId", criteria.subphaseId());
        }
        if (criteria.indicatorId() != null) {
            sql.append(" AND e.indicator_id = :indicatorId ");
            params.put("indicatorId", criteria.indicatorId());
        }
        if (criteria.programId() != null) {
            sql.append(" AND (ap.career_id = :programId OR ind.program_id = :programId) ");
            params.put("programId", criteria.programId());
        }
        if (criteria.managementYear() != null) {
            sql.append(" AND (EXTRACT(YEAR FROM ap.start_date) = :managementYear ");
            sql.append(" OR EXTRACT(YEAR FROM ev.created_at) = :managementYear) ");
            params.put("managementYear", criteria.managementYear());
        }
    }

    private static void appendTextFilter(
            StringBuilder sql,
            Map<String, Object> params,
            EvidenceSearchCriteria criteria,
            boolean useFullText) {
        if (!hasTextQuery(criteria)) {
            return;
        }

        if (useFullText) {
            String ftsQuery = resolveFtsQuery(criteria.query());
            params.put("ftsQuery", ftsQuery);
            sql.append("""
                     AND (
                       ev.search_vector @@ plainto_tsquery('spanish', :ftsQuery)
                       OR """).append(CONTEXT_VECTOR).append("""
                     @@ plainto_tsquery('spanish', :ftsQuery)
                     )
                    """);
            return;
        }

        sql.append("""
                 AND (
                   LOWER(ev.description) LIKE :q
                   OR LOWER(COALESCE(ev.original_filename, '')) LIKE :q
                   OR LOWER(COALESCE(sp.name, '')) LIKE :q
                   OR LOWER(COALESCE(ph.name, '')) LIKE :q
                   OR LOWER(COALESCE(ind.code, '')) LIKE :q
                   OR LOWER(COALESCE(ind.title, '')) LIKE :q
                 )
                """);
        params.put("q", "%" + criteria.query().trim().toLowerCase(Locale.ROOT) + "%");
    }

    private static String resolveFtsQuery(String rawQuery) {
        String condensed = NormativeSearchQueryNormalizer.condensedForSearch(rawQuery);
        if (!condensed.isBlank()) {
            return condensed;
        }
        return NormativeSearchQueryNormalizer.sanitize(rawQuery);
    }

    private static void bindParams(Query query, Map<String, Object> params) {
        params.forEach(query::setParameter);
    }

    private boolean hasFullTextColumn() {
        if (fullTextAvailable != null) {
            return fullTextAvailable;
        }
        try (Connection connection = dataSource.getConnection()) {
            var columns = connection.getMetaData()
                    .getColumns(null, null, "evidence_version", "search_vector");
            fullTextAvailable = columns.next();
        } catch (SQLException ex) {
            fullTextAvailable = false;
        }
        return fullTextAvailable;
    }

    private boolean isPostgreSql() {
        try (Connection connection = dataSource.getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName();
            return product != null && product.toLowerCase(Locale.ROOT).contains("postgresql");
        } catch (SQLException ex) {
            return false;
        }
    }

    private static EvidenceSearchHit mapRow(Object[] row) {
        return new EvidenceSearchHit(
                asUuid(row[0]),
                asUuid(row[1]),
                asString(row[2]),
                asUuid(row[3]),
                asString(row[4]),
                asUuid(row[5]),
                asUuid(row[6]),
                asString(row[7]),
                asString(row[8]),
                row[9] != null ? ((Number) row[9]).intValue() : 0,
                asString(row[10]),
                asString(row[11]),
                asDateTime(row[12]),
                asUuid(row[13]),
                !toBoolean(row[14])
        );
    }

    private static boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(value.toString());
    }

    private static UUID asUuid(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(value.toString());
    }

    private static String asString(Object value) {
        return value != null ? value.toString() : null;
    }

    private static LocalDateTime asDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return null;
    }

    private record FilterClause(String sql, Map<String, Object> params) {
    }
}
