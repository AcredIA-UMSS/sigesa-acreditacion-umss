package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.AuditEvent;
import com.umss.sigesa.domain.model.AuditLogQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AuditLogQueryPort {

    Page<AuditEvent> find(AuditLogQuery query, Pageable pageable);

    Optional<AuditEvent> findById(UUID id);
}
