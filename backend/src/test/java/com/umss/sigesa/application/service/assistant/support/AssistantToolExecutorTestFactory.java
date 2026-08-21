package com.umss.sigesa.application.service.assistant.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.application.port.in.ActivateUserUseCase;
import com.umss.sigesa.application.port.in.AddProcessPhaseUseCase;
import com.umss.sigesa.application.port.in.AddProcessSubphaseUseCase;
import com.umss.sigesa.application.port.in.CheckEvidenceCompletenessUseCase;
import com.umss.sigesa.application.port.in.DeactivateUserUseCase;
import com.umss.sigesa.application.port.in.DeleteProcessPhaseUseCase;
import com.umss.sigesa.application.port.in.DeleteProcessSubphaseUseCase;
import com.umss.sigesa.application.port.in.GetEvidenceDetailUseCase;
import com.umss.sigesa.application.port.in.GetProcessDetailUseCase;
import com.umss.sigesa.application.port.in.ListPendingEvidencesUseCase;
import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.application.port.in.ListProgramsUseCase;
import com.umss.sigesa.application.port.in.ListUsersUseCase;
import com.umss.sigesa.application.port.in.ManageUserProgramAssignmentUseCase;
import com.umss.sigesa.application.port.in.RegisterUserUseCase;
import com.umss.sigesa.application.port.in.ReorderProcessStructureUseCase;
import com.umss.sigesa.application.port.in.UpdateProcessPhaseUseCase;
import com.umss.sigesa.application.port.in.UpdateProcessSubphaseUseCase;
import com.umss.sigesa.application.port.in.SearchNormativeDocumentsUseCase;
import com.umss.sigesa.application.port.out.AssistantToolAuditPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.application.service.assistant.AssistantToolExecutor;
import com.umss.sigesa.application.service.assistant.AssistantToolRegistry;

public final class AssistantToolExecutorTestFactory {

    private AssistantToolExecutorTestFactory() {
    }

    public static AssistantToolExecutor create(
            AssistantToolRegistry registry,
            ListUsersUseCase listUsersUseCase,
            AssistantToolAuditPort auditPort) {
        return new AssistantToolExecutor(
                registry,
                listUsersUseCase,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new ObjectMapper(),
                auditPort);
    }

    public static AssistantToolExecutor createMinimal(
            AssistantToolRegistry registry,
            AssistantToolAuditPort auditPort) {
        return create(registry, null, auditPort);
    }

    public static AssistantToolExecutor createFull(
            AssistantToolRegistry registry,
            ListUsersUseCase listUsersUseCase,
            ActivateUserUseCase activateUserUseCase,
            DeactivateUserUseCase deactivateUserUseCase,
            RegisterUserUseCase registerUserUseCase,
            ManageUserProgramAssignmentUseCase manageUserProgramAssignmentUseCase,
            UserRepositoryPort userRepositoryPort,
            ListProgramsUseCase listProgramsUseCase,
            ListProcessesUseCase listProcessesUseCase,
            GetProcessDetailUseCase getProcessDetailUseCase,
            AddProcessPhaseUseCase addProcessPhaseUseCase,
            UpdateProcessPhaseUseCase updateProcessPhaseUseCase,
            DeleteProcessPhaseUseCase deleteProcessPhaseUseCase,
            AddProcessSubphaseUseCase addProcessSubphaseUseCase,
            UpdateProcessSubphaseUseCase updateProcessSubphaseUseCase,
            DeleteProcessSubphaseUseCase deleteProcessSubphaseUseCase,
            ReorderProcessStructureUseCase reorderProcessStructureUseCase,
            ListPendingEvidencesUseCase listPendingEvidencesUseCase,
            GetEvidenceDetailUseCase getEvidenceDetailUseCase,
            CheckEvidenceCompletenessUseCase checkEvidenceCompletenessUseCase,
            SearchNormativeDocumentsUseCase searchNormativeDocumentsUseCase,
            AssistantToolAuditPort auditPort) {
        return new AssistantToolExecutor(
                registry,
                listUsersUseCase,
                activateUserUseCase,
                deactivateUserUseCase,
                registerUserUseCase,
                manageUserProgramAssignmentUseCase,
                userRepositoryPort,
                listProgramsUseCase,
                listProcessesUseCase,
                getProcessDetailUseCase,
                addProcessPhaseUseCase,
                updateProcessPhaseUseCase,
                deleteProcessPhaseUseCase,
                addProcessSubphaseUseCase,
                updateProcessSubphaseUseCase,
                deleteProcessSubphaseUseCase,
                reorderProcessStructureUseCase,
                listPendingEvidencesUseCase,
                getEvidenceDetailUseCase,
                checkEvidenceCompletenessUseCase,
                searchNormativeDocumentsUseCase,
                new ObjectMapper(),
                auditPort);
    }
}
