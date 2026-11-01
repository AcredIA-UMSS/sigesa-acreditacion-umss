package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.NotificationOutboxJpaEntity;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationOutboxJpaRepository extends JpaRepository<NotificationOutboxJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")})
    @Query("SELECT n FROM NotificationOutboxJpaEntity n " +
           "WHERE n.status = :status AND (n.nextRetryAt IS NULL OR n.nextRetryAt <= :now) " +
           "ORDER BY n.createdAt ASC")
    List<NotificationOutboxJpaEntity> findPendingBatch(
            @Param("status") String status,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    long countByStatus(String status);
}
