package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtProgramScopeSupport;
import com.umss.sigesa.adapter.in.web.dto.CaeCandidateResponseDto;
import com.umss.sigesa.adapter.in.web.dto.CreateDelegationRequestDto;
import com.umss.sigesa.adapter.in.web.dto.IndicatorDelegationResponseDto;
import com.umss.sigesa.application.port.in.CreateDelegationUseCase;
import com.umss.sigesa.application.port.in.DeleteDelegationUseCase;
import com.umss.sigesa.application.port.in.ListCaeCandidatesUseCase;
import com.umss.sigesa.application.port.in.ListDelegationsUseCase;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.IndicatorDelegation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/processes/{processId}/delegations")
@Tag(name = "Evaluation", description = "Delegaciones CAE (API-WF-22/23)")
public class IndicatorDelegationController {

    private final ListDelegationsUseCase listDelegationsUseCase;
    private final CreateDelegationUseCase createDelegationUseCase;
    private final DeleteDelegationUseCase deleteDelegationUseCase;
    private final ListCaeCandidatesUseCase listCaeCandidatesUseCase;

    public IndicatorDelegationController(
            ListDelegationsUseCase listDelegationsUseCase,
            CreateDelegationUseCase createDelegationUseCase,
            DeleteDelegationUseCase deleteDelegationUseCase,
            ListCaeCandidatesUseCase listCaeCandidatesUseCase) {
        this.listDelegationsUseCase = listDelegationsUseCase;
        this.createDelegationUseCase = createDelegationUseCase;
        this.deleteDelegationUseCase = deleteDelegationUseCase;
        this.listCaeCandidatesUseCase = listCaeCandidatesUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CC','JD')")
    @Operation(operationId = "listDelegations", summary = "Listar delegaciones CAE del proceso")
    public ResponseEntity<List<IndicatorDelegationResponseDto>> list(
            @PathVariable UUID processId,
            Authentication authentication) {
        List<IndicatorDelegationResponseDto> items = listDelegationsUseCase.list(
                        processId,
                        (UUID) authentication.getPrincipal(),
                        extractRoles(authentication),
                        JwtProgramScopeSupport.programScopeFromAuthentication(authentication))
                .stream()
                .map(IndicatorDelegationController::toDto)
                .toList();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/cae-candidates")
    @PreAuthorize("hasAnyRole('CC','JD')")
    @Operation(operationId = "listCaeCandidates", summary = "Listar usuarios CAE_MEMBER de la carrera del proceso")
    public ResponseEntity<List<CaeCandidateResponseDto>> listCandidates(
            @PathVariable UUID processId,
            Authentication authentication) {
        List<CaeCandidateResponseDto> users = listCaeCandidatesUseCase.list(
                        processId,
                        (UUID) authentication.getPrincipal(),
                        extractRoles(authentication),
                        JwtProgramScopeSupport.programScopeFromAuthentication(authentication))
                .stream()
                .map(IndicatorDelegationController::toCandidate)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CC','JD')")
    @Operation(operationId = "createDelegation", summary = "Crear delegación CAE (exactamente un alcance)")
    public ResponseEntity<IndicatorDelegationResponseDto> create(
            @PathVariable UUID processId,
            @RequestBody CreateDelegationRequestDto request,
            Authentication authentication) {
        IndicatorDelegation delegation = createDelegationUseCase.create(
                processId,
                request.getUserId(),
                request.getLevel1Id(),
                request.getLevel2Id(),
                request.getIndicatorId(),
                (UUID) authentication.getPrincipal(),
                extractRoles(authentication),
                JwtProgramScopeSupport.programScopeFromAuthentication(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(delegation));
    }

    @DeleteMapping("/{delegationId}")
    @PreAuthorize("hasAnyRole('CC','JD')")
    @Operation(operationId = "deleteDelegation", summary = "Revocar delegación CAE")
    public ResponseEntity<Void> delete(
            @PathVariable UUID processId,
            @PathVariable UUID delegationId,
            Authentication authentication) {
        deleteDelegationUseCase.delete(
                processId,
                delegationId,
                (UUID) authentication.getPrincipal(),
                extractRoles(authentication),
                JwtProgramScopeSupport.programScopeFromAuthentication(authentication));
        return ResponseEntity.noContent().build();
    }

    private static IndicatorDelegationResponseDto toDto(IndicatorDelegation delegation) {
        return IndicatorDelegationResponseDto.builder()
                .id(delegation.getId())
                .processId(delegation.getProcessId())
                .userId(delegation.getUserId())
                .level1Id(delegation.getLevel1Id())
                .level2Id(delegation.getLevel2Id())
                .indicatorId(delegation.getIndicatorId())
                .permission(delegation.getPermission())
                .createdAt(delegation.getCreatedAt())
                .build();
    }

    private static CaeCandidateResponseDto toCandidate(AppUser user) {
        String first = user.getFirstName() != null ? user.getFirstName() : "";
        String last = user.getLastName() != null ? user.getLastName() : "";
        return CaeCandidateResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail() != null ? user.getEmail().toString() : null)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName((first + " " + last).trim())
                .build();
    }

    private static List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .toList();
    }
}
