package com.umss.sigesa.application.port.out;

import com.umss.sigesa.adapter.in.web.dto.EvidenceSearchDetailDto;
import com.umss.sigesa.application.model.evidence.SearchFilters;
import java.util.List;
import java.util.UUID;

public interface SearchEvidenceQueryPort {
    List<EvidenceSearchDetailDto> executeSearch(SearchFilters filters, List<UUID> programScope);
    java.util.Optional<EvidenceSearchDetailDto> findVersionById(UUID versionId, List<UUID> programScope);
    String getLastExecutedSql();
}
