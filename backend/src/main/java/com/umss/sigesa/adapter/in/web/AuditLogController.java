package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.AuditLogResponse;
import com.umss.sigesa.application.port.in.GetAuditLogsUseCase;
import com.umss.sigesa.domain.exception.AuditEventNotFoundException;
import com.umss.sigesa.domain.exception.InvalidFilterException;
import com.umss.sigesa.domain.model.AuditAction;
import com.umss.sigesa.domain.model.AuditEvent;
import com.umss.sigesa.domain.model.AuditLogQuery;
import com.umss.sigesa.domain.model.AuditResourceType;
import com.umss.sigesa.domain.model.AuditResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit/logs")
@PreAuthorize("hasRole('JD')")
@Tag(name = "Audit", description = "Bitácora de auditoría (FSD-UC-017). Solo [JD]. Sin modificación ni borrado.")
public class AuditLogController {

    static final ZoneId DISPLAY_ZONE = ZoneId.of("America/La_Paz");

    private final GetAuditLogsUseCase getAuditLogsUseCase;

    public AuditLogController(GetAuditLogsUseCase getAuditLogsUseCase) {
        this.getAuditLogsUseCase = getAuditLogsUseCase;
    }

    @GetMapping
    @Operation(summary = "Consultar bitácora",
            description = "Filtros y paginación. Orden por fecha descendente. Tope de página 100. "
                    + "action acepta un valor, varios repetidos o una lista separada por comas. "
                    + "from posterior a to responde 422.")
    public ResponseEntity<Page<AuditLogResponse>> list(
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) String actorEmail,
            @RequestParam(required = false) List<String> action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) UUID resourceId,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @PageableDefault(size = 20, sort = "occurredAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AuditLogResponse> page = getAuditLogsUseCase.getAuditLogs(query(
                actorId, actorEmail, action, resourceType, resourceId, result, from, to), pageable)
                .map(event -> toResponse(event, false));
        return ResponseEntity.ok(page);
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @Operation(summary = "Exportar bitácora",
            description = "Mismos filtros que la consulta. Tope de 1000 filas. Sin payload.")
    public ResponseEntity<String> export(
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) String actorEmail,
            @RequestParam(required = false) List<String> action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) UUID resourceId,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        var rows = getAuditLogsUseCase.exportAuditLogs(query(
                actorId, actorEmail, action, resourceType, resourceId, result, from, to));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"historico.csv\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(AuditLogCsv.write(rows));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un evento de auditoría")
    public ResponseEntity<AuditLogResponse> get(@PathVariable UUID id) {
        AuditEvent event = getAuditLogsUseCase.getAuditLog(id)
                .orElseThrow(AuditEventNotFoundException::new);
        return ResponseEntity.ok(toResponse(event, true));
    }

    private static AuditLogResponse toResponse(AuditEvent event, boolean includePayload) {
        OffsetDateTime displayed = event.occurredAt() == null
                ? null
                : event.occurredAt().atZone(DISPLAY_ZONE).toOffsetDateTime();
        return new AuditLogResponse(
                event.id(),
                event.actorId(),
                event.actorEmail(),
                event.action().name(),
                event.resourceType().name(),
                event.resourceId(),
                event.description(),
                displayed,
                event.result().name(),
                event.ipAddress(),
                includePayload ? event.payload() : null
        );
    }

    private static AuditLogQuery query(UUID actorId,
                                        String actorEmail,
                                        List<String> action,
                                        String resourceType,
                                        UUID resourceId,
                                        String result,
                                        Instant from,
                                        Instant to) {
        return new AuditLogQuery(
                actorId,
                actorEmail,
                parseActions(action),
                parseEnum(AuditResourceType.class, resourceType),
                resourceId,
                parseEnum(AuditResult.class, result),
                from,
                to
        );
    }

    private static Set<AuditAction> parseActions(List<String> raw) {
        Set<AuditAction> actions = new LinkedHashSet<>();
        if (raw == null) {
            return actions;
        }
        for (String value : raw) {
            if (value == null || value.isBlank()) {
                continue;
            }
            for (String part : value.split(",")) {
                if (!part.isBlank()) {
                    actions.add(parseEnum(AuditAction.class, part));
                }
            }
        }
        return actions;
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(type, raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidFilterException("Valor de filtro inválido.");
        }
    }
}
