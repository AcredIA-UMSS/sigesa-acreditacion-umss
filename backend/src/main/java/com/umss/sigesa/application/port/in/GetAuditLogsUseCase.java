package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.AuditEvent;
import com.umss.sigesa.domain.model.AuditLogQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GetAuditLogsUseCase {

    Page<AuditEvent> getAuditLogs(AuditLogQuery query, Pageable pageable);

    List<AuditEvent> exportAuditLogs(AuditLogQuery query);

    Optional<AuditEvent> getAuditLog(UUID id);
}
