package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.AuditLogJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogJpaRepository extends JpaRepository<AuditLogJpaEntity, UUID> {

    @Query("SELECT a FROM AuditLogJpaEntity a " +
           "WHERE (:actorEmail IS NULL OR a.actorEmail = :actorEmail) " +
           "  AND (:action IS NULL OR a.action = :action) " +
           "  AND (:entityType IS NULL OR a.entityType = :entityType) " +
           "  AND (:from IS NULL OR a.createdAt >= :from) " +
           "  AND (:to IS NULL OR a.createdAt <= :to) " +
           "ORDER BY a.createdAt DESC")
    Page<AuditLogJpaEntity> findFiltered(
            @Param("actorEmail") String actorEmail,
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    @Query("SELECT a FROM AuditLogJpaEntity a " +
           "WHERE (:actorEmail IS NULL OR a.actorEmail = :actorEmail) " +
           "  AND (:action IS NULL OR a.action = :action) " +
           "  AND (:entityType IS NULL OR a.entityType = :entityType) " +
           "  AND (:from IS NULL OR a.createdAt >= :from) " +
           "  AND (:to IS NULL OR a.createdAt <= :to) " +
           "ORDER BY a.createdAt DESC")
    List<AuditLogJpaEntity> findAllFiltered(
            @Param("actorEmail") String actorEmail,
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
