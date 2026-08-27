package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.IndicatorWorkflowResponseDto;
import com.umss.sigesa.adapter.in.web.dto.RejectIndicatorRequestDto;
import com.umss.sigesa.application.port.in.ApproveIndicatorUseCase;
import com.umss.sigesa.application.port.in.RejectIndicatorUseCase;
import com.umss.sigesa.domain.model.IndicatorWorkflowResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/indicators/{indicatorId}")
@Tag(name = "Indicator workflow", description = "Aprobación y rechazo (FSD-UC-008/009)")
public class IndicatorWorkflowController {

    private final RejectIndicatorUseCase rejectIndicatorUseCase;
    private final ApproveIndicatorUseCase approveIndicatorUseCase;

    public IndicatorWorkflowController(RejectIndicatorUseCase rejectIndicatorUseCase,
                                         ApproveIndicatorUseCase approveIndicatorUseCase) {
        this.rejectIndicatorUseCase = rejectIndicatorUseCase;
        this.approveIndicatorUseCase = approveIndicatorUseCase;
    }

    @PostMapping("/reject")
    @PreAuthorize("hasRole('TD')")
    @Operation(summary = "Rechazar indicador (requiere evidencia cargada)")
    public ResponseEntity<IndicatorWorkflowResponseDto> reject(
            @PathVariable UUID indicatorId,
            @Valid @RequestBody RejectIndicatorRequestDto request,
            Authentication authentication) {
        UUID actorId = (UUID) authentication.getPrincipal();
        IndicatorWorkflowResult result = rejectIndicatorUseCase.reject(
                indicatorId,
                request.getJustification(),
                actorId,
                primaryRole(authentication));
        return ResponseEntity.ok(toDto(result, "IndicatorRejected"));
    }

    @PostMapping("/approve")
    @PreAuthorize("hasRole('TD')")
    @Operation(summary = "Aprobar indicador (requiere evidencia cargada)")
    public ResponseEntity<IndicatorWorkflowResponseDto> approve(
            @PathVariable UUID indicatorId,
            Authentication authentication) {
        UUID actorId = (UUID) authentication.getPrincipal();
        IndicatorWorkflowResult result = approveIndicatorUseCase.approve(
                indicatorId, actorId, primaryRole(authentication));
        return ResponseEntity.ok(toDto(result, "IndicatorApproved"));
    }

    private static IndicatorWorkflowResponseDto toDto(IndicatorWorkflowResult result, String event) {
        IndicatorWorkflowResponseDto dto = new IndicatorWorkflowResponseDto();
        dto.setIndicatorId(result.indicatorId());
        dto.setPreviousState(result.previousState().name());
        dto.setNewState(result.newState().name());
        dto.setStateHistoryId(result.stateHistoryId());
        dto.setObservationId(result.observationId());
        dto.setEvent(event);
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
