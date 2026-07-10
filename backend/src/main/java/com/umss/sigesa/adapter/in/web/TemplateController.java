package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.ActivateTemplateRequest;
import com.umss.sigesa.adapter.in.web.dto.ActivateTemplateResponse;
import com.umss.sigesa.adapter.in.web.dto.TemplateSummaryResponse;
import com.umss.sigesa.application.port.in.ActivateTemplateUseCase;
import com.umss.sigesa.application.port.in.ListTemplatesUseCase;
import com.umss.sigesa.domain.model.Template;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/templates")
public class TemplateController {

    private final ListTemplatesUseCase listTemplatesUseCase;
    private final ActivateTemplateUseCase activateTemplateUseCase;

    public TemplateController(ListTemplatesUseCase listTemplatesUseCase,
                              ActivateTemplateUseCase activateTemplateUseCase) {
        this.listTemplatesUseCase = listTemplatesUseCase;
        this.activateTemplateUseCase = activateTemplateUseCase;
    }

    @GetMapping
    public List<TemplateSummaryResponse> list() {
        return listTemplatesUseCase.list().stream()
                .map(summary -> new TemplateSummaryResponse(
                        summary.id(),
                        summary.validated(),
                        summary.taxonomyVersion(),
                        summary.activePeriod(),
                        summary.activatedAt(),
                        summary.type()
                ))
                .toList();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ActivateTemplateResponse> activate(@PathVariable UUID id,
                                                           @Valid @RequestBody ActivateTemplateRequest request) {
        Template template = activateTemplateUseCase.activate(id, request.period());
        return ResponseEntity.ok(new ActivateTemplateResponse(
                template.getId(),
                template.getTaxonomy().version(),
                template.getActivePeriod(),
                template.getActivatedAt()
        ));
    }
}
