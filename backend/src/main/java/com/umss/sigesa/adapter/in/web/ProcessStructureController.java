package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.CreatePhaseRequestDto;
import com.umss.sigesa.adapter.in.web.dto.CreateSubphaseRequestDto;
import com.umss.sigesa.adapter.in.web.dto.ProcessResponseDto;
import com.umss.sigesa.adapter.in.web.dto.ReorderStructureRequestDto;
import com.umss.sigesa.adapter.in.web.dto.UpdatePhaseRequestDto;
import com.umss.sigesa.adapter.in.web.dto.UpdateSubphaseRequestDto;
import com.umss.sigesa.application.port.in.AddProcessPhaseUseCase;
import com.umss.sigesa.application.port.in.AddProcessSubphaseUseCase;
import com.umss.sigesa.application.port.in.DeleteProcessPhaseUseCase;
import com.umss.sigesa.application.port.in.DeleteProcessSubphaseUseCase;
import com.umss.sigesa.application.port.in.ReorderProcessStructureUseCase;
import com.umss.sigesa.application.port.in.UpdateProcessPhaseUseCase;
import com.umss.sigesa.application.port.in.UpdateProcessSubphaseUseCase;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Phase;
import com.umss.sigesa.domain.model.Subphase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/processes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('JD','TD')")
@Tag(name = "Estructura de proceso", description = "CRUD de fases/subfases en proceso ACTIVE (FSD-UC-022)")
public class ProcessStructureController {

    private final AddProcessPhaseUseCase addProcessPhaseUseCase;
    private final UpdateProcessPhaseUseCase updateProcessPhaseUseCase;
    private final DeleteProcessPhaseUseCase deleteProcessPhaseUseCase;
    private final AddProcessSubphaseUseCase addProcessSubphaseUseCase;
    private final UpdateProcessSubphaseUseCase updateProcessSubphaseUseCase;
    private final DeleteProcessSubphaseUseCase deleteProcessSubphaseUseCase;
    private final ReorderProcessStructureUseCase reorderProcessStructureUseCase;

    @PostMapping("/{processId}/phases")
    @Operation(summary = "Agregar fase a proceso ACTIVE")
    public ResponseEntity<ProcessResponseDto.PhaseDto> addPhase(
            @PathVariable UUID processId,
            @Valid @RequestBody CreatePhaseRequestDto request) {
        Phase phase = addProcessPhaseUseCase.execute(
                processId, request.getName(), request.getOrder(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(toPhaseDto(phase));
    }

    @PutMapping("/{processId}/phases/{phaseId}")
    @Operation(summary = "Actualizar fase de proceso")
    public ResponseEntity<ProcessResponseDto.PhaseDto> updatePhase(
            @PathVariable UUID processId,
            @PathVariable UUID phaseId,
            @RequestBody UpdatePhaseRequestDto request) {
        Phase phase = updateProcessPhaseUseCase.execute(
                processId, phaseId, request.getName(), request.getOrder(), request.getDescription());
        return ResponseEntity.ok(toPhaseDto(phase));
    }

    @DeleteMapping("/{processId}/phases/{phaseId}")
    @Operation(summary = "Eliminar fase de proceso")
    public ResponseEntity<Void> deletePhase(
            @PathVariable UUID processId,
            @PathVariable UUID phaseId) {
        deleteProcessPhaseUseCase.execute(processId, phaseId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{processId}/phases/{phaseId}/subphases")
    @Operation(summary = "Agregar subfase a fase")
    public ResponseEntity<ProcessResponseDto.SubphaseDto> addSubphase(
            @PathVariable UUID processId,
            @PathVariable UUID phaseId,
            @Valid @RequestBody CreateSubphaseRequestDto request) {
        Subphase subphase = addProcessSubphaseUseCase.execute(
                processId,
                phaseId,
                request.getName(),
                request.getOrder(),
                request.getReferenceUrl(),
                request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(toSubphaseDto(subphase));
    }

    @PutMapping("/{processId}/phases/{phaseId}/subphases/{subphaseId}")
    @Operation(summary = "Actualizar subfase")
    public ResponseEntity<ProcessResponseDto.SubphaseDto> updateSubphase(
            @PathVariable UUID processId,
            @PathVariable UUID phaseId,
            @PathVariable UUID subphaseId,
            @RequestBody UpdateSubphaseRequestDto request) {
        Subphase subphase = updateProcessSubphaseUseCase.execute(
                processId,
                phaseId,
                subphaseId,
                request.getName(),
                request.getOrder(),
                request.getReferenceUrl(),
                request.getDescription());
        return ResponseEntity.ok(toSubphaseDto(subphase));
    }

    @DeleteMapping("/{processId}/phases/{phaseId}/subphases/{subphaseId}")
    @Operation(summary = "Eliminar subfase")
    public ResponseEntity<Void> deleteSubphase(
            @PathVariable UUID processId,
            @PathVariable UUID phaseId,
            @PathVariable UUID subphaseId) {
        deleteProcessSubphaseUseCase.execute(processId, phaseId, subphaseId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{processId}/structure/reorder")
    @Operation(summary = "Reordenar fases y/o subfases")
    public ResponseEntity<ProcessResponseDto> reorderStructure(
            @PathVariable UUID processId,
            @RequestBody ReorderStructureRequestDto request) {
        AccreditationProcess process = reorderProcessStructureUseCase.execute(
                processId, request.getPhases(), request.getSubphasesByPhase());
        return ResponseEntity.ok(toProcessDto(process));
    }

    private ProcessResponseDto toProcessDto(AccreditationProcess process) {
        return ProcessResponseDto.builder()
                .id(process.getId())
                .careerId(process.getCareerId())
                .templateId(process.getTemplateId())
                .status(process.getStatus())
                .startDate(process.getStartDate())
                .phases(process.getPhases().stream()
                        .sorted(Comparator.comparing(Phase::getOrder, Comparator.nullsLast(Integer::compareTo)))
                        .map(this::toPhaseDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private ProcessResponseDto.PhaseDto toPhaseDto(Phase phase) {
        return ProcessResponseDto.PhaseDto.builder()
                .id(phase.getId())
                .name(phase.getName())
                .order(phase.getOrder())
                .description(phase.getDescription())
                .subphases(phase.getSubphases() == null ? null : phase.getSubphases().stream()
                        .sorted(Comparator.comparing(Subphase::getOrder, Comparator.nullsLast(Integer::compareTo)))
                        .map(this::toSubphaseDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private ProcessResponseDto.SubphaseDto toSubphaseDto(Subphase subphase) {
        return ProcessResponseDto.SubphaseDto.builder()
                .id(subphase.getId())
                .name(subphase.getName())
                .order(subphase.getOrder())
                .referenceUrl(subphase.getReferenceUrl())
                .description(subphase.getDescription())
                .build();
    }
}
