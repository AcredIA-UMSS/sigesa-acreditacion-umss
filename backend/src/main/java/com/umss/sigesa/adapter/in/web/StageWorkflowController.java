package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.MethodologicalStageResponseDto;
import com.umss.sigesa.adapter.in.web.dto.ObserveStageRequestDto;
import com.umss.sigesa.adapter.in.web.dto.StageDeliverableResponseDto;
import com.umss.sigesa.adapter.in.web.dto.StageGateEvaluationResponseDto;
import com.umss.sigesa.application.port.in.ApproveStageDeliverableUseCase;
import com.umss.sigesa.application.port.in.ApproveStageUseCase;
import com.umss.sigesa.application.port.in.EvaluateStageGateUseCase;
import com.umss.sigesa.application.port.in.ListProcessStagesUseCase;
import com.umss.sigesa.application.port.in.ObserveStageUseCase;
import com.umss.sigesa.application.port.in.SubmitStageForReviewUseCase;
import com.umss.sigesa.domain.model.MethodologicalStage;
import com.umss.sigesa.domain.model.StageDeliverable;
import com.umss.sigesa.domain.model.StageGateResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/processes/{processId}/stages")
@Tag(name = "Stage workflow", description = "Workflow metodológico E1–E7 (FSD-UC-025…028 / ADR-0005)")
public class StageWorkflowController {

    private final ListProcessStagesUseCase listProcessStagesUseCase;
    private final SubmitStageForReviewUseCase submitStageForReviewUseCase;
    private final ApproveStageUseCase approveStageUseCase;
    private final ObserveStageUseCase observeStageUseCase;
    private final EvaluateStageGateUseCase evaluateStageGateUseCase;
    private final ApproveStageDeliverableUseCase approveStageDeliverableUseCase;

