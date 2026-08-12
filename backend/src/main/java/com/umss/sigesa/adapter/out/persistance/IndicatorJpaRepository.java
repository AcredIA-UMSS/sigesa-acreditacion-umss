package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.IndicatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface IndicatorJpaRepository extends JpaRepository<IndicatorEntity, UUID> {

    long countByProgramId(UUID programId);

    List<IndicatorEntity> findByProgramId(UUID programId);

    @Query("SELECT i FROM IndicatorEntity i JOIN PhaseJpaEntity p ON i.phaseId = p.id WHERE i.programId = :programId AND p.order = :phaseOrder")
    List<IndicatorEntity> findByProgramIdAndPhaseOrder(UUID programId, Integer phaseOrder);
}
