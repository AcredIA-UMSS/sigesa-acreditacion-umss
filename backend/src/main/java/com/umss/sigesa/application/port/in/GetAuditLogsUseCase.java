package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.AuditLogEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.OutputStream;
import java.time.LocalDateTime;

public interface GetAuditLogsUseCase {

    Page<AuditLogEntry> getAuditLogs(String actorEmail, String action, String entityType,
                                     LocalDateTime from, LocalDateTime to, Pageable pageable);

    void streamCsvExport(String actorEmail, String action, String entityType,
                         LocalDateTime from, LocalDateTime to, OutputStream outputStream);
}
