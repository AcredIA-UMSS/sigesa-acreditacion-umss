package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.EvidenceSearchHitResponseDto;
import com.umss.sigesa.adapter.in.web.dto.EvidenceSearchPageResponseDto;
import com.umss.sigesa.application.port.in.SearchEvidencesUseCase;
import com.umss.sigesa.domain.model.EvidenceSearchCriteria;
import com.umss.sigesa.domain.model.EvidenceSearchHit;
import com.umss.sigesa.domain.model.EvidenceSearchPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/evidences")
@Tag(name = "Evidence search", description = "Búsqueda multifiltro de evidencias (FSD-UC-007)")
public class EvidenceSearchController {

    private final SearchEvidencesUseCase searchEvidencesUseCase;

    public EvidenceSearchController(SearchEvidencesUseCase searchEvidencesUseCase) {
        this.searchEvidencesUseCase = searchEvidencesUseCase;
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('CC','TD','JD')")
    @Operation(summary = "Buscar evidencias con filtros y paginación")
    public ResponseEntity<EvidenceSearchPageResponseDto> search(
            @RequestParam(required = false) UUID processId,
            @RequestParam(required = false) UUID phaseId,
            @RequestParam(required = false) UUID subphaseId,
            @RequestParam(required = false) UUID indicatorId,
            @RequestParam(required = false) UUID programId,
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) Integer managementYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        UUID requesterId = (UUID) authentication.getPrincipal();
        EvidenceSearchCriteria criteria = new EvidenceSearchCriteria(
                processId,
                phaseId,
                subphaseId,
                indicatorId,
                programId,
                query,
                managementYear,
                page,
                size);
        EvidenceSearchPage result = searchEvidencesUseCase.search(
                criteria, requesterId, extractRoles(authentication));
        return ResponseEntity.ok(toDto(result));
    }

    private EvidenceSearchPageResponseDto toDto(EvidenceSearchPage page) {
        EvidenceSearchPageResponseDto dto = new EvidenceSearchPageResponseDto();
        dto.setItems(page.items().stream().map(this::toHitDto).toList());
        dto.setTotal(page.total());
        dto.setPage(page.page());
        dto.setSize(page.size());
        return dto;
    }

    private EvidenceSearchHitResponseDto toHitDto(EvidenceSearchHit hit) {
        EvidenceSearchHitResponseDto dto = new EvidenceSearchHitResponseDto();
        dto.setEvidenceId(hit.evidenceId());
        dto.setSubphaseId(hit.subphaseId());
        dto.setSubphaseName(hit.subphaseName());
        dto.setPhaseId(hit.phaseId());
        dto.setPhaseName(hit.phaseName());
        dto.setProcessId(hit.processId());
        dto.setIndicatorId(hit.indicatorId());
        dto.setIndicatorCode(hit.indicatorCode());
        dto.setIndicatorTitle(hit.indicatorTitle());
        dto.setVersion(hit.version());
        dto.setDescription(hit.description());
        dto.setOriginalFilename(hit.originalFilename());
        dto.setUploadedAt(hit.uploadedAt());
        dto.setUploadedBy(hit.uploadedBy());
        dto.setBlobAvailable(hit.blobAvailable());
        return dto;
    }

    private static List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.startsWith("ROLE_") ? auth.substring(5) : auth)
                .toList();
    }
}
