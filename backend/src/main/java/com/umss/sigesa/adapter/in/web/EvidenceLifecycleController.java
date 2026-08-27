package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.EvidenceVersionHistoryResponseDto;
import com.umss.sigesa.application.port.in.AttemptDeleteEvidenceUseCase;
import com.umss.sigesa.application.port.in.ListEvidenceVersionsUseCase;
import com.umss.sigesa.domain.model.EvidenceVersionHistoryItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/evidences/{evidenceId}")
@Tag(name = "Evidence lifecycle", description = "Historial de versiones y bloqueo de borrado (FSD-UC-005)")
public class EvidenceLifecycleController {

    private final ListEvidenceVersionsUseCase listEvidenceVersionsUseCase;
    private final AttemptDeleteEvidenceUseCase attemptDeleteEvidenceUseCase;

    public EvidenceLifecycleController(ListEvidenceVersionsUseCase listEvidenceVersionsUseCase,
                                       AttemptDeleteEvidenceUseCase attemptDeleteEvidenceUseCase) {
        this.listEvidenceVersionsUseCase = listEvidenceVersionsUseCase;
        this.attemptDeleteEvidenceUseCase = attemptDeleteEvidenceUseCase;
    }

    @GetMapping("/versions")
    @Operation(summary = "Listar historial de versiones de una evidencia")
    public ResponseEntity<List<EvidenceVersionHistoryResponseDto>> listVersions(
            @PathVariable UUID evidenceId,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        List<EvidenceVersionHistoryResponseDto> items = listEvidenceVersionsUseCase
                .list(evidenceId, userId, roles).stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(items);
    }

    @DeleteMapping
    @Operation(summary = "Intento de borrado (siempre rechazado si existe — append-only)")
    public ResponseEntity<Void> delete(
            @PathVariable UUID evidenceId,
            Authentication authentication) {
        UUID actorId = (UUID) authentication.getPrincipal();
        attemptDeleteEvidenceUseCase.attemptDelete(evidenceId, actorId, extractRoles(authentication));
        return ResponseEntity.noContent().build();
    }

    private EvidenceVersionHistoryResponseDto toDto(EvidenceVersionHistoryItem item) {
        EvidenceVersionHistoryResponseDto dto = new EvidenceVersionHistoryResponseDto();
        dto.setVersionId(item.versionId());
        dto.setVersion(item.version());
        dto.setSupersedesVersion(item.supersedesVersion());
        dto.setObservationId(item.observationId());
        dto.setDescription(item.description());
        dto.setContentHash(item.contentHash());
        dto.setOriginalFilename(item.originalFilename());
        dto.setCreatedBy(item.createdBy());
        dto.setCreatedAt(item.createdAt());
        dto.setCurrent(item.current());
        dto.setBlobAvailable(item.blobAvailable());
        return dto;
    }

    private static List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.startsWith("ROLE_") ? auth.substring(5) : auth)
                .toList();
    }
}
