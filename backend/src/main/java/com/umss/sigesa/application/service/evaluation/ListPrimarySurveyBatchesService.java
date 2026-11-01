package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.in.ListPrimarySurveyBatchesUseCase;
import com.umss.sigesa.application.port.out.PrimarySurveyBatchPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.PrimarySurveyBatch;

import java.util.List;
import java.util.UUID;

public class ListPrimarySurveyBatchesService implements ListPrimarySurveyBatchesUseCase {

    private final ProcessQueryPort processQueryPort;
    private final PrimarySurveyBatchPort surveyBatchPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public ListPrimarySurveyBatchesService(
            ProcessQueryPort processQueryPort,
            PrimarySurveyBatchPort surveyBatchPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.processQueryPort = processQueryPort;
        this.surveyBatchPort = surveyBatchPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public List<PrimarySurveyBatch> list(
            UUID processId,
            UUID requesterId,
            List<String> roles,
            List<UUID> jwtProgramScope) {
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        EvaluationAccessGuard.assertProcessVisible(
                processId,
                process.getCareerId(),
                requesterId,
                roles,
                jwtProgramScope,
                assignmentRepository);
        return surveyBatchPort.findByProcessId(processId);
    }
}
