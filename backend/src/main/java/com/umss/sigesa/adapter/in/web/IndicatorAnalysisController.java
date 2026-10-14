package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtProgramScopeSupport;
import com.umss.sigesa.adapter.in.web.dto.IndicatorAnalysisResponseDto;
import com.umss.sigesa.adapter.in.web.dto.UpsertIndicatorAnalysisRequestDto;
import com.umss.sigesa.application.port.in.GetIndicatorAnalysisUseCase;
import com.umss.sigesa.application.port.in.UpsertIndicatorAnalysisUseCase;
import com.umss.sigesa.domain.model.IndicatorAnalysis;
import com.umss.sigesa.domain.model.UpsertIndicatorAnalysisCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/indicators/{indicatorId}/analysis")
@Tag(name = "Evaluation", description = "Análisis FODA por indicador (API-WF-10 / FSD-UC-029)")
public class IndicatorAnalysisController {

    private final GetIndicatorAnalysisUseCase getIndicatorAnalysisUseCase;
    private final UpsertIndicatorAnalysisUseCase upsertIndicatorAnalysisUseCase;

    public IndicatorAnalysisController(
            GetIndicatorAnalysisUseCase getIndicatorAnalysisUseCase,
            UpsertIndicatorAnalysisUseCase upsertIndicatorAnalysisUseCase) {
        this.getIndicatorAnalysisUseCase = getIndicatorAnalysisUseCase;
        this.upsertIndicatorAnalysisUseCase = upsertIndicatorAnalysisUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CC','TD','JD')")
    @Operation(operationId = "getIndicatorAnalysis", summary = "Obtener análisis FODA del indicador")
    public ResponseEntity<IndicatorAnalysisResponseDto> getAnalysis(
            @PathVariable UUID indicatorId,
            Authentication authentication) {
        IndicatorAnalysis analysis = getIndicatorAnalysisUseCase.get(
                indicatorId,
                (UUID) authentication.getPrincipal(),
                extractRoles(authentication),
                JwtProgramScopeSupport.programScopeFromAuthentication(authentication));
        return ResponseEntity.ok(toDto(analysis));
    }

    @PutMapping
    @PreAuthorize("hasRole('CC')")
    @Operation(operationId = "upsertIndicatorAnalysis", summary = "Crear o actualizar análisis FODA del indicador")
    public ResponseEntity<IndicatorAnalysisResponseDto> upsertAnalysis(
            @PathVariable UUID indicatorId,
            @Valid @RequestBody UpsertIndicatorAnalysisRequestDto request,
            Authentication authentication) {
        IndicatorAnalysis analysis = upsertIndicatorAnalysisUseCase.upsert(new UpsertIndicatorAnalysisCommand(
                indicatorId,
                request.getFodaQuadrant(),
                request.getCompliance(),
                request.getSynthesis(),
                (UUID) authentication.getPrincipal(),
                extractRoles(authentication),
                JwtProgramScopeSupport.programScopeFromAuthentication(authentication)));
        return ResponseEntity.ok(toDto(analysis));
    }

    private static IndicatorAnalysisResponseDto toDto(IndicatorAnalysis analysis) {
        return IndicatorAnalysisResponseDto.builder()
                .id(analysis.getId())
                .indicatorId(analysis.getIndicatorId())
                .processId(analysis.getProcessId())
                .fodaQuadrant(analysis.getFodaQuadrant() != null ? analysis.getFodaQuadrant().name() : null)
                .compliance(analysis.getCompliance() != null ? analysis.getCompliance().name() : null)
                .synthesis(analysis.getSynthesis())
                .authorId(analysis.getAuthorId())
                .createdAt(analysis.getCreatedAt())
                .updatedAt(analysis.getUpdatedAt())
                .build();
    }

    private static List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .toList();
    }
}
