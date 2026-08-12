package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.TemplateDetailResponseDto;
import com.umss.sigesa.adapter.in.web.dto.TemplatePhaseRequestDto;
import com.umss.sigesa.adapter.in.web.dto.TemplatePhaseResponseDto;
import com.umss.sigesa.adapter.in.web.dto.TemplateSubphaseRequestDto;
import com.umss.sigesa.adapter.in.web.dto.TemplateSubphaseResponseDto;
import com.umss.sigesa.adapter.in.web.dto.TemplateSummaryResponseDto;
import com.umss.sigesa.adapter.in.web.dto.UpsertTemplateRequestDto;
import com.umss.sigesa.application.port.in.ArchiveTemplateUseCase;
import com.umss.sigesa.application.port.in.CreateTemplateUseCase;
import com.umss.sigesa.application.port.in.DeleteTemplateUseCase;
import com.umss.sigesa.application.port.in.DuplicateTemplateUseCase;
import com.umss.sigesa.application.port.in.GetTemplateUseCase;
import com.umss.sigesa.application.port.in.ListTemplatesUseCase;
import com.umss.sigesa.application.port.in.PublishTemplateUseCase;
import com.umss.sigesa.application.port.in.UpdateTemplateUseCase;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplatePhase;
import com.umss.sigesa.domain.model.TemplateStatus;
import com.umss.sigesa.domain.model.TemplateSubphase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('JD')")
@Tag(name = "Plantillas normativas", description = "Gestión CRUD de plantillas CEUB/ARCU-SUR (FSD-UC-021)")
public class TemplateController {

    private final CreateTemplateUseCase createTemplateUseCase;
    private final UpdateTemplateUseCase updateTemplateUseCase;
    private final GetTemplateUseCase getTemplateUseCase;
    private final ListTemplatesUseCase listTemplatesUseCase;
    private final PublishTemplateUseCase publishTemplateUseCase;
    private final ArchiveTemplateUseCase archiveTemplateUseCase;
    private final DuplicateTemplateUseCase duplicateTemplateUseCase;
    private final DeleteTemplateUseCase deleteTemplateUseCase;

