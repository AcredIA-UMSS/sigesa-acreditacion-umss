package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtProgramScopeSupport;
import com.umss.sigesa.adapter.in.web.dto.CreateImprovementPlanItemRequestDto;
import com.umss.sigesa.adapter.in.web.dto.ImportImprovementPlanResponseDto;
import com.umss.sigesa.adapter.in.web.dto.ImprovementPlanItemResponseDto;
import com.umss.sigesa.adapter.in.web.dto.UpdateImprovementPlanItemRequestDto;
import com.umss.sigesa.application.port.in.CreateImprovementPlanItemUseCase;
import com.umss.sigesa.application.port.in.ImportImprovementPlanFromFodaUseCase;
import com.umss.sigesa.application.port.in.ListImprovementPlanUseCase;
import com.umss.sigesa.application.port.in.UpdateImprovementPlanItemUseCase;
import com.umss.sigesa.domain.model.CreateImprovementPlanItemCommand;
import com.umss.sigesa.domain.model.ImportImprovementPlanResult;
import com.umss.sigesa.domain.model.ImprovementPlanItem;
import com.umss.sigesa.domain.model.UpdateImprovementPlanItemCommand;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/processes/{processId}/improvement-plan")
@Tag(name = "Evaluation", description = "Plan de Mejoras (API-WF-13…16 / FSD-UC-031)")
public class ImprovementPlanController {

    private final ListImprovementPlanUseCase listImprovementPlanUseCase;
    private final CreateImprovementPlanItemUseCase createImprovementPlanItemUseCase;
    private final UpdateImprovementPlanItemUseCase updateImprovementPlanItemUseCase;
    private final ImportImprovementPlanFromFodaUseCase importImprovementPlanFromFodaUseCase;

    public ImprovementPlanController(
            ListImprovementPlanUseCase listImprovementPlanUseCase,
            CreateImprovementPlanItemUseCase createImprovementPlanItemUseCase,
            UpdateImprovementPlanItemUseCase updateImprovementPlanItemUseCase,
            ImportImprovementPlanFromFodaUseCase importImprovementPlanFromFodaUseCase) {
        this.listImprovementPlanUseCase = listImprovementPlanUseCase;
        this.createImprovementPlanItemUseCase = createImprovementPlanItemUseCase;
        this.updateImprovementPlanItemUseCase = updateImprovementPlanItemUseCase;
        this.importImprovementPlanFromFodaUseCase = importImprovementPlanFromFodaUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CC','TD','JD','EE')")
    @Operation(operationId = "listImprovementPlan", summary = "Listar Plan de Mejoras del proceso")
    public ResponseEntity<List<ImprovementPlanItemResponseDto>> list(
            @PathVariable UUID processId,
            Authentication authentication) {
        List<ImprovementPlanItemResponseDto> items = listImprovementPlanUseCase.list(
                        processId,
                        (UUID) authentication.getPrincipal(),
                        extractRoles(authentication),
                        JwtProgramScopeSupport.programScopeFromAuthentication(authentication))
                .stream()
                .map(ImprovementPlanController::toDto)
                .toList();
        return ResponseEntity.ok(items);
    }

    @PostMapping
    @PreAuthorize("hasRole('CC')")
    @Operation(operationId = "createImprovementPlanItem", summary = "Crear ítem manual del Plan de Mejoras")
    public ResponseEntity<ImprovementPlanItemResponseDto> create(
            @PathVariable UUID processId,
            @RequestBody CreateImprovementPlanItemRequestDto request,
            Authentication authentication) {
        ImprovementPlanItem item = createImprovementPlanItemUseCase.create(new CreateImprovementPlanItemCommand(
                processId,
                request.getObjective(),
                request.getActivity(),
                request.getResponsibleId(),
                request.getDeadline(),
                request.getProgress(),
                (UUID) authentication.getPrincipal(),
                extractRoles(authentication),
                JwtProgramScopeSupport.programScopeFromAuthentication(authentication)));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(item));
    }

    @PostMapping("/import-from-foda")
    @PreAuthorize("hasRole('CC')")
    @Operation(operationId = "importImprovementPlanFromFoda", summary = "Importar debilidades FODA al Plan de Mejoras")
    public ResponseEntity<ImportImprovementPlanResponseDto> importFromFoda(
            @PathVariable UUID processId,
            Authentication authentication) {
        ImportImprovementPlanResult result = importImprovementPlanFromFodaUseCase.importFromFoda(
                processId,
                (UUID) authentication.getPrincipal(),
                extractRoles(authentication),
                JwtProgramScopeSupport.programScopeFromAuthentication(authentication));
        return ResponseEntity.ok(ImportImprovementPlanResponseDto.builder()
                .importedCount(result.importedCount())
                .skippedCount(result.skippedCount())
                .items(result.items().stream().map(ImprovementPlanController::toDto).toList())
                .build());
    }

    @PutMapping("/{itemId}")
    @PreAuthorize("hasRole('CC')")
    @Operation(operationId = "updateImprovementPlanItem", summary = "Actualizar ítem del Plan de Mejoras")
    public ResponseEntity<ImprovementPlanItemResponseDto> update(
            @PathVariable UUID processId,
            @PathVariable UUID itemId,
            @RequestBody UpdateImprovementPlanItemRequestDto request,
            Authentication authentication) {
        ImprovementPlanItem item = updateImprovementPlanItemUseCase.update(new UpdateImprovementPlanItemCommand(
                processId,
                itemId,
                request.getObjective(),
                request.getActivity(),
                request.getResponsibleId(),
                request.getDeadline(),
                request.getProgress(),
                request.getJustification(),
                request.getProgressEvidenceAssetId(),
                (UUID) authentication.getPrincipal(),
                extractRoles(authentication),
                JwtProgramScopeSupport.programScopeFromAuthentication(authentication)));
        return ResponseEntity.ok(toDto(item));
    }

    private static ImprovementPlanItemResponseDto toDto(ImprovementPlanItem item) {
        return ImprovementPlanItemResponseDto.builder()
                .id(item.getId())
                .processId(item.getProcessId())
                .sourceIndicatorId(item.getSourceIndicatorId())
                .objective(item.getObjective())
                .activity(item.getActivity())
                .responsibleId(item.getResponsibleId())
                .deadline(item.getDeadline())
                .progress(item.getProgress())
                .justification(item.getJustification())
                .progressEvidenceAssetId(item.getProgressEvidenceAssetId())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private static List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .toList();
    }
}
