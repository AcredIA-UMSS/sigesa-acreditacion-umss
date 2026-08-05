package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataAccreditationProcessRepository extends JpaRepository<AccreditationProcessJpaEntity, UUID> {
    boolean existsByCareerIdAndStatus(UUID careerId, String status);

    long countByCareerId(UUID careerId);

    @Query("""
            SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
            FROM AccreditationProcessJpaEntity p
            JOIN TemplateJpaEntity t ON p.templateId = t.id
            WHERE p.careerId = :careerId
              AND p.status = 'ACTIVE'
              AND UPPER(t.type) = UPPER(:templateType)
            """)
    boolean existsActiveByCareerIdAndTemplateType(
            @Param("careerId") UUID careerId,
            @Param("templateType") String templateType);

    List<AccreditationProcessJpaEntity> findAllByOrderByStartDateDesc();

    List<AccreditationProcessJpaEntity> findByCareerIdInOrderByStartDateDesc(Collection<UUID> careerIds);

    @EntityGraph(attributePaths = {"phases"})
    Optional<AccreditationProcessJpaEntity> findWithPhasesById(UUID id);

    @Query("SELECT COUNT(p) FROM PhaseJpaEntity p WHERE p.process.id = :processId")
    long countPhasesByProcessId(@Param("processId") UUID processId);

    @Query("SELECT COUNT(s) FROM SubphaseJpaEntity s WHERE s.phase.process.id = :processId")
    long countSubphasesByProcessId(@Param("processId") UUID processId);
}