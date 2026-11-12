package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.access.AccessSession;
import com.umss.sigesa.domain.model.access.AccessSessionEndReason;
import com.umss.sigesa.domain.model.access.AccessSessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AccessSessionPort {

    UUID openSuccessful(UUID userId, String fullName, String email, String role);

    void recordFailure(String email);

    void close(UUID sessionId, AccessSessionEndReason reason);

    void revokeActiveByUser(UUID userId);

    Optional<AccessSession> findById(UUID sessionId);

    Page<AccessSession> search(String email, String role, AccessSessionStatus status, String ipAddress,
                               LocalDateTime from, LocalDateTime to, Pageable pageable);

    long countAll();

    long countByStatus(AccessSessionStatus status);

    long countStartedSince(LocalDateTime from);

    long countDistinctActiveUsers();
}
