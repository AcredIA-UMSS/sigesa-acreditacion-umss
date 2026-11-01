package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.AuditLogResponseDto;
import com.umss.sigesa.application.port.in.GetAuditLogsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/audit/logs")
@Tag(name = "Audit Log", description = "Bitácora inmutable de auditoría append-only para acreditación (FSD-UC-017)")
public class AuditLogController {

    private final GetAuditLogsUseCase getAuditLogsUseCase;

    public AuditLogController(GetAuditLogsUseCase getAuditLogsUseCase) {
        this.getAuditLogsUseCase = getAuditLogsUseCase;
    }

    @GetMapping
    @PreAuthorize("hasRole('JD')")
    @Operation(summary = "Consultar bitácora de auditoría inmutable",
               description = "Acceso exclusivo para rol [JD]. Retorna listado paginado con filtros.")
    public ResponseEntity<Page<AuditLogResponseDto>> getAuditLogs(
            @RequestParam(required = false) String actorEmail,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AuditLogResponseDto> result = getAuditLogsUseCase
                .getAuditLogs(actorEmail, action, entityType, from, to, pageable)
                .map(AuditLogResponseDto::from);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('JD')")
    @Operation(summary = "Exportar bitácora de auditoría a CSV por streaming",
               description = "Transmite registros de auditoría en formato CSV sin saturar memoria heap.")
    public ResponseEntity<StreamingResponseBody> exportAuditLogs(
            @RequestParam(required = false) String actorEmail,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        StreamingResponseBody responseBody = outputStream ->
                getAuditLogsUseCase.streamCsvExport(actorEmail, action, entityType, from, to, outputStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit_logs.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(responseBody);
    }
}
