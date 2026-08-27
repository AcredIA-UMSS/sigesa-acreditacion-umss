package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.EvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EvidenceJpaRepository extends JpaRepository<EvidenceEntity, UUID> {

    boolean existsByIndicatorId(UUID indicatorId);

    Optional<EvidenceEntity> findByIndicatorId(UUID indicatorId);

    List<EvidenceEntity> findBySubphaseIdOrderByCreatedAtDesc(UUID subphaseId);

    long countBySubphaseId(UUID subphaseId);

    @Query("""
            SELECT DISTINCT e.indicatorId FROM EvidenceEntity e
            WHERE e.subphaseId = :subphaseId AND e.indicatorId IS NOT NULL
            """)
    List<UUID> findDistinctIndicatorIdsBySubphaseId(@Param("subphaseId") UUID subphaseId);

    @Query("""
            SELECT DISTINCT e.subphaseId FROM EvidenceEntity e
            WHERE e.indicatorId = :indicatorId AND e.subphaseId IS NOT NULL
            """)
    List<UUID> findDistinctSubphaseIdsByIndicatorId(@Param("indicatorId") UUID indicatorId);

    boolean existsByIndicatorIdAndSubphaseIdIsNotNull(UUID indicatorId);
}
