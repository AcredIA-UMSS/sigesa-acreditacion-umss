package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.AccessSessionJpaEntity;
import com.umss.sigesa.domain.model.access.AccessSessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AccessSessionJpaRepository extends JpaRepository<AccessSessionJpaEntity, UUID> {

    @Query("""
            SELECT s FROM AccessSessionJpaEntity s
            WHERE (:email IS NULL OR LOWER(s.email) LIKE LOWER(CONCAT('%', :email, '%')))
              AND (:role IS NULL OR s.role = :role)
              AND (:status IS NULL OR s.status = :status)
              AND (:ipAddress IS NULL OR s.ipAddress LIKE CONCAT('%', :ipAddress, '%'))
              AND (:from IS NULL OR s.startedAt >= :from)
              AND (:to IS NULL OR s.startedAt <= :to)
            """)
    Page<AccessSessionJpaEntity> search(
            @Param("email") String email,
            @Param("role") String role,
            @Param("status") AccessSessionStatus status,
            @Param("ipAddress") String ipAddress,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    long countByStatus(AccessSessionStatus status);

    long countByStartedAtGreaterThanEqual(LocalDateTime from);

    @Query("SELECT COUNT(DISTINCT s.userId) FROM AccessSessionJpaEntity s WHERE s.status = :status AND s.userId IS NOT NULL")
    long countDistinctUsersByStatus(@Param("status") AccessSessionStatus status);

    java.util.List<AccessSessionJpaEntity> findByUserIdAndStatus(UUID userId, AccessSessionStatus status);
}
