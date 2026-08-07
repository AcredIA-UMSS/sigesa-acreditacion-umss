package com.umss.sigesa.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.umss.sigesa.application.model.process.EnrichedProcessDetail;
import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.model.process.ProcessSummary;
import com.umss.sigesa.application.port.in.CreateProcessUseCase;
import com.umss.sigesa.application.port.in.GetProcessDetailUseCase;
import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import com.umss.sigesa.adapter.in.web.dto.CreateProcessRequestDto;
import com.umss.sigesa.adapter.in.web.dto.ProcessResponseDto;
import com.umss.sigesa.adapter.in.web.dto.ProcessResponsibleDto;
import com.umss.sigesa.adapter.in.web.dto.ProcessSummaryResponseDto;
import com.umss.sigesa.application.model.process.ProcessResponsibleInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/processes")
@RequiredArgsConstructor
@Tag(name = "Procesos de Acreditación", description = "Endpoints para la gestión de procesos CEUB/ARCU-SUR")
public class ProcessController {

    private final CreateProcessUseCase createProcessUseCase;
    private final ListProcessesUseCase listProcessesUseCase;
    private final GetProcessDetailUseCase getProcessDetailUseCase;
    private final UserProgramAssignmentRepositoryPort userProgramAssignmentRepositoryPort;

    @PostMapping
    @PreAuthorize("hasRole('JD')")
    @Operation(summary = "Crear un nuevo proceso", description = "Inicia un proceso clonando la taxonomía de una plantilla (Fase -> Subfase).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Proceso creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tiene permisos (Requiere ROLE_JD)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Plantilla no encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "La carrera ya cuenta con un proceso activo", content = @Content)
    })
    public ResponseEntity<ProcessResponseDto> createProcess(@Valid @RequestBody CreateProcessRequestDto request) {
        AccreditationProcess process = createProcessUseCase.createProcess(request.getCareerId(), request.getTemplateId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapCreatedProcessToDto(process));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('JD','TD','CC')")
    @Operation(summary = "Listar procesos de acreditación", description = "JD/TD ven todos; CC solo procesos de su carrera asignada (FSD-UC-019).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de procesos"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado", content = @Content)
    })
    public ResponseEntity<List<ProcessSummaryResponseDto>> listProcesses() {
        ProcessQueryContext ctx = buildQueryContext();
        List<ProcessSummaryResponseDto> response = listProcessesUseCase.list(ctx).stream()
                .map(this::mapSummaryToDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{processId}")
    @PreAuthorize("hasAnyRole('JD','TD','CC')")
    @Operation(summary = "Detalle de proceso", description = "Incluye árbol Fase -> Subfase ordenado por order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalle del proceso"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado", content = @Content),
            @ApiResponse(responseCode = "404", description = "PROCESS_NOT_FOUND", content = @Content)
    })
    public ResponseEntity<ProcessResponseDto> getProcess(@PathVariable UUID processId) {
        ProcessQueryContext ctx = buildQueryContext();
        EnrichedProcessDetail detail = getProcessDetailUseCase.getDetail(processId, ctx);
        return ResponseEntity.ok(mapDetailToDto(detail));
    }

    private ProcessQueryContext buildQueryContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = extractRole(auth);
        UUID userId = extractUserId(auth);
        List<UUID> programScope = extractProgramScopes(userId);
        return new ProcessQueryContext(role, programScope);
    }

    private String extractRole(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) {
            return "";
        }
        List<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        if (containsRole(authorities, "JD")) {
            return "JD";
        }
        if (containsRole(authorities, "TD")) {
            return "TD";
        }
        if (containsRole(authorities, "CC")) {
            return "CC";
        }
        return "";
    }

    private boolean containsRole(List<String> authorities, String role) {
        return authorities.contains("ROLE_" + role) || authorities.contains(role);
    }

    private UUID extractUserId(Authentication auth) {
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

    private List<UUID> extractProgramScopes(UUID userId) {
        try {
            List<UserProgramAssignment> assignments = userProgramAssignmentRepositoryPort.findActiveByUserId(userId);
            return assignments.stream().map(UserProgramAssignment::getProgramId).toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private ProcessSummaryResponseDto mapSummaryToDto(ProcessSummary summary) {
        return ProcessSummaryResponseDto.builder()
                .id(summary.id())
                .careerId(summary.careerId())
                .careerCode(summary.careerCode())
                .careerName(summary.careerName())
                .templateId(summary.templateId())
                .templateName(summary.templateName())
                .templateType(summary.templateType())
                .status(summary.status())
                .startDate(summary.startDate())
                .phaseCount(summary.phaseCount())
                .subphaseCount(summary.subphaseCount())
                .responsible(mapResponsibleToDto(summary.responsible()))
                .build();
    }

    private ProcessResponseDto mapDetailToDto(EnrichedProcessDetail detail) {
        return ProcessResponseDto.builder()
                .id(detail.id())
                .careerId(detail.careerId())
                .careerCode(detail.careerCode())
                .careerName(detail.careerName())
                .templateId(detail.templateId())
                .templateName(detail.templateName())
                .templateType(detail.templateType())
                .status(detail.status())
                .startDate(detail.startDate())
                .phases(detail.phases().stream().map(p -> ProcessResponseDto.PhaseDto.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .order(p.getOrder())
                        .description(p.getDescription())
                        .subphases(p.getSubphases().stream().map(s -> ProcessResponseDto.SubphaseDto.builder()
                                .id(s.getId())
                                .name(s.getName())
                                .order(s.getOrder())
                                .referenceUrl(s.getReferenceUrl())
                                .description(s.getDescription())
                                .build()).collect(Collectors.toList()))
                        .build()).collect(Collectors.toList()))
                .responsible(mapResponsibleToDto(detail.responsible()))
                .build();
    }

    private ProcessResponseDto mapCreatedProcessToDto(AccreditationProcess domain) {
        return ProcessResponseDto.builder()
                .id(domain.getId())
                .careerId(domain.getCareerId())
                .templateId(domain.getTemplateId())
                .status(domain.getStatus())
                .startDate(domain.getStartDate())
                .phases(domain.getPhases().stream().map(p -> ProcessResponseDto.PhaseDto.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .order(p.getOrder())
                        .description(p.getDescription())
                        .subphases(p.getSubphases().stream().map(s -> ProcessResponseDto.SubphaseDto.builder()
                                .id(s.getId())
                                .name(s.getName())
                                .order(s.getOrder())
                                .referenceUrl(s.getReferenceUrl())
                                .description(s.getDescription())
                        .build()).collect(Collectors.toList()))
                .build()).collect(Collectors.toList()))
                .build();
    }

    private ProcessResponsibleDto mapResponsibleToDto(ProcessResponsibleInfo info) {
        return ProcessResponsibleController.mapToDto(info);
    }
}
