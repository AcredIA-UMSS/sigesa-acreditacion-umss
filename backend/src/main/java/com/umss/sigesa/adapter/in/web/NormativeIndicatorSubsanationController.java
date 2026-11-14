package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.NormativeIndicatorSubsanationEligibilityResponseDto;
import com.umss.sigesa.adapter.in.web.dto.SubsanateNormativeIndicatorEvidenceResponseDto;
import com.umss.sigesa.application.port.in.GetNormativeIndicatorSubsanationEligibilityUseCase;
import com.umss.sigesa.application.port.in.SubsanateNormativeIndicatorEvidenceUseCase;
import com.umss.sigesa.domain.model.EvidenceSubsanationResult;
import com.umss.sigesa.domain.model.NormativeIndicatorEvidenceSubsanationCommand;
import com.umss.sigesa.domain.model.NormativeIndicatorSubsanationEligibility;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/indicators/{indicatorId}")
@Tag(name = "Evidence", description = "Subsanación de evidencias por indicador normativo v2 (API-EVD-05)")
public class NormativeIndicatorSubsanationController {

    private final GetNormativeIndicatorSubsanationEligibilityUseCase eligibilityUseCase;
    private final SubsanateNormativeIndicatorEvidenceUseCase subsanateUseCase;

    public NormativeIndicatorSubsanationController(
            GetNormativeIndicatorSubsanationEligibilityUseCase eligibilityUseCase,
            SubsanateNormativeIndicatorEvidenceUseCase subsanateUseCase) {
        this.eligibilityUseCase = eligibilityUseCase;
        this.subsanateUseCase = subsanateUseCase;
    }

    @GetMapping("/subsanation-eligibility")
    @Operation(summary = "Verificar si el indicador permite subsanación (API-EVD-05)")
    public ResponseEntity<NormativeIndicatorSubsanationEligibilityResponseDto> subsanationEligibility(
            @PathVariable UUID indicatorId,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        NormativeIndicatorSubsanationEligibility eligibility = eligibilityUseCase.get(
                indicatorId, userId, extractRoles(authentication));
        NormativeIndicatorSubsanationEligibilityResponseDto dto = new NormativeIndicatorSubsanationEligibilityResponseDto();
        dto.setCanSubsanate(eligibility.canSubsanate());
        dto.setOpenObservationId(eligibility.openObservationId());
        dto.setReason(eligibility.reason());
        return ResponseEntity.ok(dto);
    }

    @PostMapping(value = "/evidences/{evidenceId}/subsanate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CC')")
    @Operation(summary = "Subsanar evidencia del indicador (una vez por observación OPEN)")
    public ResponseEntity<SubsanateNormativeIndicatorEvidenceResponseDto> subsanateEvidence(
            @PathVariable UUID indicatorId,
            @PathVariable UUID evidenceId,
            @RequestPart("file") MultipartFile file,
            @RequestPart("description") String description,
            @RequestPart("observationId") UUID observationId,
            Authentication authentication) throws IOException {
        UUID uploadedBy = (UUID) authentication.getPrincipal();
        NormativeIndicatorEvidenceSubsanationCommand command = new NormativeIndicatorEvidenceSubsanationCommand(
                indicatorId,
                evidenceId,
                observationId,
                description,
                file.getBytes(),
                file.getContentType(),
                file.getOriginalFilename(),
                uploadedBy);
        EvidenceSubsanationResult result = subsanateUseCase.subsanate(command);
        SubsanateNormativeIndicatorEvidenceResponseDto dto = new SubsanateNormativeIndicatorEvidenceResponseDto();
        dto.setEvidenceId(result.evidenceId());
        dto.setVersion(result.version());
        dto.setObservationId(result.observationId());
        dto.setSupersedesVersion(result.supersedesVersion());
        dto.setContentHash(result.contentHash());
        dto.setEvent(result.event());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    private static List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .toList();
    }
}
