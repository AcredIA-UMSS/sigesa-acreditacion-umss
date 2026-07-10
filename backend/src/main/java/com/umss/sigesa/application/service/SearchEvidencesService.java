package com.umss.sigesa.application.service;

import com.umss.sigesa.application.port.in.SearchEvidencesUseCase;
import com.umss.sigesa.application.port.out.EvidenceRepositoryPort;
import com.umss.sigesa.application.port.out.IndicatorCatalogPort;
import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.EvidenceVersion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class SearchEvidencesService implements SearchEvidencesUseCase {

    private final EvidenceRepositoryPort evidenceRepository;
    private final IndicatorCatalogPort indicatorCatalog;

    public SearchEvidencesService(EvidenceRepositoryPort evidenceRepository,
                                  IndicatorCatalogPort indicatorCatalog) {
        this.evidenceRepository = evidenceRepository;
        this.indicatorCatalog = indicatorCatalog;
    }

    @Override
    @Transactional(readOnly = true)
    public EvidenceSearchPage search(EvidenceSearchQuery query) {
        int page = Math.max(query.page(), 0);
        int size = query.size() <= 0 ? 10 : Math.min(query.size(), 50);

        List<EvidenceSearchItem> filtered = evidenceRepository.findAll().stream()
                .map(this::toSearchItem)
                .flatMap(java.util.Optional::stream)
                .filter(item -> matchesFilters(item, query))
                .sorted(Comparator.comparing(EvidenceSearchItem::createdAt).reversed())
                .toList();

        long totalElements = filtered.size();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());

        return new EvidenceSearchPage(
                filtered.subList(fromIndex, toIndex),
                page,
                size,
                totalElements,
                totalPages
        );
    }

    private java.util.Optional<EvidenceSearchItem> toSearchItem(Evidence evidence) {
        return indicatorCatalog.findById(evidence.getIndicatorId())
                .flatMap(indicator -> evidenceRepository.findVersionById(evidence.getLatestVersionId())
                        .map(latestVersion -> new EvidenceSearchItem(
                                evidence.getId(),
                                evidence.getIndicatorId(),
                                indicator.code(),
                                indicator.title(),
                                indicator.programId(),
                                indicator.phaseId(),
                                latestVersion.getVersionNumber(),
                                latestVersion.getDescription(),
                                evidence.getCreatedAt()
                        )));
    }

    private boolean matchesFilters(EvidenceSearchItem item, EvidenceSearchQuery query) {
        if (query.programId() != null && !query.programId().equals(item.programId())) {
            return false;
        }
        if (query.phaseId() != null && query.phaseId() != item.phaseId()) {
            return false;
        }
        if (query.indicatorId() != null && !query.indicatorId().equals(item.indicatorId())) {
            return false;
        }
        if (query.allowedProgramIds() != null
                && !query.allowedProgramIds().isEmpty()
                && !query.allowedProgramIds().contains(item.programId())) {
            return false;
        }
        if (query.query() != null && !query.query().isBlank()) {
            String normalized = query.query().trim().toLowerCase(Locale.ROOT);
            String haystack = ((item.indicatorCode() + " " + item.indicatorTitle() + " "
                    + (item.description() != null ? item.description() : "")).toLowerCase(Locale.ROOT));
            return haystack.contains(normalized);
        }
        return true;
    }
}
