package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.UserProgramAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface UserProgramAssignmentJpaRepository extends JpaRepository<UserProgramAssignmentEntity, UUID> {

    List<UserProgramAssignmentEntity> findByUserIdAndRevokedAtIsNull(UUID userId);

    boolean existsByUserIdAndProgramIdAndRevokedAtIsNull(UUID userId, UUID programId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE UserProgramAssignmentEntity a SET a.revokedAt = :revokedAt WHERE a.userId = :userId AND a.revokedAt IS NULL")
    int revokeAllActiveByUserId(@Param("userId") UUID userId, @Param("revokedAt") LocalDateTime revokedAt);
}
