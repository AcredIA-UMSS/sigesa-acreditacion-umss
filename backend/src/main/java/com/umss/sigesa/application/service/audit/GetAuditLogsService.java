package com.umss.sigesa.application.service.audit;

import com.umss.sigesa.application.port.in.GetAuditLogsUseCase;
import com.umss.sigesa.application.port.out.AuditLogQueryPort;
import com.umss.sigesa.domain.model.AuditLogEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.OutputStream;
import java.time.LocalDateTime;

public class GetAuditLogsService implements GetAuditLogsUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final AuditLogQueryPort queryPort;

    public GetAuditLogsService(AuditLogQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    public Page<AuditLogEntry> getAuditLogs(String actorEmail, String action, String entityType,
                                           LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Pageable effectivePageable = pageable;
        if (pageable.isPaged() && pageable.getPageSize() > MAX_PAGE_SIZE) {
            effectivePageable = PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort());
        }
        return queryPort.findAuditLogs(actorEmail, action, entityType, from, to, effectivePageable);
    }

    @Override
    public void streamCsvExport(String actorEmail, String action, String entityType,
                                LocalDateTime from, LocalDateTime to, OutputStream outputStream) {
        queryPort.streamAuditLogsCsv(actorEmail, action, entityType, from, to, outputStream);
    }
}
