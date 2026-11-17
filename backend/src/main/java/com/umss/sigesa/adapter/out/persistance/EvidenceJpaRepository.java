package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.EvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EvidenceJpaRepository extends JpaRepository<EvidenceEntity, UUID> {

    boolean existsByIndicatorId(UUID indicatorId);

    Optional<EvidenceEntity> findByIndicatorId(UUID indicatorId);

    List<EvidenceEntity> findByNormativeIndicatorIdOrderByCreatedAtDesc(UUID normativeIndicatorId);

    long countByNormativeIndicatorId(UUID normativeIndicatorId);

    @Query("""
            SELECT e.normativeIndicatorId, COUNT(e)
            FROM EvidenceEntity e
            WHERE e.normativeIndicatorId IN :ids
            GROUP BY e.normativeIndicatorId
            """)
    List<Object[]> countGroupedByNormativeIndicatorId(@Param("ids") Collection<UUID> ids);
}
