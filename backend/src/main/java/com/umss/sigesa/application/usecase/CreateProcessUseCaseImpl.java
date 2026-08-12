package com.umss.sigesa.application.usecase;

import com.umss.sigesa.application.port.in.CreateProcessUseCase;
import com.umss.sigesa.application.port.out.AccreditationProcessPort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.domain.exception.ProcessAlreadyActiveException;
import com.umss.sigesa.domain.exception.ProgramNotFoundException;
import com.umss.sigesa.domain.exception.TemplateNotFoundException;
import com.umss.sigesa.domain.exception.TemplateNotPublishedException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
public class CreateProcessUseCaseImpl implements CreateProcessUseCase {

    private final AccreditationProcessPort accreditationProcessPort;
    private final TemplatePort templatePort;
    private final ProgramCatalogPort programCatalogPort;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccreditationProcess createProcess(UUID careerId, UUID templateId) {

        programCatalogPort.findById(careerId)
                .orElseThrow(() -> new ProgramNotFoundException(careerId));

        Template template = templatePort.findById(templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Plantilla no encontrada con ID: " + templateId));

        if (!isAllowedTemplateType(template.getType())) {
            throw new TemplateNotFoundException(
                    "Plantilla no permitida. Solo se admiten procesos CEUB o ARCU-SUR.");
        }

        if (template.getStatus() != TemplateStatus.PUBLISHED) {
            throw new TemplateNotPublishedException(
                    "Solo se pueden crear procesos desde plantillas publicadas (PUBLISHED).");
        }

        if (accreditationProcessPort.existsActiveProcessByCareerAndTemplateType(
                careerId, normalizeTemplateType(template.getType()))) {
            throw new ProcessAlreadyActiveException(
                    "La carrera ya cuenta con un proceso ACTIVO de tipo "
                            + normalizeTemplateType(template.getType()) + ".");
        }

        AccreditationProcess newProcess = AccreditationProcess.createFromTemplate(careerId, template);

        return accreditationProcessPort.save(newProcess);
    }

    private static String normalizeTemplateType(String type) {
        if (type == null) {
            return "";
        }
        return type.trim().toUpperCase();
    }

    private static boolean isAllowedTemplateType(String type) {
        String normalized = normalizeTemplateType(type);
        return normalized.equals("CEUB") || normalized.equals("ARCU-SUR");
    }
}
