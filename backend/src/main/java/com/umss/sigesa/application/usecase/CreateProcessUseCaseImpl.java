package com.umss.sigesa.application.usecase;

import com.umss.sigesa.application.port.in.CreateProcessUseCase;
import com.umss.sigesa.application.port.out.AccreditationProcessPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.domain.exception.ProcessAlreadyActiveException;
import com.umss.sigesa.domain.exception.TemplateNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Template;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateProcessUseCaseImpl implements CreateProcessUseCase {

    private final AccreditationProcessPort accreditationProcessPort;
    private final TemplatePort templatePort;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccreditationProcess createProcess(UUID careerId, UUID templateId) {

        if (accreditationProcessPort.existsActiveProcessByCareer(careerId)) {
            throw new ProcessAlreadyActiveException("La carrera ya cuenta con un proceso de acreditación ACTIVO.");
        }

        Template template = templatePort.findById(templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Plantilla no encontrada con ID: " + templateId));

        AccreditationProcess newProcess = AccreditationProcess.createFromTemplate(careerId, template);

        return accreditationProcessPort.save(newProcess);
    }
}