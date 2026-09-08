package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.NormativeIndicatorEvidenceResponseDto;
import com.umss.sigesa.adapter.in.web.dto.UploadEvidenceResponse;
import com.umss.sigesa.application.port.in.ListNormativeIndicatorEvidencesUseCase;
import com.umss.sigesa.application.port.in.UploadNormativeIndicatorEvidenceUseCase;
import com.umss.sigesa.domain.model.NormativeIndicatorEvidenceItem;
import com.umss.sigesa.domain.model.NormativeIndicatorEvidenceUploadCommand;
import com.umss.sigesa.domain.model.NormativeIndicatorEvidenceUploadResult;
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
@RequestMapping("/api/v1/indicators/{indicatorId}/evidences")
@Tag(name = "Evidence", description = "Carga y consulta de evidencias por indicador normativo v2 (API-EVD-01)")
public class EvidenceController {

    private final UploadNormativeIndicatorEvidenceUseCase uploadUseCase;
    private final ListNormativeIndicatorEvidencesUseCase listUseCase;

    public EvidenceController(
            UploadNormativeIndicatorEvidenceUseCase uploadUseCase,
            ListNormativeIndicatorEvidencesUseCase listUseCase) {
        this.uploadUseCase = uploadUseCase;
        this.listUseCase = listUseCase;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CC')")
    @Operation(summary = "Cargar evidencia en indicador normativo (API-EVD-01)")
    public ResponseEntity<UploadEvidenceResponse> uploadEvidence(
            @PathVariable UUID indicatorId,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "externalUrl", required = false) String externalUrl,
            @RequestPart("description") String description,
            Authentication authentication) throws IOException {

        UUID uploadedBy = (UUID) authentication.getPrincipal();
        byte[] fileContent = file != null ? file.getBytes() : null;
        String contentType = file != null ? file.getContentType() : null;
        String originalFilename = file != null ? file.getOriginalFilename() : null;

        NormativeIndicatorEvidenceUploadCommand command = new NormativeIndicatorEvidenceUploadCommand(
                indicatorId,
                description,
                fileContent,
                contentType,
                originalFilename,
                externalUrl,
                uploadedBy);

        NormativeIndicatorEvidenceUploadResult result = uploadUseCase.upload(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UploadEvidenceResponse(
                result.evidenceId(),
                result.version(),
                result.contentHash(),
                result.event(),
                result.indicatorState().name()));
    }

    @GetMapping
    @Operation(summary = "Listar evidencias cargadas en el indicador normativo")
    public ResponseEntity<List<NormativeIndicatorEvidenceResponseDto>> listEvidences(
            @PathVariable UUID indicatorId,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        List<NormativeIndicatorEvidenceResponseDto> items = listUseCase.list(indicatorId, userId, roles).stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(items);
    }

    private NormativeIndicatorEvidenceResponseDto toDto(NormativeIndicatorEvidenceItem item) {
        NormativeIndicatorEvidenceResponseDto dto = new NormativeIndicatorEvidenceResponseDto();
        dto.setEvidenceId(item.evidenceId());
        dto.setIndicatorId(item.indicatorId());
        dto.setVersion(item.version());
        dto.setDescription(item.description());
        dto.setContentHash(item.contentHash());
        dto.setOriginalFilename(item.originalFilename());
        dto.setExternalUrl(item.externalUrl());
        dto.setUploadedAt(item.uploadedAt());
        dto.setUploadedBy(item.uploadedBy());
        return dto;
    }

    private static List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .toList();
    }
}
