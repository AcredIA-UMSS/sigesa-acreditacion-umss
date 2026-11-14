package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "Resultado de importación FODA → Plan de Mejoras (API-WF-15)")
public class ImportImprovementPlanResponseDto {

    private int importedCount;
    private int skippedCount;
    private List<ImprovementPlanItemResponseDto> items;
}
