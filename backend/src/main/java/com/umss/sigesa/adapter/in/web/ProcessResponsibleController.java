package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.AssignResponsibleRequestDto;
import com.umss.sigesa.adapter.in.web.dto.EligibleResponsibleDto;
import com.umss.sigesa.adapter.in.web.dto.ProcessResponsibleDto;
import com.umss.sigesa.application.model.process.ProcessResponsibleInfo;
import com.umss.sigesa.application.port.in.AssignProcessResponsibleUseCase;
import com.umss.sigesa.application.port.in.ListEligibleResponsiblesUseCase;
import com.umss.sigesa.application.port.in.RemoveProcessResponsibleUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/processes")
@RequiredArgsConstructor
@Tag(name = "Procesos de Acreditación", description = "Asignación de responsable [CC] (FSD-UC-023)")
public class ProcessResponsibleController {

    private final AssignProcessResponsibleUseCase assignProcessResponsibleUseCase;
    private final RemoveProcessResponsibleUseCase removeProcessResponsibleUseCase;
    private final ListEligibleResponsiblesUseCase listEligibleResponsiblesUseCase;

    @PutMapping("/{processId}/responsible")
    @PreAuthorize("hasRole('JD')")
    @Operation(summary = "Asignar responsable [CC]", description = "Designa un coordinador como responsable único del proceso ACTIVE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Responsable asignado"),
            @ApiResponse(responseCode = "400", description = "INVALID_RESPONSIBLE_USER", content = @Content),
            @ApiResponse(responseCode = "403", description = "Solo JD", content = @Content),
            @ApiResponse(responseCode = "404", description = "PROCESS_NOT_FOUND", content = @Content),
            @ApiResponse(responseCode = "409", description = "CC_ALREADY_ASSIGNED / CAREER_SCOPE_MISMATCH / PROCESS_NOT_EDITABLE", content = @Content)
    })
    public ResponseEntity<ProcessResponsibleDto> assignResponsible(
            @PathVariable UUID processId,
            @Valid @RequestBody AssignResponsibleRequestDto request) {
        ProcessResponsibleInfo info = assignProcessResponsibleUseCase.assign(
                processId,
                request.getUserId(),
                extractUserId());
        return ResponseEntity.ok(mapToDto(info));
    }

    @DeleteMapping("/{processId}/responsible")
    @PreAuthorize("hasRole('JD')")
    @Operation(summary = "Quitar responsable", description = "Revoca la asignación activa del proceso.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Responsable removido"),
            @ApiResponse(responseCode = "403", description = "Solo JD", content = @Content),
            @ApiResponse(responseCode = "404", description = "PROCESS_NOT_FOUND", content = @Content),
            @ApiResponse(responseCode = "409", description = "PROCESS_NOT_EDITABLE", content = @Content)
    })
    public ResponseEntity<Void> removeResponsible(@PathVariable UUID processId) {
        removeProcessResponsibleUseCase.remove(processId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{processId}/responsible/candidates")
    @PreAuthorize("hasRole('JD')")
    @Operation(summary = "Listar candidatos [CC]", description = "Coordinadores elegibles para el proceso según carrera y disponibilidad.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de candidatos"),
            @ApiResponse(responseCode = "403", description = "Solo JD", content = @Content),
            @ApiResponse(responseCode = "404", description = "PROCESS_NOT_FOUND", content = @Content)
    })
    public ResponseEntity<List<EligibleResponsibleDto>> listCandidates(@PathVariable UUID processId) {
        List<EligibleResponsibleDto> candidates = listEligibleResponsiblesUseCase.listEligible(processId).stream()
                .map(candidate -> EligibleResponsibleDto.builder()
                        .userId(candidate.userId())
                        .fullName(candidate.fullName())
                        .email(candidate.email())
                        .build())
                .toList();
        return ResponseEntity.ok(candidates);
    }

    private UUID extractUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return UUID.randomUUID();
        }
        if (auth.getPrincipal() instanceof UUID uuid) {
            return uuid;
        }
        try {
            return UUID.fromString(auth.getPrincipal().toString());
        } catch (Exception e) {
            return UUID.randomUUID();
        }
    }

    static ProcessResponsibleDto mapToDto(ProcessResponsibleInfo info) {
        if (info == null) {
            return null;
        }
        return ProcessResponsibleDto.builder()
                .userId(info.userId())
                .fullName(info.fullName())
                .email(info.email())
                .assignedAt(info.assignedAt())
                .build();
    }
}
