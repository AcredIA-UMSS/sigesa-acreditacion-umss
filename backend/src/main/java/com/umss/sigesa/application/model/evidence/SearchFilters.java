package com.umss.sigesa.application.model.evidence;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class SearchFilters {
    private final String termino;
    private final String dimension;
    private final String criterioCodigo;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
}
