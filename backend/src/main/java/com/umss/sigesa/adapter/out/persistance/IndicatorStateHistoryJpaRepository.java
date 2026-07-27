package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.IndicatorStateHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IndicatorStateHistoryJpaRepository extends JpaRepository<IndicatorStateHistoryEntity, UUID> {

    Optional<IndicatorStateHistoryEntity> findTopByIndicatorIdOrderByCreatedAtDesc(UUID indicatorId);
}
