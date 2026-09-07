package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.assistant.AssistantChatContext;
import com.umss.sigesa.application.model.assistant.AssistantToolInvocation;
import com.umss.sigesa.domain.model.ChatMessage;
import com.umss.sigesa.domain.model.ChatRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AssistantKeywordRouterTest {

    private static final UUID PROCESS_ID = UUID.fromString("950e8400-e29b-41d4-a716-446655440020");

    private AssistantKeywordRouter router;

    @BeforeEach
    void setUp() {
        router = new AssistantKeywordRouter();
    }

    @Test
    void listPhasesQuestion_resolvesToListProcessPhases() {
        Optional<AssistantToolInvocation> result = router.resolve(
                "Lista las fases de Ingeniería de Sistemas CEUB",
                List.of(),
                tdAuth(),
                AssistantChatContext.general());

        assertThat(result).isPresent();
        assertThat(result.get().toolId()).isEqualTo(AssistantToolRegistry.LIST_PROCESS_PHASES_ID);
        assertThat(result.get().argumentsJson()).contains("careerQuery");
    }

    @Test
    void phasesAgent_contextualEtapas_usesCareerFromContext() {
        AssistantChatContext context = AssistantChatContext.phases(
                PROCESS_ID, "Ingeniería de Sistemas", "INF-SIS", "CEUB");

        Optional<AssistantToolInvocation> result = router.resolve(
                "¿Cuáles son las etapas del proceso?",
                List.of(),
                tdAuth(),
                context);

        assertThat(result).isPresent();
        assertThat(result.get().toolId()).isEqualTo(AssistantToolRegistry.LIST_PROCESS_PHASES_ID);
        assertThat(result.get().argumentsJson()).contains("Ingeniería de Sistemas");
        assertThat(result.get().argumentsJson()).contains("CEUB");
    }

    @Test
    void phasesAgent_confirmoAfterSubphasePreview_resolvesManageSubphaseConfirmed() {
        AssistantChatContext context = AssistantChatContext.phases(
                PROCESS_ID, "Ingeniería de Sistemas", "INF-SIS", "CEUB");

        String previewReply = """
                La fase «Fase 2.: verificacion de evidencias actualizada» tiene **1** subfase(s).
                Enlace: https://example.com/evidencia_docente

                Resumen: «Evidencia docente» → orden 2 en «Fase 2.: verificacion de evidencias actualizada».

                Responda **confirmo** para ejecutar la acción.""";

        List<ChatMessage> history = List.of(
                new ChatMessage(ChatRole.USER, "Agrega una subfase «Evidencia docente» en Fase 2"),
                new ChatMessage(ChatRole.ASSISTANT, previewReply));

        Optional<AssistantToolInvocation> result = router.resolve(
                "confirmo",
                history,
                tdAuth(),
                context);

        assertThat(result).isPresent();
        assertThat(result.get().toolId()).isEqualTo(AssistantToolRegistry.MANAGE_PROCESS_SUBPHASE_ID);
        assertThat(result.get().argumentsJson()).contains("\"confirmed\":true");
        assertThat(result.get().argumentsJson()).contains("Evidencia docente");
    }

    @Test
    void jdListUsers_resolvesToListUsers() {
        Optional<AssistantToolInvocation> result = router.resolve(
                "Lista usuarios registrados",
                List.of(),
                jdAuth(),
                AssistantChatContext.general());

        assertThat(result).isPresent();
        assertThat(result.get().toolId()).isEqualTo(AssistantToolRegistry.LIST_USERS_ID);
        assertThat(result.get().argumentsJson()).isEqualTo("{}");
    }

    @Test
    void ccPendingEvidences_resolvesToListPendingEvidences() {
        Optional<AssistantToolInvocation> result = router.resolve(
                "Lista evidencias pendientes",
                List.of(),
                ccAuth(),
                AssistantChatContext.general());

        assertThat(result).isPresent();
        assertThat(result.get().toolId()).isEqualTo(AssistantToolRegistry.LIST_PENDING_EVIDENCES_ID);
    }

    @Test
    void normativaCeub_resolvesToSearchNormativeDocs() {
        Optional<AssistantToolInvocation> result = router.resolve(
                "normativa CEUB",
                List.of(),
                tdAuth(),
                AssistantChatContext.general());

        assertThat(result).isPresent();
        assertThat(result.get().toolId()).isEqualTo(AssistantToolRegistry.SEARCH_NORMATIVE_DOCS_ID);
        assertThat(result.get().argumentsJson()).contains("normativa CEUB");
    }

    @Test
    void multiStepNormativeIntent_returnsEmpty() {
        Optional<AssistantToolInvocation> result = router.resolve(
                "y luego busca normativa",
                List.of(),
                tdAuth(),
                AssistantChatContext.general());

        assertThat(result).isEmpty();
    }

    @Test
    void ccListUsers_returnsEmpty() {
        Optional<AssistantToolInvocation> result = router.resolve(
                "Lista usuarios registrados",
                List.of(),
                ccAuth(),
                AssistantChatContext.general());

        assertThat(result).isEmpty();
    }

    private static AssistantAuthContext jdAuth() {
        return new AssistantAuthContext(UUID.randomUUID(), "JD", List.of());
    }

    private static AssistantAuthContext tdAuth() {
        return new AssistantAuthContext(UUID.randomUUID(), "TD", List.of());
    }

    private static AssistantAuthContext ccAuth() {
        return new AssistantAuthContext(UUID.randomUUID(), "CC", List.of(UUID.randomUUID()));
    }
}
