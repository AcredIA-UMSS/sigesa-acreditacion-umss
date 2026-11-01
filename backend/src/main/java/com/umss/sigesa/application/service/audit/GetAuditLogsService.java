package com.umss.sigesa.application.service.audit;

import com.umss.sigesa.application.port.in.GetAuditLogsUseCase;
import com.umss.sigesa.application.port.out.AuditLogQueryPort;
import com.umss.sigesa.domain.exception.InvalidFilterException;
import com.umss.sigesa.domain.model.AuditEvent;
import com.umss.sigesa.domain.model.AuditLogQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GetAuditLogsService implements GetAuditLogsUseCase {

    public static final int MAX_PAGE_SIZE = 100;
    public static final int MAX_EXPORT_ROWS = 1000;

    private final AuditLogQueryPort queryPort;

    public GetAuditLogsService(AuditLogQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    public Page<AuditEvent> getAuditLogs(AuditLogQuery query, Pageable pageable) {
        if (query.from() != null && query.to() != null && query.from().isAfter(query.to())) {
            throw new InvalidFilterException("El rango de fechas es inválido.");
        }
        return queryPort.find(query, capped(pageable));
    }

    @Override
    public List<AuditEvent> exportAuditLogs(AuditLogQuery query) {
        List<AuditEvent> rows = new ArrayList<>();
        int page = 0;
        while (rows.size() < MAX_EXPORT_ROWS) {
            int size = Math.min(MAX_PAGE_SIZE, MAX_EXPORT_ROWS - rows.size());
            var slice = getAuditLogs(query, PageRequest.of(page, size));
            rows.addAll(slice.getContent());
            if (!slice.hasNext() || slice.getContent().isEmpty()) {
                break;
            }
            page++;
        }
        return rows;
    }

    @Override
    public Optional<AuditEvent> getAuditLog(UUID id) {
        return queryPort.findById(id);
    }

    private static Pageable capped(Pageable pageable) {
        int page = pageable.isUnpaged() ? 0 : pageable.getPageNumber();
        int requested = pageable.isUnpaged() ? MAX_PAGE_SIZE : pageable.getPageSize();
        int size = Math.min(Math.max(requested, 1), MAX_PAGE_SIZE);
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt"));
    }
}
