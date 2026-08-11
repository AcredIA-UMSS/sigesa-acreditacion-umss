package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.in.web.dto.EvidenceSearchDetailDto;
import com.umss.sigesa.application.port.out.SearchEvidenceQueryPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class SearchEvidenceJpaAdapter implements SearchEvidenceQueryPort {

    @PersistenceContext
    private EntityManager entityManager;

    private final ThreadLocal<String> lastSql = new ThreadLocal<>();

    @Override
    public String getLastExecutedSql() {
        return lastSql.get();
    }

    private void saveExecutedSql(String jpql, String termino, String dimension, Integer anio, List<UUID> programScope) {
        String sql = jpql
                .replace("EvidenceVersionEntity ev", "evidence_version ev")
                .replace("EvidenceEntity e", "evidence e")
                .replace("IndicatorEntity ind", "indicator ind")
                .replace("ProgramJpaEntity p", "programs p")
                .replace("ev.evidenceId", "ev.evidence_id")
                .replace("e.indicatorId", "e.indicator_id")
                .replace("ind.programId", "ind.program_id")
                .replace("ev.criterionId", "ev.criterion_id")
                .replace("ev.storageKey", "ev.storage_key")
                .replace("ev.createdAt", "ev.created_at")
                .replace("e.latestVersionId", "e.latest_version_id");

        if (programScope != null) {
            sql = sql.replace(":programScope", programScope.toString());
        }
        if (dimension != null) {
            sql = sql.replace(":matchingCriteria", getCriteriaForDimension(dimension).toString());
        }
        if (termino != null) {
            sql = sql.replace(":term", "'%" + termino + "%'");
        }
        if (anio != null) {
            sql = sql.replace(":anio", anio.toString());
        }
        lastSql.set(sql);
    }

    @Override
    public List<EvidenceSearchDetailDto> executeSearch(String termino, String dimension, Integer anio, List<UUID> programScope) {
        StringBuilder jpql = new StringBuilder(
                "SELECT ev.id, ev.storageKey, ev.description, ev.criterionId, p.name, ev.createdAt " +
                "FROM EvidenceVersionEntity ev " +
                "JOIN EvidenceEntity e ON ev.evidenceId = e.id " +
                "JOIN IndicatorEntity ind ON e.indicatorId = ind.id " +
                "JOIN ProgramJpaEntity p ON ind.programId = p.id " +
                "WHERE ev.id = e.latestVersionId "
        );

        if (programScope != null) {
            if (programScope.isEmpty()) {
                return List.of();
            }
            jpql.append(" AND ind.programId IN :programScope");
        }

        if (dimension != null && !dimension.strip().isEmpty()) {
            List<UUID> matchingCriteria = getCriteriaForDimension(dimension);
            if (matchingCriteria.isEmpty()) {
                return List.of();
            }
            jpql.append(" AND ev.criterionId IN :matchingCriteria");
        }

        if (termino != null && !termino.strip().isEmpty()) {
            jpql.append(" AND (LOWER(ev.description) LIKE :term OR LOWER(ev.storageKey) LIKE :term)");
        }

        if (anio != null) {
            jpql.append(" AND YEAR(ev.createdAt) = :anio");
        }

        TypedQuery<Object[]> query = entityManager.createQuery(jpql.toString(), Object[].class);

        if (programScope != null && !programScope.isEmpty()) {
            query.setParameter("programScope", programScope);
        }
        if (dimension != null && !dimension.strip().isEmpty()) {
            query.setParameter("matchingCriteria", getCriteriaForDimension(dimension));
        }
        if (termino != null && !termino.strip().isEmpty()) {
            query.setParameter("term", "%" + termino.toLowerCase() + "%");
        }
        if (anio != null) {
            query.setParameter("anio", anio);
        }

        saveExecutedSql(jpql.toString(), termino, dimension, anio, programScope);
        List<Object[]> rows = query.getResultList();
        List<EvidenceSearchDetailDto> results = new ArrayList<>();

        for (Object[] row : rows) {
            UUID evId = (UUID) row[0];
            String storageKey = (String) row[1];
            String desc = (String) row[2];
            UUID critId = (UUID) row[3];
            String progName = (String) row[4];
            LocalDateTime created = (LocalDateTime) row[5];

            String criterionCode = getCriterionCode(critId);
            String dimensionName = getDimensionName(critId);

            results.add(new EvidenceSearchDetailDto(
                    evId,
                    storageKey,
                    desc,
                    dimensionName,
                    criterionCode,
                    progName,
                    created
            ));
        }

        return results;
    }

    private List<UUID> getCriteriaForDimension(String dimension) {
        try {
            return entityManager.createQuery(
                    "SELECT c.id FROM EvaluationCriterionEntity c " +
                    "JOIN EvaluationDimensionEntity d ON c.dimensionId = d.id " +
                    "WHERE LOWER(d.name) = LOWER(:dimName)", UUID.class)
                    .setParameter("dimName", dimension)
                    .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private String getCriterionCode(UUID critId) {
        try {
            return entityManager.createQuery(
                    "SELECT c.code FROM EvaluationCriterionEntity c WHERE c.id = :critId", String.class)
                    .setParameter("critId", critId)
                    .getSingleResult();
        } catch (Exception e) {
            return "CRT-01";
        }
    }

    private String getDimensionName(UUID critId) {
        try {
            return entityManager.createQuery(
                    "SELECT d.name FROM EvaluationDimensionEntity d " +
                    "JOIN EvaluationCriterionEntity c ON c.dimensionId = d.id " +
                    "WHERE c.id = :critId", String.class)
                    .setParameter("critId", critId)
                    .getSingleResult();
        } catch (Exception e) {
            return "Infraestructura";
        }
    }
}
