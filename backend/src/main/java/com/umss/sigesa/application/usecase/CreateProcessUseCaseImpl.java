package com.umss.sigesa.application.usecase;

import com.umss.sigesa.application.port.in.CreateProcessUseCase;
import com.umss.sigesa.application.port.out.AccreditationProcessPort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.application.service.process.ProcessNormativeTreeCloner;
import com.umss.sigesa.domain.exception.ProcessAlreadyActiveException;
import com.umss.sigesa.domain.exception.ProgramNotFoundException;
import com.umss.sigesa.domain.exception.TemplateNotFoundException;
import com.umss.sigesa.domain.exception.TemplateNotPublishedException;
import com.umss.sigesa.domain.exception.TemplateStructureIncompleteException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplateStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public class CreateProcessUseCaseImpl implements CreateProcessUseCase {

    private final AccreditationProcessPort accreditationProcessPort;
    private final TemplatePort templatePort;
    private final ProgramCatalogPort programCatalogPort;
    private final NormativeHierarchyQueryPort hierarchyQueryPort;
    private final ProcessNormativeTreeCloner normativeTreeCloner;

    public CreateProcessUseCaseImpl(AccreditationProcessPort accreditationProcessPort,
                                    TemplatePort templatePort,
                                    ProgramCatalogPort programCatalogPort,
                                    NormativeHierarchyQueryPort hierarchyQueryPort,
                                    ProcessNormativeTreeCloner normativeTreeCloner) {
        this.accreditationProcessPort = accreditationProcessPort;
        this.templatePort = templatePort;
        this.programCatalogPort = programCatalogPort;
        this.hierarchyQueryPort = hierarchyQueryPort;
        this.normativeTreeCloner = normativeTreeCloner;
    }

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

        long templateIndicatorCount = hierarchyQueryPort.countIndicatorsByTemplateId(templateId);
        List<TemplateLevel1Node> templateLevel1Nodes = hierarchyQueryPort.findTemplateTree(templateId)
                .map(NormativeHierarchyQueryPort.TemplateNormativeTree::level1Nodes)
                .orElse(List.of());
        boolean hasLegacyPhases = template.getPhases() != null && !template.getPhases().isEmpty();

        if (templateIndicatorCount < 1 && !hasLegacyPhases) {
            throw new TemplateStructureIncompleteException(
                    "La plantilla debe tener al menos un indicador en el árbol normativo v2 para crear un proceso.");
        }

        AccreditationProcess newProcess = AccreditationProcess.createFromTemplate(careerId, template);
        AccreditationProcess savedProcess = accreditationProcessPort.save(newProcess);

        if (templateIndicatorCount > 0) {
            normativeTreeCloner.cloneFromTemplate(savedProcess.getId(), templateLevel1Nodes);
        }

        return savedProcess;
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
