package com.umss.sigesa.application.service;

import com.umss.sigesa.application.port.in.ListTemplatesUseCase;
import com.umss.sigesa.application.port.out.TemplateRepositoryPort;
import com.umss.sigesa.domain.model.ProcessType;
import com.umss.sigesa.domain.model.Template;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListTemplatesService implements ListTemplatesUseCase {

    private final TemplateRepositoryPort templateRepository;

    public ListTemplatesService(TemplateRepositoryPort templateRepository) {
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
        return new TemplateSummary(
                template.getId(),
                template.isValidated(),
                template.getTaxonomy().version(),
                template.getActivePeriod(),
                template.getActivatedAt(),
                inferProcessType(template.getTaxonomy().version())
        );
    }

    static ProcessType inferProcessType(String taxonomyVersion) {
        if (taxonomyVersion != null && taxonomyVersion.toUpperCase().contains("ARCU")) {
            return ProcessType.ARCU_SUR;
        }
        return ProcessType.CEUB;
    }
}
