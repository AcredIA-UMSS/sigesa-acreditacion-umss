package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.IndicatorObservationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataIndicatorObservationRepository extends JpaRepository<IndicatorObservationJpaEntity, UUID> {

    List<IndicatorObservationJpaEntity> findByIndicatorIdOrderByCreatedAtDesc(UUID indicatorId);

    List<IndicatorObservationJpaEntity> findByIndicator_Id(UUID indicatorId);

    Optional<IndicatorObservationJpaEntity> findFirstByIndicator_IdAndStatusOrderByCreatedAtDesc(
            UUID indicatorId,
            String status);

    @Query("""
            SELECT o FROM IndicatorObservationJpaEntity o
            JOIN FETCH o.indicator i
            JOIN FETCH i.level3Node l3
            JOIN FETCH l3.level2Node l2
            JOIN FETCH l2.level1Node l1
            JOIN FETCH l1.process p
            WHERE o.status = 'OPEN'
              AND o.dueDate IS NOT NULL
              AND o.dueDate >= :from
              AND o.dueDate <= :until
            ORDER BY o.dueDate ASC
            """)
    List<IndicatorObservationJpaEntity> findApproachingDeadlines(
            @Param("from") LocalDate from,
            @Param("until") LocalDate until);
}
