package com.umss.sigesa.adapter.in.web.dto;

import com.umss.sigesa.domain.model.CoverageMatrix;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CoverageMatrixResponseDto(
        UUID processId,
        String careerName,
        Instant generatedAt,
        CoverageMatrix.Totals totals,
        List<CoverageMatrix.DimensionNode> dimensions
) {

    public static CoverageMatrixResponseDto from(CoverageMatrix matrix) {
        return new CoverageMatrixResponseDto(
                matrix.processId(),
                matrix.careerName(),
                matrix.generatedAt(),
                matrix.totals(),
                matrix.dimensions());
    }
}
