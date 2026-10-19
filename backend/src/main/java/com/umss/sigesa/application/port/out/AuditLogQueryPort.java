package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.AuditLogEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.OutputStream;
import java.time.LocalDateTime;

public interface AuditLogQueryPort {

    Page<AuditLogEntry> findAuditLogs(String actorEmail, String action, String entityType,
                                      LocalDateTime from, LocalDateTime to, Pageable pageable);

    void streamAuditLogsCsv(String actorEmail, String action, String entityType,
                           LocalDateTime from, LocalDateTime to, OutputStream outputStream);
}
