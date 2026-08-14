package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.SearchQueryResponseDto;
import com.umss.sigesa.application.port.in.SearchEvidenceUseCase;
import com.umss.sigesa.application.port.in.DownloadEvidenceUseCase;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/evidences")
@Tag(name = "Evidence Search", description = "Búsqueda inteligente de evidencias")
public class SearchEvidenceController {

    private final SearchEvidenceUseCase searchEvidenceUseCase;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;
    private final DownloadEvidenceUseCase downloadEvidenceUseCase;

    public SearchEvidenceController(SearchEvidenceUseCase searchEvidenceUseCase,
                                    UserProgramAssignmentRepositoryPort assignmentRepository,
                                    DownloadEvidenceUseCase downloadEvidenceUseCase) {
        this.searchEvidenceUseCase = searchEvidenceUseCase;
        this.assignmentRepository = assignmentRepository;
        this.downloadEvidenceUseCase = downloadEvidenceUseCase;
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar evidencias con IA o búsqueda tradicional", description = "Enruta la búsqueda por sinónimos usando LLM y aplica aislamiento de carrera.")
    public ResponseEntity<SearchQueryResponseDto> search(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestHeader(value = "X-AI-Enabled", defaultValue = "true") boolean xAiEnabled) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = extractUserId(auth);
        String role = extractRole(auth);
        List<UUID> programScope = extractProgramScope(userId);

        SearchQueryResponseDto response = searchEvidenceUseCase.search(query, xAiEnabled, userId, role, programScope);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{versionId}/download")
    @Operation(summary = "Descargar un archivo de evidencia", description = "Valida el acceso por rol y carrera antes de retornar los bytes del archivo.")
    public ResponseEntity<Resource> download(@PathVariable UUID versionId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = extractUserId(auth);
        String role = extractRole(auth);
        List<UUID> programScope = extractProgramScope(userId);

        DownloadEvidenceUseCase.EvidenceFileResult fileResult = downloadEvidenceUseCase.download(
                versionId, userId, role, programScope
        );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileResult.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResult.filename() + "\"")
                .body(new ByteArrayResource(fileResult.content()));
    }

    private static UUID extractUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("Usuario no autenticado.");
        }
        if (auth.getPrincipal() instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(auth.getPrincipal().toString());
    }

    private static String extractRole(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) {
            throw new IllegalStateException("Usuario sin rol asignado.");
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.startsWith("ROLE_") ? authority.substring("ROLE_".length()) : authority)
                .findFirst()
                .orElse("CC");
    }

    private List<UUID> extractProgramScope(UUID userId) {
        return assignmentRepository.findActiveByUserId(userId).stream()
                .map(UserProgramAssignment::getProgramId)
                .toList();
    }
}
