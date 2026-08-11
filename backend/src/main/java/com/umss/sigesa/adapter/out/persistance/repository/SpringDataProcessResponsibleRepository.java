package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.ProcessResponsibleAssignmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataProcessResponsibleRepository extends JpaRepository<ProcessResponsibleAssignmentJpaEntity, UUID> {

    Optional<ProcessResponsibleAssignmentJpaEntity> findByProcessIdAndRevokedAtIsNull(UUID processId);

    Optional<ProcessResponsibleAssignmentJpaEntity> findByUserIdAndRevokedAtIsNull(UUID userId);

    List<ProcessResponsibleAssignmentJpaEntity> findByRevokedAtIsNull();

    @Modifying
    @Query("""
            UPDATE ProcessResponsibleAssignmentJpaEntity e
            SET e.revokedAt = :revokedAt
            WHERE e.processId = :processId AND e.revokedAt IS NULL
            """)
    int revokeActiveByProcessId(@Param("processId") UUID processId,
                                @Param("revokedAt") LocalDateTime revokedAt);
}
