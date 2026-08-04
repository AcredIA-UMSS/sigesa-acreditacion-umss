package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.ProgramSummaryResponse;
import com.umss.sigesa.application.port.in.ListProgramsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/programs")
@Tag(name = "Programas", description = "Catálogo de carreras UMSS")
public class ProgramCatalogController {

    private final ListProgramsUseCase listProgramsUseCase;

    public ProgramCatalogController(ListProgramsUseCase listProgramsUseCase) {
        this.listProgramsUseCase = listProgramsUseCase;
    }

    @GetMapping
    @Operation(summary = "Listar carreras", description = "Devuelve carreras activas. Use q para autocompletar por nombre o código.")
    public List<ProgramSummaryResponse> list(
            @Parameter(description = "Texto de búsqueda (nombre o código)")
            @RequestParam(required = false) String q) {
        return listProgramsUseCase.list(q).stream()
                .map(summary -> new ProgramSummaryResponse(summary.id(), summary.code(), summary.name()))
                .toList();
    }
}
