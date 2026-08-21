package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.application.port.in.ApproveIndicatorUseCase;
import com.umss.sigesa.application.port.in.RejectIndicatorUseCase;
import com.umss.sigesa.application.port.out.EvidenceControlQueryPort;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/indicators")
@PreAuthorize("hasAnyRole('JD', 'TD')")
@Tag(name = "Indicator Workflow", description = "Endpoints de aprobación y rechazo de indicadores por el TD (FSD-UC-008, FSD-UC-009)")
public class IndicatorWorkflowController {

    private final ApproveIndicatorUseCase approveIndicatorUseCase;
    private final RejectIndicatorUseCase rejectIndicatorUseCase;
    private final EvidenceControlQueryPort evidenceControlQueryPort;

    public IndicatorWorkflowController(ApproveIndicatorUseCase approveIndicatorUseCase,
                                       RejectIndicatorUseCase rejectIndicatorUseCase,
                                       EvidenceControlQueryPort evidenceControlQueryPort) {
        this.approveIndicatorUseCase = approveIndicatorUseCase;
        this.rejectIndicatorUseCase = rejectIndicatorUseCase;
        this.evidenceControlQueryPort = evidenceControlQueryPort;
    }

    @GetMapping("/pending")
    @Operation(summary = "Listar indicadores pendientes de revisión", description = "Rol TD. Retorna los indicadores en estado SUBIDO o SUBSANADO.")
    public ResponseEntity<List<PendingIndicatorResponseDto>> listPending() {
        var items = evidenceControlQueryPort.listByProgramIdsAndStates(null, Set.of(IndicatorState.SUBIDO, IndicatorState.SUBSANADO, IndicatorState.PENDIENTE));
        var body = items.stream().map(item -> new PendingIndicatorResponseDto(
                item.indicatorId(),
                item.programId(),
                item.criterionId(),
                item.currentState().name(),
                item.evidenceId(),
                item.versionNumber(),
                item.contentHash(),
                item.description()
        )).toList();
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{indicatorId}/approve")
    @Operation(summary = "Aprobar indicador", description = "Rol TD. Transiciona a APROBADO y resuelve observaciones.")
    public ResponseEntity<ApproveResponseDto> approve(
            @PathVariable UUID indicatorId,
            Authentication authentication) {
        UUID actorId = extractActorId(authentication);
        String roleStr = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        Role actorRole = Role.valueOf(roleStr);

        var result = approveIndicatorUseCase.approve(indicatorId, actorId, actorRole);
        return ResponseEntity.ok(new ApproveResponseDto(
                result.newState().name(),
                result.stateHistoryId(),
                result.event()
        ));
    }

    @PostMapping("/{indicatorId}/reject")
    @Operation(summary = "Rechazar indicador", description = "Rol TD. Transiciona a OBSERVADO y crea observación.")
    public ResponseEntity<RejectResponseDto> reject(
            @PathVariable UUID indicatorId,
            @RequestBody RejectRequestDto request,
            Authentication authentication) {
        UUID actorId = extractActorId(authentication);
        String roleStr = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        Role actorRole = Role.valueOf(roleStr);

        var result = rejectIndicatorUseCase.reject(indicatorId, request.justification(), actorId, actorRole);
        return ResponseEntity.ok(new RejectResponseDto(
                result.newState().name(),
                result.observationId(),
                result.stateHistoryId()
        ));
    }

    private UUID extractActorId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UUID uuid) {
            return uuid;
        }
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            try {
                return UUID.fromString(userDetails.getUsername());
            } catch (Exception e) {
                return UUID.nameUUIDFromBytes(userDetails.getUsername().getBytes());
            }
        }
        if (principal instanceof String str) {
            try {
                return UUID.fromString(str);
            } catch (Exception e) {
                return UUID.nameUUIDFromBytes(str.getBytes());
            }
        }
        return UUID.randomUUID();
    }

    public record RejectRequestDto(String justification) {}
    public record ApproveResponseDto(String newState, UUID stateHistoryId, String event) {}
    public record RejectResponseDto(String newState, String observationId, UUID stateHistoryId) {}
    public record PendingIndicatorResponseDto(
            UUID indicatorId,
            UUID programId,
            UUID criterionId,
            String currentState,
            UUID evidenceId,
            Integer versionNumber,
            String contentHash,
            String description
    ) {}
}
