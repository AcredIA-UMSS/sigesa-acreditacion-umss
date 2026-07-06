package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.ProgramSummaryResponse;
import com.umss.sigesa.application.port.in.ListProgramsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/programs")
public class ProgramCatalogController {

    private final ListProgramsUseCase listProgramsUseCase;

    public ProgramCatalogController(ListProgramsUseCase listProgramsUseCase) {
        this.listProgramsUseCase = listProgramsUseCase;
    }

    @GetMapping
    public List<ProgramSummaryResponse> list() {
        return listProgramsUseCase.list().stream()
                .map(summary -> new ProgramSummaryResponse(summary.id(), summary.code(), summary.name()))
                .toList();
    }
}
