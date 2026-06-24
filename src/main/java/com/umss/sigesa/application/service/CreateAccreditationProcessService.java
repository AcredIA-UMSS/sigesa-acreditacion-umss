package com.umss.sigesa.application.service;

import com.umss.sigesa.application.port.in.CreateAccreditationProcessUseCase;
import com.umss.sigesa.application.port.out.AccreditationProcessRepositoryPort;
import com.umss.sigesa.application.port.out.TemplateRepositoryPort;
import com.umss.sigesa.domain.exception.ProcessAlreadyActiveException;
import com.umss.sigesa.domain.exception.TemplateNotValidException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ProcessStatus;
import com.umss.sigesa.domain.model.ProcessType;
import com.umss.sigesa.domain.model.Template;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CreateAccreditationProcessService implements CreateAccreditationProcessUseCase {

    private final AccreditationProcessRepositoryPort processRepository;
    private final TemplateRepositoryPort templateRepository;

    public CreateAccreditationProcessService(AccreditationProcessRepositoryPort processRepository,
                                             TemplateRepositoryPort templateRepository) {
        this.processRepository = processRepository;
        this.templateRepository = templateRepository;
    }

    @Override
    @Transactional
    public AccreditationProcess create(UUID templateId, UUID careerId, String period, ProcessType type) {
        // Regla FSD-BR-08: Un solo proceso activo por tipo/carrera/periodo
        if (processRepository.existsActiveProcessByCareerAndTypeAndPeriod(careerId, type, period)) {
            throw new ProcessAlreadyActiveException(
                    String.format("Ya existe un proceso activo de tipo %s para la carrera en el periodo %s", type, period)
            );
        }

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Plantilla no encontrada."));

        if (!template.isValidated()) {
            throw new TemplateNotValidException("La plantilla seleccionada no ha sido validada por el comité normativo.");
        }

        AccreditationProcess newProcess = new AccreditationProcess(
                UUID.randomUUID(),
                templateId,
                careerId,
                period,
                type,
                ProcessStatus.ACTIVE,
                template.getTaxonomy().version(), // Clonación/Snapshot conceptual
                LocalDateTime.now()
        );

        return processRepository.save(newProcess);
    }
}