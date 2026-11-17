package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
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

    interface ProcessSummaryProjection {
        UUID getId();
        UUID getCareerId();
        UUID getTemplateId();
        String getStatus();
        java.time.LocalDateTime getStartDate();
    }

    @Query("""
            SELECT p.id AS id, p.careerId AS careerId, p.templateId AS templateId, p.status AS status, p.startDate AS startDate
            FROM AccreditationProcessJpaEntity p
            WHERE p.status <> 'ARCHIVED'
            ORDER BY p.startDate DESC
            """)
    List<ProcessSummaryProjection> findAllSummaryProjections();

    @Query("""
            SELECT p.id AS id, p.careerId AS careerId, p.templateId AS templateId, p.status AS status, p.startDate AS startDate
            FROM AccreditationProcessJpaEntity p
            WHERE p.careerId IN :careerIds
              AND p.status <> 'ARCHIVED'
            ORDER BY p.startDate DESC
            """)
    List<ProcessSummaryProjection> findSummaryProjectionsByCareerIds(@Param("careerIds") Collection<UUID> careerIds);

    List<AccreditationProcessJpaEntity> findAllByOrderByStartDateDesc();

    List<AccreditationProcessJpaEntity> findByCareerIdInOrderByStartDateDesc(Collection<UUID> careerIds);

    boolean existsByTemplateId(UUID templateId);
}