    @GetMapping
    @Operation(summary = "Listar plantillas normativas")
    public ResponseEntity<List<TemplateSummaryResponseDto>> listTemplates(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        Optional<TemplateStatus> statusFilter = parseStatus(status);
        Optional<String> typeFilter = type != null && !type.isBlank()
                ? Optional.of(type.trim().toUpperCase())
                : Optional.empty();

        List<TemplateSummaryResponseDto> response = listTemplatesUseCase.list(statusFilter, typeFilter).stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Crear plantilla en borrador")
    public ResponseEntity<TemplateDetailResponseDto> createTemplate(
            @Valid @RequestBody UpsertTemplateRequestDto request) {
        Template created = createTemplateUseCase.create(fromRequest(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDetailDto(created));
    }

    @GetMapping("/{templateId}")
    @Operation(summary = "Obtener detalle de plantilla")
    public ResponseEntity<TemplateDetailResponseDto> getTemplate(@PathVariable UUID templateId) {
        return ResponseEntity.ok(toDetailDto(getTemplateUseCase.getById(templateId)));
    }

    @PutMapping("/{templateId}")
    @Operation(summary = "Actualizar plantilla")
    public ResponseEntity<TemplateDetailResponseDto> updateTemplate(
            @PathVariable UUID templateId,
            @Valid @RequestBody UpsertTemplateRequestDto request) {
        Template updated = updateTemplateUseCase.update(templateId, fromRequest(request));
        return ResponseEntity.ok(toDetailDto(updated));
    }

    @DeleteMapping("/{templateId}")
    @Operation(summary = "Eliminar plantilla DRAFT sin procesos referenciados")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID templateId) {
        deleteTemplateUseCase.delete(templateId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{templateId}/publish")
    @Operation(summary = "Publicar plantilla")
    public ResponseEntity<TemplateDetailResponseDto> publishTemplate(@PathVariable UUID templateId) {
        return ResponseEntity.ok(toDetailDto(publishTemplateUseCase.publish(templateId)));
    }

    @PostMapping("/{templateId}/duplicate")
    @Operation(summary = "Duplicar plantilla como borrador")
    public ResponseEntity<TemplateDetailResponseDto> duplicateTemplate(@PathVariable UUID templateId) {
        Template duplicated = duplicateTemplateUseCase.duplicate(templateId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDetailDto(duplicated));
    }

    @PostMapping("/{templateId}/archive")
    @Operation(summary = "Archivar plantilla")
    public ResponseEntity<TemplateDetailResponseDto> archiveTemplate(@PathVariable UUID templateId) {
        return ResponseEntity.ok(toDetailDto(archiveTemplateUseCase.archive(templateId)));
    }

    private Optional<TemplateStatus> parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(TemplateStatus.valueOf(status.trim().toUpperCase()));
    }

    private Template fromRequest(UpsertTemplateRequestDto request) {
        List<TemplatePhase> phases = new ArrayList<>();
        if (request.getPhases() != null) {
            for (TemplatePhaseRequestDto phaseDto : request.getPhases()) {
                List<TemplateSubphase> subphases = new ArrayList<>();
                if (phaseDto.getSubphases() != null) {
                    for (TemplateSubphaseRequestDto subphaseDto : phaseDto.getSubphases()) {
                        subphases.add(TemplateSubphase.builder()
                                .id(subphaseDto.getId())
                                .name(subphaseDto.getName())
                                .order(subphaseDto.getOrder())
                                .referenceUrl(subphaseDto.getReferenceUrl())
                                .description(subphaseDto.getDescription())
                                .build());
                    }
                }
                phases.add(TemplatePhase.builder()
                        .id(phaseDto.getId())
                        .name(phaseDto.getName())
                        .order(phaseDto.getOrder())
                        .description(phaseDto.getDescription())
                        .subphases(subphases)
                        .build());
            }
        }

        return Template.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .phases(phases)
                .build();
    }

    private TemplateSummaryResponseDto toSummaryDto(Template template) {
        return TemplateSummaryResponseDto.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .type(template.getType())
                .status(template.getStatus() != null ? template.getStatus().name() : null)
                .phaseCount(countPhases(template))
                .subphaseCount(countSubphases(template))
                .build();
    }

    private TemplateDetailResponseDto toDetailDto(Template template) {
        return TemplateDetailResponseDto.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .type(template.getType())
                .status(template.getStatus() != null ? template.getStatus().name() : null)
                .phaseCount(countPhases(template))
                .subphaseCount(countSubphases(template))
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .phases(template.getPhases() != null
                        ? template.getPhases().stream().map(this::toPhaseResponse).collect(Collectors.toList())
                        : List.of())
                .build();
    }

    private TemplatePhaseResponseDto toPhaseResponse(TemplatePhase phase) {
        return TemplatePhaseResponseDto.builder()
                .id(phase.getId())
                .name(phase.getName())
                .order(phase.getOrder())
                .description(phase.getDescription())
                .subphases(phase.getSubphases() != null
                        ? phase.getSubphases().stream().map(this::toSubphaseResponse).collect(Collectors.toList())
                        : List.of())
                .build();
    }

    private TemplateSubphaseResponseDto toSubphaseResponse(TemplateSubphase subphase) {
        return TemplateSubphaseResponseDto.builder()
                .id(subphase.getId())
                .name(subphase.getName())
                .order(subphase.getOrder())
                .referenceUrl(subphase.getReferenceUrl())
                .description(subphase.getDescription())
                .build();
    }

    private int countPhases(Template template) {
        return template.getPhases() != null ? template.getPhases().size() : 0;
    }

    private int countSubphases(Template template) {
        if (template.getPhases() == null) {
            return 0;
        }
        return template.getPhases().stream()
                .mapToInt(phase -> phase.getSubphases() != null ? phase.getSubphases().size() : 0)
                .sum();
    }
}
