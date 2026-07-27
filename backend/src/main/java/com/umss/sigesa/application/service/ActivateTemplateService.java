package com.umss.sigesa.application.service;

import com.umss.sigesa.application.port.in.ActivateTemplateUseCase;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.domain.exception.TemplateNotValidException;
import com.umss.sigesa.domain.model.Template;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ActivateTemplateService implements ActivateTemplateUseCase {

    private final TemplatePort templateRepository;

    public ActivateTemplateService(TemplatePort templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    @Transactional
    public Template activate(UUID templateId, String period) {
        if (period == null || period.isBlank()) {
            throw new IllegalArgumentException("El periodo de activación es obligatorio.");
        }

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Plantilla no encontrada."));

        if (!template.isValidated()) {
            throw new TemplateNotValidException(
                    "La plantilla no puede activarse porque no ha sido validada por el comité normativo.");
        }

        Template activated = template.withActivation(period.trim(), LocalDateTime.now());
        return templateRepository.save(activated);
    }
}
