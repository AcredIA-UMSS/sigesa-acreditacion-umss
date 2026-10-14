package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtProgramScopeSupport;
import com.umss.sigesa.adapter.in.web.dto.EvaluationMetricsResponseDto;
import com.umss.sigesa.application.port.in.GetEvaluationMetricsUseCase;
import com.umss.sigesa.domain.model.EvaluationMetrics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/processes/{processId}/evaluation-metrics")
@Tag(name = "Evaluation", description = "Métricas de completitud y análisis FODA (API-WF-09 / FSD-UC-029)")
public class EvaluationMetricsController {

    private final GetEvaluationMetricsUseCase getEvaluationMetricsUseCase;

    public EvaluationMetricsController(GetEvaluationMetricsUseCase getEvaluationMetricsUseCase) {
        this.getEvaluationMetricsUseCase = getEvaluationMetricsUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CC','TD','JD')")
    @Operation(operationId = "getEvaluationMetrics", summary = "Consultar métricas de evidencia, FODA y encuestas del proceso")
    public ResponseEntity<EvaluationMetricsResponseDto> getMetrics(
            @PathVariable UUID processId,
            Authentication authentication) {
        EvaluationMetrics metrics = getEvaluationMetricsUseCase.get(
                processId,
                (UUID) authentication.getPrincipal(),
                extractRoles(authentication),
                JwtProgramScopeSupport.programScopeFromAuthentication(authentication));
        return ResponseEntity.ok(EvaluationMetricsResponseDto.builder()
                .processId(metrics.processId())
                .totalIndicators(metrics.totalIndicators())
                .indicatorsWithEvidence(metrics.indicatorsWithEvidence())
                .evidenceCompletenessRatio(metrics.evidenceCompletenessRatio())
                .fodaComplete(metrics.fodaComplete())
                .fodaCompletenessRatio(metrics.fodaCompletenessRatio())
                .primarySurveyBatchCount(metrics.primarySurveyBatchCount())
                .build());
    }

    private static List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .toList();
    }
}
