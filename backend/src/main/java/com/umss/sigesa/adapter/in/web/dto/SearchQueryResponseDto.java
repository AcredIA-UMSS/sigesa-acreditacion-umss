package com.umss.sigesa.adapter.in.web.dto;

import java.util.List;

public record SearchQueryResponseDto(
        String query,
        String routingPath,
        List<SearchSubsetDto> subsets,
        String message,
        String llmThought
) {}
