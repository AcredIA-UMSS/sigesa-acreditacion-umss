package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.IndicatorAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataIndicatorAnalysisRepository extends JpaRepository<IndicatorAnalysisEntity, UUID> {

    Optional<IndicatorAnalysisEntity> findByIndicatorId(UUID indicatorId);

    @Query("""
            SELECT COUNT(a) FROM IndicatorAnalysisEntity a
            WHERE a.processId = :processId
              AND a.fodaQuadrant IS NOT NULL
              AND a.compliance IS NOT NULL
              AND a.synthesis IS NOT NULL
              AND TRIM(a.synthesis) <> ''
            """)
    long countCompleteByProcessId(@Param("processId") UUID processId);
}
