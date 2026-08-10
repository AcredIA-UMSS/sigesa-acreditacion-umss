package com.umss.sigesa.adapter.in.web.dto;

import java.util.List;

public record SearchQueryResponseDto(
        String query,
        String routingPath,
        String toolUsed,
        String dataSource,
        String message,
        List<EvidenceSearchDetailDto> results,
        String sqlQuery
) {}
