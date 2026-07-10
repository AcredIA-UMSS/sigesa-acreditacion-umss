package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.EvidenceSearchItemResponse;
import com.umss.sigesa.adapter.in.web.dto.EvidenceSearchPageResponse;
import com.umss.sigesa.adapter.in.web.dto.EvidenceVersionSummaryResponse;
import com.umss.sigesa.application.port.in.ListEvidenceVersionsUseCase;
import com.umss.sigesa.application.port.in.SearchEvidencesUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/evidences")
public class EvidenceQueryController {

    private final SearchEvidencesUseCase searchEvidencesUseCase;
    private final ListEvidenceVersionsUseCase listEvidenceVersionsUseCase;
    private final WebIdentityResolver identityResolver;

    public EvidenceQueryController(SearchEvidencesUseCase searchEvidencesUseCase,
                                   ListEvidenceVersionsUseCase listEvidenceVersionsUseCase,
                                   WebIdentityResolver identityResolver) {
        this.searchEvidencesUseCase = searchEvidencesUseCase;
        this.listEvidenceVersionsUseCase = listEvidenceVersionsUseCase;
        this.identityResolver = identityResolver;
    }

    @GetMapping("/search")
    public EvidenceSearchPageResponse search(
            @RequestParam(required = false) UUID programId,
            @RequestParam(required = false) Integer phaseId,
            @RequestParam(required = false) UUID indicatorId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        SearchEvidencesUseCase.EvidenceSearchPage result = searchEvidencesUseCase.search(
                new SearchEvidencesUseCase.EvidenceSearchQuery(
                        programId,
                        phaseId,
                        indicatorId,
                        q,
                        identityResolver.programScopeForCurrentUser(),
                        page,
                        size
                )
        );

        return new EvidenceSearchPageResponse(
                result.content().stream()
                        .map(item -> new EvidenceSearchItemResponse(
                                item.evidenceId(),
                                item.indicatorId(),
                                item.indicatorCode(),
                                item.indicatorTitle(),
                                item.programId(),
                                item.phaseId(),
                                item.latestVersion(),
                                item.description(),
                                item.createdAt()
                        ))
                        .toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }

    @GetMapping("/{id}/versions")
    public List<EvidenceVersionSummaryResponse> listVersions(@PathVariable UUID id) {
        return listEvidenceVersionsUseCase.list(id, identityResolver.programScopeForCurrentUser()).stream()
                .map(version -> new EvidenceVersionSummaryResponse(
                        version.version(),
                        version.supersedesId(),
                        version.observationId(),
                        version.createdAt(),
                        version.createdBy()
                ))
                .toList();
    }
}
