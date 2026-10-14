package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.in.RegisterPrimarySurveyBatchUseCase;
import com.umss.sigesa.application.port.out.DocumentAssetPort;
import com.umss.sigesa.application.port.out.PrimarySurveyBatchPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.DocumentAssetNotFoundException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.exception.ValidationException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.DocumentAsset;
import com.umss.sigesa.domain.model.PrimarySurveyBatch;
import com.umss.sigesa.domain.model.RegisterPrimarySurveyBatchCommand;

import java.time.LocalDateTime;

public class RegisterPrimarySurveyBatchService implements RegisterPrimarySurveyBatchUseCase {

    private final ProcessQueryPort processQueryPort;
    private final PrimarySurveyBatchPort surveyBatchPort;
    private final DocumentAssetPort documentAssetPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public RegisterPrimarySurveyBatchService(
            ProcessQueryPort processQueryPort,
            PrimarySurveyBatchPort surveyBatchPort,
            DocumentAssetPort documentAssetPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.processQueryPort = processQueryPort;
        this.surveyBatchPort = surveyBatchPort;
        this.documentAssetPort = documentAssetPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public PrimarySurveyBatch register(RegisterPrimarySurveyBatchCommand command) {
        AccreditationProcess process = processQueryPort.findDetailById(command.processId())
                .orElseThrow(() -> new ProcessNotFoundException(command.processId()));
        EvaluationAccessGuard.assertProcessVisible(
                command.processId(),
                process.getCareerId(),
                command.registeredBy(),
                command.roles(),
                command.jwtProgramScope(),
                assignmentRepository);

        String audience = command.audience() == null ? "" : command.audience().trim();
        if (audience.isEmpty() || audience.length() > 32) {
            throw new ValidationException("audience debe tener entre 1 y 32 caracteres.");
        }
        if (command.responsesCount() == null || command.responsesCount() < 1) {
            throw new ValidationException("responsesCount debe ser al menos 1.");
        }

        if (command.documentAssetId() != null) {
            DocumentAsset asset = documentAssetPort.findById(command.documentAssetId())
                    .orElseThrow(() -> new DocumentAssetNotFoundException(command.documentAssetId()));
            if (!asset.getProcessId().equals(command.processId())) {
                throw new ProgramScopeDeniedException();
            }
        }

        PrimarySurveyBatch batch = PrimarySurveyBatch.builder()
                .processId(command.processId())
                .audience(audience)
                .responsesCount(command.responsesCount())
                .documentAssetId(command.documentAssetId())
                .registeredAt(LocalDateTime.now())
                .build();
        return surveyBatchPort.save(batch);
    }
}
