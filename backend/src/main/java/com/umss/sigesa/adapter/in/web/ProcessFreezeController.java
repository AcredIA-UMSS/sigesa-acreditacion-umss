package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtProgramScopeSupport;
import com.umss.sigesa.adapter.in.web.dto.LiftFreezeRequestDto;
import com.umss.sigesa.adapter.in.web.dto.ProcessFreezeResponseDto;
import com.umss.sigesa.application.port.in.FreezeProcessUseCase;
import com.umss.sigesa.application.port.in.GetProcessFreezeUseCase;
import com.umss.sigesa.application.port.in.LiftProcessFreezeUseCase;
import com.umss.sigesa.domain.model.ProcessFreeze;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/processes/{processId}/freeze")
@Tag(name = "Stage workflow", description = "Congelamiento AUDIT_READONLY (API-WF-18…20 / FSD-UC-030)")
public class ProcessFreezeController {

    private final FreezeProcessUseCase freezeProcessUseCase;
    private final LiftProcessFreezeUseCase liftProcessFreezeUseCase;
    private final GetProcessFreezeUseCase getProcessFreezeUseCase;

    public ProcessFreezeController(
            FreezeProcessUseCase freezeProcessUseCase,
            LiftProcessFreezeUseCase liftProcessFreezeUseCase,
            GetProcessFreezeUseCase getProcessFreezeUseCase) {
        this.freezeProcessUseCase = freezeProcessUseCase;
        this.liftProcessFreezeUseCase = liftProcessFreezeUseCase;
        this.getProcessFreezeUseCase = getProcessFreezeUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('TD')")
    @Operation(operationId = "freezeProcess", summary = "Congelar proceso en AUDIT_READONLY")
    public ResponseEntity<ProcessFreezeResponseDto> freeze(
            @PathVariable UUID processId,
            Authentication authentication) {
        ProcessFreeze freeze = freezeProcessUseCase.freeze(
                processId,
                (UUID) authentication.getPrincipal(),
                extractRoles(authentication),
                JwtProgramScopeSupport.programScopeFromAuthentication(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(freeze));
    }

    @PostMapping("/lift")
    @PreAuthorize("hasRole('TD')")
    @Operation(operationId = "liftProcessFreeze", summary = "Levantar freeze hacia ACTIVE o IMPROVEMENT_MONITORING")
    public ResponseEntity<ProcessFreezeResponseDto> lift(
            @PathVariable UUID processId,
            @RequestBody LiftFreezeRequestDto request,
            Authentication authentication) {
        ProcessFreeze freeze = liftProcessFreezeUseCase.lift(
                processId,
                request != null ? request.getTargetMode() : null,
                (UUID) authentication.getPrincipal(),
                extractRoles(authentication),
                JwtProgramScopeSupport.programScopeFromAuthentication(authentication));
        return ResponseEntity.ok(toDto(freeze));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CC','TD','JD','EE','CAE_MEMBER')")
    @Operation(operationId = "getProcessFreeze", summary = "Consultar freeze del proceso")
    public ResponseEntity<ProcessFreezeResponseDto> get(
            @PathVariable UUID processId,
            Authentication authentication) {
        ProcessFreeze freeze = getProcessFreezeUseCase.get(
                processId,
                (UUID) authentication.getPrincipal(),
                extractRoles(authentication),
                JwtProgramScopeSupport.programScopeFromAuthentication(authentication));
        return ResponseEntity.ok(toDto(freeze));
    }

    private static ProcessFreezeResponseDto toDto(ProcessFreeze freeze) {
        return ProcessFreezeResponseDto.builder()
                .id(freeze.getId())
                .processId(freeze.getProcessId())
                .mode(freeze.getMode() != null ? freeze.getMode().name() : null)
                .active(freeze.isActiveAuditReadonly())
                .frozenAt(freeze.getFrozenAt())
                .frozenBy(freeze.getFrozenBy())
                .liftedAt(freeze.getLiftedAt())
                .liftedBy(freeze.getLiftedBy())
                .build();
    }

    private static List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .toList();
    }
}
