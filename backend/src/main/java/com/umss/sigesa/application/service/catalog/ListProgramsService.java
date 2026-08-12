package com.umss.sigesa.application.service.catalog;

import com.umss.sigesa.application.port.in.ListProgramsUseCase;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;

import java.util.List;

public class ListProgramsService implements ListProgramsUseCase {

    private final ProgramCatalogPort programCatalogPort;

    public ListProgramsService(ProgramCatalogPort programCatalogPort) {
        this.programCatalogPort = programCatalogPort;
    }

    @Override
    public List<ProgramSummary> list(String query) {
        return programCatalogPort.search(query).stream()
                .map(entry -> new ProgramSummary(entry.id(), entry.code(), entry.name()))
                .toList();
    }
}
