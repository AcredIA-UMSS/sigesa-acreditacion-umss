package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.IndicatorSummaryResponse;
import com.umss.sigesa.application.port.in.ListIndicatorsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/indicators")
public class IndicatorCatalogController {

    private final ListIndicatorsUseCase listIndicatorsUseCase;
    private final WebIdentityResolver identityResolver;

    public IndicatorCatalogController(ListIndicatorsUseCase listIndicatorsUseCase,
                                      WebIdentityResolver identityResolver) {
        this.listIndicatorsUseCase = listIndicatorsUseCase;
        this.identityResolver = identityResolver;
    }

    @GetMapping
    public List<IndicatorSummaryResponse> list(
            @RequestParam(required = false) UUID programId,
            @RequestParam(required = false) Integer phaseId) {
        List<UUID> programScope = identityResolver.programScopeForCurrentUser();

        return listIndicatorsUseCase.list(programId, phaseId, programScope).stream()
                .map(summary -> new IndicatorSummaryResponse(
                        summary.id(),
                        summary.code(),
                        summary.title(),
                        summary.programId(),
                        summary.phaseId(),
                        summary.criterionId(),
                        summary.currentState()
                ))
                .toList();
    }
}
