package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.AccessActionResponse;
import com.umss.sigesa.adapter.in.web.dto.AccessActionSummary;
import com.umss.sigesa.adapter.in.web.dto.AccessSessionResponse;
import com.umss.sigesa.adapter.in.web.dto.AccessSessionSummaryResponse;
import com.umss.sigesa.adapter.out.persistance.AuditLogJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.AuditLogJpaEntity;
import com.umss.sigesa.application.port.out.AccessSessionPort;
import com.umss.sigesa.domain.model.access.AccessSession;
import com.umss.sigesa.domain.model.access.AccessSessionStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/access-sessions")
@PreAuthorize("hasRole('JD')")
@Tag(name = "Access sessions", description = "Historial de accesos. Solo consulta. [JD]")
public class AccessSessionController {

    private final AccessSessionPort accessSessionPort;
    private final AuditLogJpaRepository auditLogRepository;

    public AccessSessionController(AccessSessionPort accessSessionPort, AuditLogJpaRepository auditLogRepository) {
        this.accessSessionPort = accessSessionPort;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/summary")
    @Operation(summary = "Totales del historial de accesos")
    public AccessSessionSummaryResponse summary() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return new AccessSessionSummaryResponse(
                accessSessionPort.countAll(),
                accessSessionPort.countDistinctActiveUsers(),
                accessSessionPort.countStartedSince(startOfDay),
                accessSessionPort.countByStatus(AccessSessionStatus.FAILED));
    }

    @GetMapping
    @Operation(summary = "Consultar ingresos con filtros y paginación")
    public Page<AccessSessionResponse> list(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) AccessSessionStatus status,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AccessSession> found =
                accessSessionPort.search(email, role, status, ipAddress, from, to, pageable);
        List<UUID> sessionIds = found.getContent().stream().map(AccessSession::id).toList();
        Map<UUID, List<String>> actions = sessionIds.isEmpty()
                ? Map.of()
                : AccessActionSummary.bySession(auditLogRepository.findBySessionIdInOrderByCreatedAtAsc(sessionIds));
        return found.map(session -> AccessSessionResponse.from(session, actions.getOrDefault(session.id(), List.of())));
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Detalle de un ingreso")
    public AccessSessionResponse detail(@PathVariable UUID sessionId) {
        return accessSessionPort.findById(sessionId)
                .map(AccessSessionResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingreso no encontrado"));
    }

    @GetMapping("/{sessionId}/actions")
    @Operation(summary = "Acciones de la bitácora asociadas a la sesión")
    public List<AccessActionResponse> actions(@PathVariable UUID sessionId) {
        accessSessionPort.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingreso no encontrado"));
        return auditLogRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(this::toAction)
                .toList();
    }

    private AccessActionResponse toAction(AuditLogJpaEntity entry) {
        String result = "LOGIN_FAILED".equals(entry.getAction()) || "DELETE_DENIED".equals(entry.getAction())
                || (entry.getPayloadDiff() != null && entry.getPayloadDiff().startsWith("FALLIDO"))
                ? "FALLIDO"
                : "EXITOSO";
        return new AccessActionResponse(
                entry.getId().toString(),
                entry.getCreatedAt(),
                entry.getEntityType(),
                entry.getAction(),
                entry.getPayloadDiff(),
                result,
                entry.getActorEmail(),
                entry.getActorRole());
    }
}
