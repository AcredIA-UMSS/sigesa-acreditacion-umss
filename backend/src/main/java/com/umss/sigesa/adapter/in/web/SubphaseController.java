package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.CreateSubphaseObservationRequestDto;
import com.umss.sigesa.adapter.in.web.dto.SubphaseEvidenceResponseDto;
import com.umss.sigesa.adapter.in.web.dto.SubphaseObservationResponseDto;
import com.umss.sigesa.adapter.in.web.dto.UploadEvidenceResponse;
import com.umss.sigesa.adapter.in.web.dto.SubphaseTransitionResponseDto;
import com.umss.sigesa.adapter.in.web.dto.RejectIndicatorRequestDto;
import com.umss.sigesa.adapter.in.web.dto.SubphaseApproveResponseDto;
import com.umss.sigesa.adapter.in.web.dto.SubphaseRejectResponseDto;
import com.umss.sigesa.application.port.in.AddSubphaseObservationUseCase;
import com.umss.sigesa.application.port.in.ApproveSubphaseIndicatorUseCase;
import com.umss.sigesa.application.port.in.RejectSubphaseIndicatorUseCase;
import com.umss.sigesa.application.port.in.GetSubphaseSubsanationEligibilityUseCase;
import com.umss.sigesa.application.port.in.ListSubphaseEvidencesUseCase;
import com.umss.sigesa.application.port.in.ListSubphaseObservationsUseCase;
import com.umss.sigesa.application.port.in.SubsanateSubphaseEvidenceUseCase;
import com.umss.sigesa.application.port.in.UploadSubphaseEvidenceUseCase;
import com.umss.sigesa.adapter.in.web.dto.SubphaseSubsanationEligibilityResponseDto;
import com.umss.sigesa.adapter.in.web.dto.SubsanateSubphaseEvidenceResponseDto;
import com.umss.sigesa.domain.model.EvidenceSubsanationResult;
import com.umss.sigesa.domain.model.EvidenceUploadResult;
import com.umss.sigesa.domain.model.SubphaseEvidenceSubsanationCommand;
import com.umss.sigesa.domain.model.SubphaseSubsanationEligibility;
import com.umss.sigesa.domain.model.SubphaseEvidenceItem;
import com.umss.sigesa.domain.model.SubphaseEvidenceUploadCommand;
import com.umss.sigesa.domain.model.SubphaseTransitionResult;
import com.umss.sigesa.domain.model.SubphaseApproveResult;
import com.umss.sigesa.domain.model.SubphaseObservation;
import com.umss.sigesa.domain.model.SubphaseRejectResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subphases/{subphaseId}")
@Tag(name = "Subfase", description = "Evidencias múltiples y observaciones por subfase")
public class SubphaseController {

    private final UploadSubphaseEvidenceUseCase uploadSubphaseEvidenceUseCase;
    private final ListSubphaseEvidencesUseCase listSubphaseEvidencesUseCase;
    private final ListSubphaseObservationsUseCase listSubphaseObservationsUseCase;
    private final AddSubphaseObservationUseCase addSubphaseObservationUseCase;
    private final SubsanateSubphaseEvidenceUseCase subsanateSubphaseEvidenceUseCase;
    private final GetSubphaseSubsanationEligibilityUseCase subsanationEligibilityUseCase;
    private final RejectSubphaseIndicatorUseCase rejectSubphaseIndicatorUseCase;
    private final ApproveSubphaseIndicatorUseCase approveSubphaseIndicatorUseCase;

    public SubphaseController(UploadSubphaseEvidenceUseCase uploadSubphaseEvidenceUseCase,
                              ListSubphaseEvidencesUseCase listSubphaseEvidencesUseCase,
                              ListSubphaseObservationsUseCase listSubphaseObservationsUseCase,
                              AddSubphaseObservationUseCase addSubphaseObservationUseCase,
                              SubsanateSubphaseEvidenceUseCase subsanateSubphaseEvidenceUseCase,
                              GetSubphaseSubsanationEligibilityUseCase subsanationEligibilityUseCase,
                              RejectSubphaseIndicatorUseCase rejectSubphaseIndicatorUseCase,
                              ApproveSubphaseIndicatorUseCase approveSubphaseIndicatorUseCase) {
        this.uploadSubphaseEvidenceUseCase = uploadSubphaseEvidenceUseCase;
        this.listSubphaseEvidencesUseCase = listSubphaseEvidencesUseCase;
        this.listSubphaseObservationsUseCase = listSubphaseObservationsUseCase;
        this.addSubphaseObservationUseCase = addSubphaseObservationUseCase;
        this.subsanateSubphaseEvidenceUseCase = subsanateSubphaseEvidenceUseCase;
        this.subsanationEligibilityUseCase = subsanationEligibilityUseCase;
        this.rejectSubphaseIndicatorUseCase = rejectSubphaseIndicatorUseCase;
        this.approveSubphaseIndicatorUseCase = approveSubphaseIndicatorUseCase;
    }

