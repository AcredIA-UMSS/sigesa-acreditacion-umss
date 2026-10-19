package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.EvidenceEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceVersionEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorObservationJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level1NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level2NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level3NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.NormativeIndicatorJpaEntity;
import com.umss.sigesa.application.model.dashboard.TechnicianInboxItem;
import com.umss.sigesa.application.port.out.TechnicianInboxQueryPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class TechnicianInboxJpaAdapter implements TechnicianInboxQueryPort {

    private final EntityManager entityManager;
    private final com.umss.sigesa.application.port.out.ProgramCatalogPort programCatalogPort;

    public TechnicianInboxJpaAdapter(EntityManager entityManager,
                                     com.umss.sigesa.application.port.out.ProgramCatalogPort programCatalogPort) {
        this.entityManager = entityManager;
        this.programCatalogPort = programCatalogPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TechnicianInboxItem> findInboxItems(UUID programId, UUID level1Id, String status, Pageable pageable) {
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        if (programId != null) {
            where.append(" AND proc.careerId = :programId ");
        }
        if (level1Id != null) {
            where.append(" AND l1.id = :level1Id ");
        }
        if (status != null && !status.isBlank()) {
            where.append(" AND i.status = :status ");
        } else {
            where.append(" AND i.status IN ('SUBIDO', 'SUBSANADO', 'OBSERVADO') ");
        }

        String countJpql = "SELECT COUNT(i) FROM NormativeIndicatorJpaEntity i " +
                "JOIN i.level3Node l3 " +
                "JOIN l3.level2Node l2 " +
                "JOIN l2.level1Node l1 " +
                "JOIN l1.process proc " + where;

        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        bindParameters(countQuery, programId, level1Id, status);
        Long total = countQuery.getSingleResult();

        if (total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        String selectJpql = "SELECT i, l3, l2, l1, proc.careerId FROM NormativeIndicatorJpaEntity i " +
                "JOIN i.level3Node l3 " +
                "JOIN l3.level2Node l2 " +
                "JOIN l2.level1Node l1 " +
                "JOIN l1.process proc " + where +
                " ORDER BY i.updatedAt DESC";

        TypedQuery<Object[]> selectQuery = entityManager.createQuery(selectJpql, Object[].class);
        bindParameters(selectQuery, programId, level1Id, status);
        if (pageable.isPaged()) {
            selectQuery.setFirstResult((int) pageable.getOffset());
            selectQuery.setMaxResults(pageable.getPageSize());
        }

        List<Object[]> rows = selectQuery.getResultList();
        List<TechnicianInboxItem> items = new ArrayList<>(rows.size());

        for (Object[] row : rows) {
            NormativeIndicatorJpaEntity ind = (NormativeIndicatorJpaEntity) row[0];
            Level3NodeJpaEntity l3 = (Level3NodeJpaEntity) row[1];
            Level2NodeJpaEntity l2 = (Level2NodeJpaEntity) row[2];
            Level1NodeJpaEntity l1 = (Level1NodeJpaEntity) row[3];
            UUID careerId = (UUID) row[4];

            long openObsCount = countOpenObservations(ind.getId());
            EvidenceInfo evidenceInfo = findLatestEvidence(ind.getId());

            String pathHierarchy = String.format("%s > %s > %s",
                    l1.getName(), l2.getName(), l3.getName());

            String resolvedProgramName = "N/A";
            if (careerId != null) {
                resolvedProgramName = programCatalogPort.findById(careerId)
                        .map(p -> p.name() != null && !p.name().isBlank() ? p.name() : p.code())
                        .orElse("Carrera " + careerId);
            }

            items.add(TechnicianInboxItem.builder()
                    .indicatorId(ind.getId())
                    .indicatorCode(ind.getCode())
                    .indicatorDescription(ind.getDescription())
                    .status(ind.getStatus())
                    .dimensionName(l1.getName())
                    .criterionName(l2.getName())
                    .subcriterionName(l3.getName())
                    .pathHierarchy(pathHierarchy)
                    .programId(careerId)
                    .programName(resolvedProgramName)
                    .evidenceId(evidenceInfo != null ? evidenceInfo.evidenceId : null)
                    .evidenceFilename(evidenceInfo != null ? evidenceInfo.filename : null)
                    .evidenceUploadedAt(evidenceInfo != null ? evidenceInfo.uploadedAt : null)
                    .activeObservationsCount(openObsCount)
                    .updatedAt(ind.getUpdatedAt())
                    .build());
        }

        return new PageImpl<>(items, pageable, total);
    }

    private void bindParameters(TypedQuery<?> query, UUID programId, UUID level1Id, String status) {
        if (programId != null) {
            query.setParameter("programId", programId);
        }
        if (level1Id != null) {
            query.setParameter("level1Id", level1Id);
        }
        if (status != null && !status.isBlank()) {
            query.setParameter("status", status.trim().toUpperCase());
        }
    }

    private long countOpenObservations(UUID indicatorId) {
        String jpql = "SELECT COUNT(o) FROM IndicatorObservationJpaEntity o WHERE o.indicator.id = :indId AND o.status = 'OPEN'";
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("indId", indicatorId)
                .getSingleResult();
    }

    private EvidenceInfo findLatestEvidence(UUID indicatorId) {
        String jpql = "SELECT e.id, v.storageKey, v.createdAt FROM EvidenceEntity e, EvidenceVersionEntity v " +
                "WHERE e.normativeIndicatorId = :indId AND v.id = e.latestVersionId";
        List<Object[]> results = entityManager.createQuery(jpql, Object[].class)
                .setParameter("indId", indicatorId)
                .setMaxResults(1)
                .getResultList();

        if (results.isEmpty()) {
            return null;
        }
        Object[] r = results.get(0);
        return new EvidenceInfo((UUID) r[0], (String) r[1], (java.time.LocalDateTime) r[2]);
    }

    private record EvidenceInfo(UUID evidenceId, String filename, java.time.LocalDateTime uploadedAt) {
    }
}
