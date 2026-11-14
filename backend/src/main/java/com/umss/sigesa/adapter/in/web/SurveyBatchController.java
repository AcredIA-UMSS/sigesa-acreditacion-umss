package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtProgramScopeSupport;
import com.umss.sigesa.adapter.in.web.dto.PrimarySurveyBatchResponseDto;
import com.umss.sigesa.adapter.in.web.dto.RegisterSurveyBatchRequestDto;
import com.umss.sigesa.application.port.in.ListPrimarySurveyBatchesUseCase;
import com.umss.sigesa.application.port.in.RegisterPrimarySurveyBatchUseCase;
import com.umss.sigesa.domain.model.PrimarySurveyBatch;
import com.umss.sigesa.domain.model.RegisterPrimarySurveyBatchCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/processes/{processId}/survey-batches")
@Tag(name = "Evaluation", description = "Lotes de encuesta primaria (API-WF-11 / WF-12)")
public class SurveyBatchController {

    private final RegisterPrimarySurveyBatchUseCase registerUseCase;
    private final ListPrimarySurveyBatchesUseCase listUseCase;

    public SurveyBatchController(
            RegisterPrimarySurveyBatchUseCase registerUseCase,
            ListPrimarySurveyBatchesUseCase listUseCase) {
        this.registerUseCase = registerUseCase;
        this.listUseCase = listUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('CC')")
    @Operation(operationId = "registerSurveyBatch", summary = "Registrar lote de encuesta primaria")
    public ResponseEntity<PrimarySurveyBatchResponseDto> register(
            @PathVariable UUID processId,
            @Valid @RequestBody RegisterSurveyBatchRequestDto request,
            Authentication authentication) {
        PrimarySurveyBatch batch = registerUseCase.register(new RegisterPrimarySurveyBatchCommand(
                processId,
                request.getAudience(),
                request.getResponsesCount(),
                request.getDocumentAssetId(),
                (UUID) authentication.getPrincipal(),
                extractRoles(authentication),
                JwtProgramScopeSupport.programScopeFromAuthentication(authentication)));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(batch));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CC','TD','JD','EE')")
    @Operation(operationId = "listSurveyBatches", summary = "Listar lotes de encuesta primaria del proceso")
    public ResponseEntity<List<PrimarySurveyBatchResponseDto>> list(
            @PathVariable UUID processId,
            Authentication authentication) {
        List<PrimarySurveyBatchResponseDto> batches = listUseCase.list(
                        processId,
                        (UUID) authentication.getPrincipal(),
                        extractRoles(authentication),
                        JwtProgramScopeSupport.programScopeFromAuthentication(authentication))
                .stream()
                .map(SurveyBatchController::toDto)
                .toList();
        return ResponseEntity.ok(batches);
    }

    private static PrimarySurveyBatchResponseDto toDto(PrimarySurveyBatch batch) {
        return PrimarySurveyBatchResponseDto.builder()
                .id(batch.getId())
                .processId(batch.getProcessId())
                .audience(batch.getAudience())
                .responsesCount(batch.getResponsesCount())
                .documentAssetId(batch.getDocumentAssetId())
                .registeredAt(batch.getRegisteredAt())
                .build();
    }

    private static List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .toList();
    }
}
