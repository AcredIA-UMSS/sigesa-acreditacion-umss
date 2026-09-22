package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.IndicatorObservationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataIndicatorObservationRepository extends JpaRepository<IndicatorObservationJpaEntity, UUID> {

    List<IndicatorObservationJpaEntity> findByIndicatorIdOrderByCreatedAtDesc(UUID indicatorId);

    Optional<IndicatorObservationJpaEntity> findFirstByIndicator_IdAndStatusOrderByCreatedAtDesc(
            UUID indicatorId,
            String status);
}
