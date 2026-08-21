package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.NormativeDocumentJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataNormativeDocumentRepository;
import com.umss.sigesa.application.model.normative.NormativeDocumentHit;
import com.umss.sigesa.application.port.out.NormativeDocumentSearchPort;
import com.umss.sigesa.application.service.normative.NormativeSearchQueryNormalizer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class NormativeDocumentSearchJpaAdapter implements NormativeDocumentSearchPort {

    private static final int SNIPPET_MAX = 420;

    private final SpringDataNormativeDocumentRepository repository;
    private final DataSource dataSource;
    private volatile Boolean fullTextAvailable;

    @PersistenceContext
    private EntityManager entityManager;

    public NormativeDocumentSearchJpaAdapter(SpringDataNormativeDocumentRepository repository,
                                             DataSource dataSource) {
        this.repository = repository;
        this.dataSource = dataSource;
    }

    @Override
    public List<NormativeDocumentHit> search(String query, String templateType, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        int effectiveLimit = Math.clamp(limit, 1, 10);
        String normalizedTemplate = normalizeTemplateType(templateType);
        String sanitizedQuery = NormativeSearchQueryNormalizer.sanitize(query);
        String condensedQuery = NormativeSearchQueryNormalizer.condensedForSearch(query);

        List<NormativeDocumentHit> hits = searchCandidates(
                sanitizedQuery,
                condensedQuery,
                normalizedTemplate,
                effectiveLimit);
        if (!hits.isEmpty()) {
            return hits;
        }

        for (String term : NormativeSearchQueryNormalizer.significantTerms(query)) {
            hits = mergeHits(hits, searchSingleTerm(term, normalizedTemplate, effectiveLimit));
            if (hits.size() >= effectiveLimit) {
                break;
            }
        }
        return hits.stream().limit(effectiveLimit).toList();
    }

    private List<NormativeDocumentHit> searchCandidates(String sanitizedQuery,
                                                        String condensedQuery,
                                                        String templateType,
                                                        int limit) {
        if (isPostgreSql() && hasFullTextColumn()) {
            List<NormativeDocumentHit> condensedHits = searchWithFullText(condensedQuery, templateType, limit);
            if (!condensedHits.isEmpty()) {
                return condensedHits;
            }
            return searchWithFullTextWeb(sanitizedQuery, templateType, limit);
        }

        List<NormativeDocumentHit> condensedHits = searchWithLike(condensedQuery, templateType, limit);
        if (!condensedHits.isEmpty()) {
            return condensedHits;
        }
        return searchWithLike(sanitizedQuery, templateType, limit);
    }

    private List<NormativeDocumentHit> searchSingleTerm(String term,
                                                        String templateType,
                                                        int limit) {
        if (isPostgreSql() && hasFullTextColumn()) {
            List<NormativeDocumentHit> hits = searchWithFullText(term, templateType, limit);
            if (!hits.isEmpty()) {
                return hits;
            }
        }
        return searchWithLike(term, templateType, limit);
    }

    private static List<NormativeDocumentHit> mergeHits(List<NormativeDocumentHit> base,
                                                        List<NormativeDocumentHit> extra) {
        Map<String, NormativeDocumentHit> merged = new LinkedHashMap<>();
        for (NormativeDocumentHit hit : base) {
            merged.put(dedupeKey(hit), hit);
        }
        for (NormativeDocumentHit hit : extra) {
            merged.putIfAbsent(dedupeKey(hit), hit);
        }
        return new ArrayList<>(merged.values());
    }

    private static String dedupeKey(NormativeDocumentHit hit) {
        return hit.title() + "|" + hit.sourceUrl();
    }

    @SuppressWarnings("unchecked")
    private List<NormativeDocumentHit> searchWithFullTextWeb(String query,
                                                             String templateType,
                                                             int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String sql = """
                SELECT title, template_type, phase_name, subphase_name, source_url, body_text,
                       ts_rank(search_vector, websearch_to_tsquery('spanish', :query)) AS rank
                FROM normative_document
                WHERE search_vector @@ websearch_to_tsquery('spanish', :query)
                  AND (:templateType IS NULL OR template_type = :templateType)
                ORDER BY rank DESC
                LIMIT :limit
                """;

        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("query", query)
                .setParameter("templateType", templateType)
                .setParameter("limit", limit)
                .getResultList();

        return mapFullTextRows(rows, query);
    }

    @SuppressWarnings("unchecked")
    private List<NormativeDocumentHit> searchWithFullText(String query,
                                                          String templateType,
                                                          int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String sql = """
                SELECT title, template_type, phase_name, subphase_name, source_url, body_text,
                       ts_rank(search_vector, plainto_tsquery('spanish', :query)) AS rank
                FROM normative_document
                WHERE search_vector @@ plainto_tsquery('spanish', :query)
                  AND (:templateType IS NULL OR template_type = :templateType)
                ORDER BY rank DESC
                LIMIT :limit
                """;

        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("query", query)
                .setParameter("templateType", templateType)
                .setParameter("limit", limit)
                .getResultList();

        return mapFullTextRows(rows, query);
    }

    private List<NormativeDocumentHit> mapFullTextRows(List<Object[]> rows, String highlightQuery) {
        List<NormativeDocumentHit> hits = new ArrayList<>();
        for (Object[] row : rows) {
            String bodyText = row[5] != null ? row[5].toString() : "";
            double score = row[6] != null ? ((Number) row[6]).doubleValue() : 0.0;
            hits.add(new NormativeDocumentHit(
                    stringValue(row[0]),
                    stringValue(row[1]),
                    stringValue(row[2]),
                    stringValue(row[3]),
                    stringValue(row[4]),
                    buildSnippet(bodyText, highlightQuery),
                    score));
        }
        return hits;
    }

    private List<NormativeDocumentHit> searchWithLike(String query,
                                                      String templateType,
                                                      int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        List<NormativeDocumentJpaEntity> entities = repository.searchByLike(
                query,
                templateType,
                PageRequest.of(0, limit));

        List<NormativeDocumentHit> hits = new ArrayList<>();
        for (NormativeDocumentJpaEntity entity : entities) {
            hits.add(new NormativeDocumentHit(
                    entity.getTitle(),
                    entity.getTemplateType(),
                    entity.getPhaseName(),
                    entity.getSubphaseName(),
                    entity.getSourceUrl(),
                    buildSnippet(entity.getBodyText(), query),
                    scoreLikeMatch(entity, query)));
        }
        return hits;
    }

    private static double scoreLikeMatch(NormativeDocumentJpaEntity entity, String query) {
        String lowerQuery = query.toLowerCase(Locale.ROOT);
        String title = entity.getTitle() != null ? entity.getTitle().toLowerCase(Locale.ROOT) : "";
        String body = entity.getBodyText() != null ? entity.getBodyText().toLowerCase(Locale.ROOT) : "";
        if (title.contains(lowerQuery)) {
            return 1.0;
        }
        if (body.contains(lowerQuery)) {
            return 0.6;
        }
        for (String term : NormativeSearchQueryNormalizer.significantTerms(query)) {
            String lowerTerm = term.toLowerCase(Locale.ROOT);
            if (title.contains(lowerTerm)) {
                return 0.9;
            }
            if (body.contains(lowerTerm)) {
                return 0.5;
            }
        }
        return 0.3;
    }

    private boolean hasFullTextColumn() {
        if (fullTextAvailable != null) {
            return fullTextAvailable;
        }
        try (Connection connection = dataSource.getConnection()) {
            var columns = connection.getMetaData().getColumns(null, null, "normative_document", "search_vector");
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

    private static String normalizeTemplateType(String templateType) {
        if (templateType == null || templateType.isBlank()) {
            return null;
        }
        String normalized = templateType.trim().toUpperCase(Locale.ROOT);
        if ("ARCUSUR".equals(normalized) || "ARCU_SUR".equals(normalized)) {
            return "ARCU-SUR";
        }
        return normalized;
    }

    private static String buildSnippet(String bodyText, String query) {
        if (bodyText == null || bodyText.isBlank()) {
            return "";
        }
        String compact = bodyText.replaceAll("\\s+", " ").trim();
        if (compact.length() <= SNIPPET_MAX) {
            return compact;
        }
        String lowerBody = compact.toLowerCase(Locale.ROOT);
        String lowerQuery = query.toLowerCase(Locale.ROOT);
        int index = lowerBody.indexOf(lowerQuery);
        if (index < 0) {
            for (String term : NormativeSearchQueryNormalizer.significantTerms(query)) {
                index = lowerBody.indexOf(term.toLowerCase(Locale.ROOT));
                if (index >= 0) {
                    break;
                }
            }
        }
        if (index < 0) {
            return compact.substring(0, SNIPPET_MAX) + "…";
        }
        int start = Math.max(0, index - 80);
        int end = Math.min(compact.length(), start + SNIPPET_MAX);
        String snippet = compact.substring(start, end);
        if (start > 0) {
            snippet = "…" + snippet;
        }
        if (end < compact.length()) {
            snippet = snippet + "…";
        }
        return snippet;
    }

    private static String stringValue(Object value) {
        return value != null ? value.toString() : null;
    }
}
