package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.assistant.AssistantChatContext;
import com.umss.sigesa.application.model.process.EnrichedProcessDetail;
import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.port.in.GetProcessDetailUseCase;

import java.util.UUID;

public class AssistantChatContextFactory {

    private final GetProcessDetailUseCase getProcessDetailUseCase;

    public AssistantChatContextFactory(GetProcessDetailUseCase getProcessDetailUseCase) {
        this.getProcessDetailUseCase = getProcessDetailUseCase;
    }

    public AssistantChatContext resolve(String agent,
                                        UUID processId,
                                        AssistantAuthContext auth) {
        return resolve(agent, processId, null, null, auth);
    }

    public AssistantChatContext resolve(String agent,
                                        UUID processId,
                                        UUID focusUserId,
                                        UUID programId,
                                        AssistantAuthContext auth) {
        if (agent == null || agent.isBlank() || "general".equalsIgnoreCase(agent)) {
            return AssistantChatContext.general();
        }

        if ("users".equalsIgnoreCase(agent)) {
            return AssistantChatContext.users(focusUserId, programId);
        }

        if (!"phases".equalsIgnoreCase(agent)) {
            throw new IllegalArgumentException("Agente no soportado: " + agent);
        }
        if (processId == null) {
            throw new IllegalArgumentException("processId es obligatorio para agent=phases.");
        }

        ProcessQueryContext ctx = new ProcessQueryContext(auth.role(), auth.programScope());
        EnrichedProcessDetail detail = getProcessDetailUseCase.getDetail(processId, ctx);
        String phaseCatalog = AssistantStructureLookup.buildPhaseCatalogPrompt(detail);
        return AssistantChatContext.phases(
                detail.id(),
                detail.careerName(),
                detail.careerCode(),
                detail.templateType(),
                phaseCatalog);
    }
}