    @PostMapping(value = "/evidences", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CC')")
    @Operation(summary = "Cargar evidencia en subfase (1..N por subfase)")
    public ResponseEntity<UploadEvidenceResponse> uploadEvidence(
            @PathVariable UUID subphaseId,
            @RequestPart("file") MultipartFile file,
            @RequestPart("description") String description,
            Authentication authentication) throws IOException {

        UUID uploadedBy = (UUID) authentication.getPrincipal();
        SubphaseEvidenceUploadCommand command = new SubphaseEvidenceUploadCommand(
                subphaseId,
                description,
                file.getBytes(),
                file.getContentType(),
                file.getOriginalFilename(),
                uploadedBy);

        EvidenceUploadResult result = uploadSubphaseEvidenceUseCase.upload(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UploadEvidenceResponse(
                result.evidenceId(),
                result.version(),
                result.contentHash(),
                result.event(),
                result.currentState().name()));
    }

    @GetMapping("/evidences")
    @Operation(summary = "Listar evidencias cargadas en la subfase")
    public ResponseEntity<List<SubphaseEvidenceResponseDto>> listEvidences(
            @PathVariable UUID subphaseId,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        List<SubphaseEvidenceResponseDto> items = listSubphaseEvidencesUseCase
                .list(subphaseId, userId, roles).stream()
                .map(this::toEvidenceDto)
                .toList();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/observations")
    @Operation(summary = "Listar observaciones de la subfase")
    public ResponseEntity<List<SubphaseObservationResponseDto>> listObservations(
            @PathVariable UUID subphaseId,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        List<SubphaseObservationResponseDto> items = listSubphaseObservationsUseCase
                .list(subphaseId, userId, roles).stream()
                .map(this::toObservationDto)
                .toList();
        return ResponseEntity.ok(items);
    }

    @PostMapping("/observations")
    @PreAuthorize("hasAnyRole('TD','JD')")
    @Operation(summary = "Registrar observación sobre evidencias de la subfase")
    public ResponseEntity<SubphaseObservationResponseDto> addObservation(
            @PathVariable UUID subphaseId,
            @Valid @RequestBody CreateSubphaseObservationRequestDto request,
            Authentication authentication) {
        UUID authorId = (UUID) authentication.getPrincipal();
        String role = extractPrimaryRole(authentication);
        SubphaseObservation saved = addSubphaseObservationUseCase.add(
                subphaseId, request.getBody(), authorId, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(toObservationDto(saved));
    }

    @GetMapping("/subsanation-eligibility")
    @Operation(summary = "Verificar si la subfase permite subsanación")
    public ResponseEntity<SubphaseSubsanationEligibilityResponseDto> subsanationEligibility(
            @PathVariable UUID subphaseId,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        SubphaseSubsanationEligibility eligibility = subsanationEligibilityUseCase.get(
                subphaseId, userId, extractRoles(authentication));
        SubphaseSubsanationEligibilityResponseDto dto = new SubphaseSubsanationEligibilityResponseDto();
        dto.setCanSubsanate(eligibility.canSubsanate());
        dto.setOpenObservationId(eligibility.openObservationId());
        dto.setReason(eligibility.reason());
        return ResponseEntity.ok(dto);
    }

    @PostMapping(value = "/evidences/{evidenceId}/subsanate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CC')")
    @Operation(summary = "Subsanar evidencia (una vez por observación OPEN)")
    public ResponseEntity<SubsanateSubphaseEvidenceResponseDto> subsanateEvidence(
            @PathVariable UUID subphaseId,
            @PathVariable UUID evidenceId,
            @RequestPart("file") MultipartFile file,
            @RequestPart("description") String description,
            @RequestPart("observationId") UUID observationId,
            Authentication authentication) throws IOException {
        UUID uploadedBy = (UUID) authentication.getPrincipal();
        SubphaseEvidenceSubsanationCommand command = new SubphaseEvidenceSubsanationCommand(
                subphaseId,
                evidenceId,
                observationId,
                description,
                file.getBytes(),
                file.getContentType(),
                file.getOriginalFilename(),
                uploadedBy);
        EvidenceSubsanationResult result = subsanateSubphaseEvidenceUseCase.subsanate(command);
        SubsanateSubphaseEvidenceResponseDto dto = new SubsanateSubphaseEvidenceResponseDto();
        dto.setEvidenceId(result.evidenceId());
        dto.setVersion(result.version());
        dto.setObservationId(result.observationId());
        dto.setSupersedesVersion(result.supersedesVersion());
        dto.setContentHash(result.contentHash());
        dto.setEvent(result.event());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/reject")
    @PreAuthorize("hasRole('TD')")
    @Operation(summary = "Rechazar subfase (requiere evidencia)")
    public ResponseEntity<SubphaseRejectResponseDto> rejectSubphase(
            @PathVariable UUID subphaseId,
            @Valid @RequestBody RejectIndicatorRequestDto request,
            Authentication authentication) {
        UUID actorId = (UUID) authentication.getPrincipal();
        SubphaseRejectResult result = rejectSubphaseIndicatorUseCase.reject(
                subphaseId, request.getJustification(), actorId, extractPrimaryRole(authentication));
        return ResponseEntity.ok(toRejectDto(result));
    }

    @PostMapping("/approve")
    @PreAuthorize("hasRole('TD')")
    @Operation(summary = "Aprobar subfase (requiere evidencia)")
    public ResponseEntity<SubphaseApproveResponseDto> approveSubphase(
            @PathVariable UUID subphaseId,
            Authentication authentication) {
        UUID actorId = (UUID) authentication.getPrincipal();
        SubphaseApproveResult result = approveSubphaseIndicatorUseCase.approve(
                subphaseId, actorId, extractPrimaryRole(authentication));
        return ResponseEntity.ok(toApproveDto(result));
    }

    private SubphaseEvidenceResponseDto toEvidenceDto(SubphaseEvidenceItem item) {
        SubphaseEvidenceResponseDto dto = new SubphaseEvidenceResponseDto();
        dto.setEvidenceId(item.evidenceId());
        dto.setSubphaseId(item.subphaseId());
        dto.setIndicatorId(item.indicatorId());
        dto.setVersion(item.version());
        dto.setDescription(item.description());
        dto.setContentHash(item.contentHash());
        dto.setOriginalFilename(item.originalFilename());
        dto.setUploadedAt(item.uploadedAt());
        dto.setUploadedBy(item.uploadedBy());
        return dto;
    }

    private static SubphaseRejectResponseDto toRejectDto(SubphaseRejectResult result) {
        SubphaseRejectResponseDto dto = new SubphaseRejectResponseDto();
        dto.setSubphaseId(result.subphaseId());
        dto.setObservationId(result.observationId());
        dto.setTransition(toTransitionDto(result.transition()));
        return dto;
    }

    private static SubphaseApproveResponseDto toApproveDto(SubphaseApproveResult result) {
        SubphaseApproveResponseDto dto = new SubphaseApproveResponseDto();
        dto.setSubphaseId(result.subphaseId());
        dto.setTransition(toTransitionDto(result.transition()));
        return dto;
    }

    private static SubphaseTransitionResponseDto toTransitionDto(SubphaseTransitionResult transition) {
        SubphaseTransitionResponseDto dto = new SubphaseTransitionResponseDto();
        dto.setSubphaseId(transition.subphaseId());
        dto.setPreviousState(transition.previousState().name());
        dto.setNewState(transition.newState().name());
        return dto;
    }

    private SubphaseObservationResponseDto toObservationDto(SubphaseObservation observation) {
        SubphaseObservationResponseDto dto = new SubphaseObservationResponseDto();
        dto.setId(observation.getId());
        dto.setSubphaseId(observation.getSubphaseId());
        dto.setAuthorId(observation.getAuthorId());
        dto.setAuthorRole(observation.getAuthorRole());
        dto.setBody(observation.getBody());
        dto.setStatus(observation.getStatus() != null ? observation.getStatus().name() : "OPEN");
        dto.setResolvedAt(observation.getResolvedAt());
        dto.setResolvedVersionId(observation.getResolvedVersionId());
        dto.setCreatedAt(observation.getCreatedAt());
        dto.setUpdatedAt(observation.getUpdatedAt());
        return dto;
    }

    private static List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.startsWith("ROLE_") ? auth.substring(5) : auth)
                .toList();
    }

    private static String extractPrimaryRole(Authentication authentication) {
        return extractRoles(authentication).stream()
                .filter(r -> "TD".equals(r) || "JD".equals(r))
                .findFirst()
                .orElse(extractRoles(authentication).isEmpty() ? "" : extractRoles(authentication).getFirst());
    }
}
