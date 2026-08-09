package com.umss.sigesa.application.port.in;

import com.umss.sigesa.adapter.in.web.dto.SearchQueryResponseDto;
import java.util.List;
import java.util.UUID;

public interface SearchEvidenceUseCase {
    SearchQueryResponseDto search(String query, boolean xAiEnabled, UUID userId, String role, List<UUID> programScope);
}
