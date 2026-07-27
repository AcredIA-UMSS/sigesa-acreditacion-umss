package com.umss.sigesa.application.service;

import com.umss.sigesa.application.port.in.ListTemplatesUseCase;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.domain.model.ProcessType;
import com.umss.sigesa.domain.model.Template;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListTemplatesService implements ListTemplatesUseCase {

    private final TemplatePort templateRepository;

    public ListTemplatesService(TemplatePort templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<TemplateSummary> list() {
        return templateRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    private TemplateSummary toSummary(Template template) {
        String taxonomyVersion = template.getTaxonomy() != null ? template.getTaxonomy().version() : "1.0";
        return new TemplateSummary(
                template.getId(),
                template.isValidated(),
                taxonomyVersion,
                template.getActivePeriod(),
                template.getActivatedAt(),
                inferProcessType(taxonomyVersion)
        );
    }

    static ProcessType inferProcessType(String taxonomyVersion) {
        if (taxonomyVersion != null && taxonomyVersion.toUpperCase().contains("ARCU")) {
            return ProcessType.ARCU_SUR;
        }
        return ProcessType.CEUB;
    }
}
