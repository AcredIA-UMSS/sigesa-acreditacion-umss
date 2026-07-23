package com.umss.sigesa.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.umss.sigesa.application.port.in.CreateProcessUseCase;
import com.umss.sigesa.domain.exception.ProcessAlreadyActiveException;
import com.umss.sigesa.domain.exception.TemplateNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.adapter.in.web.dto.CreateProcessRequestDto;
import com.umss.sigesa.adapter.in.web.dto.ProcessResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/processes")
@RequiredArgsConstructor
@Tag(name = "Procesos de Acreditación", description = "Endpoints para la gestión de procesos CEUB/ARCU-SUR")
public class ProcessController {

    private final CreateProcessUseCase createProcessUseCase;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_JD')")
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
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDto(process));
    }

    private ProcessResponseDto mapToDto(AccreditationProcess domain) {
        return ProcessResponseDto.builder()
                .id(domain.getId())
                .careerId(domain.getCareerId())
                .status(domain.getStatus())
                .startDate(domain.getStartDate())
                .phases(domain.getPhases().stream().map(p -> ProcessResponseDto.PhaseDto.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .order(p.getOrder())
                        .subphases(p.getSubphases().stream().map(s -> ProcessResponseDto.SubphaseDto.builder()
                                .id(s.getId())
                                .name(s.getName())
                                .order(s.getOrder())
                                .build()).collect(Collectors.toList()))
                        .build()).collect(Collectors.toList()))
                .build();
    }

    @ExceptionHandler(ProcessAlreadyActiveException.class)
    public ResponseEntity<String> handleProcessAlreadyActive(ProcessAlreadyActiveException ex) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "PROCESS_ALREADY_ACTIVE", ex);
    }

    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<String> handleTemplateNotFound(TemplateNotFoundException ex) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "TEMPLATE_NOT_FOUND", ex);
    }
}