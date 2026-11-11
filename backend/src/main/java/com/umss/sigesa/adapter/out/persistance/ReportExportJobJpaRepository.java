package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.ReportExportJobEntity;
import com.umss.sigesa.domain.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public interface ReportExportJobJpaRepository extends JpaRepository<ReportExportJobEntity, UUID> {
    long countByUserIdAndStatusIn(UUID userId, Collection<JobStatus> statuses);

    @Modifying
    @Query("UPDATE ReportExportJobEntity e SET e.status = :failedStatus, e.errorMessage = :reason, e.updatedAt = :now " +
           "WHERE e.status IN (:activeStatuses) AND e.updatedAt < :threshold")
    int markStaleJobsAsFailed(
            @Param("activeStatuses") Collection<JobStatus> activeStatuses,
            @Param("threshold") Instant threshold,
            @Param("failedStatus") JobStatus failedStatus,
            @Param("reason") String reason,
            @Param("now") Instant now);
}
