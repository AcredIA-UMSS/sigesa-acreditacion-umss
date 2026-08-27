package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.SubphaseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataSubphaseRepository extends JpaRepository<SubphaseJpaEntity, UUID> {

    List<SubphaseJpaEntity> findByPhaseIdOrderByOrderAsc(UUID phaseId);

    Optional<SubphaseJpaEntity> findByIdAndPhaseId(UUID id, UUID phaseId);

    @Query("""
            SELECT s FROM SubphaseJpaEntity s
            JOIN FETCH s.phase p
            JOIN FETCH p.process proc
            WHERE s.id = :subphaseId
            """)
    Optional<SubphaseJpaEntity> findWithProcessById(@Param("subphaseId") UUID subphaseId);
}
