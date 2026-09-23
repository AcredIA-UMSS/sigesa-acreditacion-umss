package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.IndicatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IndicatorJpaRepository extends JpaRepository<IndicatorEntity, UUID> {

    long countByProgramId(UUID programId);

    List<IndicatorEntity> findByProgramId(UUID programId);

    List<IndicatorEntity> findByProgramIdIn(List<UUID> programIds);
}
