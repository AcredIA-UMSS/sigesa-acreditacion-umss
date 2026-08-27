package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.PhaseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPhaseRepository extends JpaRepository<PhaseJpaEntity, UUID> {

    List<PhaseJpaEntity> findByProcessIdOrderByOrderAsc(UUID processId);

    Optional<PhaseJpaEntity> findByIdAndProcessId(UUID id, UUID processId);

    @Query("""
            SELECT p FROM PhaseJpaEntity p
            JOIN FETCH p.process proc
            WHERE p.id = :phaseId AND proc.id = :processId
            """)
    Optional<PhaseJpaEntity> findWithProcessById(
            @Param("processId") UUID processId,
            @Param("phaseId") UUID phaseId);
}
