package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.ObservationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.stream.Stream;

public interface ObservationJpaRepository extends JpaRepository<ObservationEntity, String> {

    @Query("SELECT o FROM ObservationEntity o WHERE o.programId = :programId " +
           "AND (:phaseId IS NULL OR o.phaseId = :phaseId) " +
           "AND (:status IS NULL OR o.status = :status)")
    Page<ObservationEntity> findByProgramIdAndFilters(
            @Param("programId") UUID programId,
            @Param("phaseId") Integer phaseId,
            @Param("status") String status,
            Pageable pageable);

    @Query("SELECT o FROM ObservationEntity o WHERE o.programId = :programId " +
           "AND (:phaseId IS NULL OR o.phaseId = :phaseId)")
    Stream<ObservationEntity> streamByProgramIdAndPhaseId(
            @Param("programId") UUID programId,
            @Param("phaseId") Integer phaseId);
}
