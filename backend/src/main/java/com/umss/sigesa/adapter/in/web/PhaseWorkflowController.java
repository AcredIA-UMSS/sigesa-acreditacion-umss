package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.PhaseCompleteResponseDto;
import com.umss.sigesa.adapter.in.web.dto.PhaseProgressResponseDto;
import com.umss.sigesa.application.port.in.ClosePhaseUseCase;
import com.umss.sigesa.application.port.in.GetPhaseProgressUseCase;
import com.umss.sigesa.domain.model.PhaseCompleteResult;
import com.umss.sigesa.domain.model.PhaseProgressResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/processes/{processId}/phases/{phaseId}")
@Tag(name = "Phase workflow", description = "Cierre de fase (FSD-UC-010 / API-WF-03)")
public class PhaseWorkflowController {

    private final ClosePhaseUseCase closePhaseUseCase;
    private final GetPhaseProgressUseCase getPhaseProgressUseCase;

    public PhaseWorkflowController(ClosePhaseUseCase closePhaseUseCase,
                                   GetPhaseProgressUseCase getPhaseProgressUseCase) {
        this.closePhaseUseCase = closePhaseUseCase;
        this.getPhaseProgressUseCase = getPhaseProgressUseCase;
    }

    @GetMapping("/progress")
    @PreAuthorize("hasAnyRole('JD','TD','CC')")
    @Operation(summary = "Consultar progreso de fase según sus subfases")
    public ResponseEntity<PhaseProgressResponseDto> getPhaseProgress(
            @PathVariable UUID processId,
            @PathVariable UUID phaseId) {
        return ResponseEntity.ok(toProgressDto(getPhaseProgressUseCase.getProgress(processId, phaseId)));
    }

    @PostMapping("/complete")
    @PreAuthorize("hasRole('TD')")
    @Operation(summary = "Cerrar fase cuando todas las subfases están APROBADO")
    public ResponseEntity<PhaseCompleteResponseDto> completePhase(
            @PathVariable UUID processId,
            @PathVariable UUID phaseId,
            Authentication authentication) {
        UUID actorId = (UUID) authentication.getPrincipal();
        PhaseCompleteResult result = closePhaseUseCase.close(
                processId,
                phaseId,
                actorId,
                primaryRole(authentication));
        return ResponseEntity.ok(toDto(result));
    }

    private static PhaseCompleteResponseDto toDto(PhaseCompleteResult result) {
        PhaseCompleteResponseDto dto = new PhaseCompleteResponseDto();
        dto.setPhaseId(result.phaseId());
        dto.setPreviousState(result.previousState().name());
        dto.setNewState(result.newState().name());
        dto.setEvent(result.event());
        return dto;
    }

    private static PhaseProgressResponseDto toProgressDto(PhaseProgressResult result) {
        PhaseProgressResponseDto dto = new PhaseProgressResponseDto();
        dto.setPhaseId(result.phaseId());
        dto.setTotalSubphases(result.totalSubphases());
        dto.setApprovedSubphases(result.approvedSubphases());
        dto.setPendingSubphases(result.pendingSubphases());
        dto.setProgressPercent(result.progressPercent());
        dto.setState(result.state().name());
        return dto;
    }

    private static String primaryRole(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.startsWith("ROLE_") ? auth.substring(5) : auth)
                .toList();
        return roles.isEmpty() ? "" : roles.getFirst();
    }
}
