package com.umss.sigesa.adapter.in.web.dto;

import java.util.List;

public record SearchSubsetDto(
        String label,
        List<EvidenceSearchDetailDto> results
) {}
