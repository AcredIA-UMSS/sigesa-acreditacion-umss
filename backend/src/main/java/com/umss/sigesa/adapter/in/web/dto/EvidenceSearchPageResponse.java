package com.umss.sigesa.adapter.in.web.dto;

import java.util.List;

public record EvidenceSearchPageResponse(
        List<EvidenceSearchItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