    public StageWorkflowController(
            ListProcessStagesUseCase listProcessStagesUseCase,
            SubmitStageForReviewUseCase submitStageForReviewUseCase,
            ApproveStageUseCase approveStageUseCase,
            ObserveStageUseCase observeStageUseCase,
            EvaluateStageGateUseCase evaluateStageGateUseCase,
            ApproveStageDeliverableUseCase approveStageDeliverableUseCase) {
        this.listProcessStagesUseCase = listProcessStagesUseCase;
        this.submitStageForReviewUseCase = submitStageForReviewUseCase;
        this.approveStageUseCase = approveStageUseCase;
        this.observeStageUseCase = observeStageUseCase;
        this.evaluateStageGateUseCase = evaluateStageGateUseCase;
        this.approveStageDeliverableUseCase = approveStageDeliverableUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CC','TD','JD')")
    @Operation(summary = "Listar timeline de etapas metodológicas del proceso")
    public ResponseEntity<List<MethodologicalStageResponseDto>> listStages(@PathVariable UUID processId) {
        List<MethodologicalStageResponseDto> stages = listProcessStagesUseCase.listStages(processId).stream()
                .map(StageWorkflowController::toStageDto)
                .toList();
        return ResponseEntity.ok(stages);
    }

    @PostMapping("/{stageId}/submit")
    @PreAuthorize("hasRole('CC')")
    @Operation(summary = "Enviar etapa activa a revisión técnica")
    public ResponseEntity<MethodologicalStageResponseDto> submitStage(
            @PathVariable UUID processId,
            @PathVariable UUID stageId,
            Authentication authentication) {
        MethodologicalStage stage = submitStageForReviewUseCase.submit(
                processId,
                stageId,
                actorId(authentication),
                primaryRole(authentication));
        return ResponseEntity.ok(toStageDto(stage));
    }

    @PostMapping("/{stageId}/approve")
    @PreAuthorize("hasAnyRole('TD','JD')")
    @Operation(summary = "Aprobar etapa metodológica (exige compuerta PASS)")
    public ResponseEntity<MethodologicalStageResponseDto> approveStage(
            @PathVariable UUID processId,
            @PathVariable UUID stageId,
            Authentication authentication) {
        MethodologicalStage stage = approveStageUseCase.approve(
                processId,
                stageId,
                actorId(authentication),
                primaryRole(authentication));
        return ResponseEntity.ok(toStageDto(stage));
    }

    @PostMapping("/{stageId}/observe")
    @PreAuthorize("hasAnyRole('TD','JD')")
    @Operation(summary = "Observar etapa metodológica (devolver a CC)")
    public ResponseEntity<MethodologicalStageResponseDto> observeStage(
            @PathVariable UUID processId,
            @PathVariable UUID stageId,
            @RequestBody(required = false) ObserveStageRequestDto request,
            Authentication authentication) {
        String observations = request != null ? request.getObservations() : null;
        MethodologicalStage stage = observeStageUseCase.observe(
                processId,
                stageId,
                actorId(authentication),
                primaryRole(authentication),
                observations);
        return ResponseEntity.ok(toStageDto(stage));
    }

    @GetMapping("/{stageId}/gate")
    @PreAuthorize("hasRole('TD')")
    @Operation(summary = "Preview de compuerta de avance para la etapa")
    public ResponseEntity<StageGateEvaluationResponseDto> evaluateGate(
            @PathVariable UUID processId,
            @PathVariable UUID stageId,
            Authentication authentication) {
        StageGateResult result = evaluateStageGateUseCase.evaluate(
                processId,
                stageId,
                actorId(authentication),
                primaryRole(authentication));
        return ResponseEntity.ok(toGateDto(result));
    }

    @PostMapping("/{stageId}/deliverables/{deliverableId}/approve")
    @PreAuthorize("hasRole('TD')")
    @Operation(summary = "Aprobar entregable documental de etapa")
    public ResponseEntity<StageDeliverableResponseDto> approveDeliverable(
            @PathVariable UUID processId,
            @PathVariable UUID stageId,
            @PathVariable UUID deliverableId,
            Authentication authentication) {
        StageDeliverable deliverable = approveStageDeliverableUseCase.approve(
                processId,
                stageId,
                deliverableId,
                actorId(authentication),
                primaryRole(authentication));
        return ResponseEntity.ok(toDeliverableDto(deliverable));
    }

    private static MethodologicalStageResponseDto toStageDto(MethodologicalStage stage) {
        MethodologicalStageResponseDto dto = new MethodologicalStageResponseDto();
        dto.setId(stage.getId());
        dto.setProcessId(stage.getProcessId());
        dto.setOrder(stage.getOrder());
        dto.setCode(stage.getCode().name());
        dto.setDisplayName(stage.getCode().displayName());
        dto.setStatus(stage.getStatus().name());
        dto.setStartedAt(stage.getStartedAt());
        dto.setClosedAt(stage.getClosedAt());
        dto.setDeliverables(stage.getDeliverables().stream()
                .map(StageWorkflowController::toDeliverableDto)
                .toList());
        return dto;
    }

    private static StageDeliverableResponseDto toDeliverableDto(StageDeliverable deliverable) {
        StageDeliverableResponseDto dto = new StageDeliverableResponseDto();
        dto.setId(deliverable.getId());
        dto.setStageId(deliverable.getStageId());
        dto.setDeliverableCode(deliverable.getDeliverableCode().name());
        dto.setApprovalStatus(deliverable.getApprovalStatus().name());
        dto.setApprovedBy(deliverable.getApprovedBy());
        dto.setTechnicalObservations(deliverable.getTechnicalObservations());
        dto.setApprovedAt(deliverable.getApprovedAt());
        return dto;
    }

    private static StageGateEvaluationResponseDto toGateDto(StageGateResult result) {
        StageGateEvaluationResponseDto dto = new StageGateEvaluationResponseDto();
        dto.setPass(result.pass());
        dto.setFailedRules(result.failedRules());
        dto.setSummary(result.summary());
        return dto;
    }

    private static UUID actorId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    private static String primaryRole(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.startsWith("ROLE_") ? auth.substring(5) : auth)
                .toList();
        return roles.isEmpty() ? "" : roles.getFirst();
    }
}
