package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.application.model.assistant.AssistantChatResult;
import com.umss.sigesa.application.model.assistant.AssistantResolutionPath;
import com.umss.sigesa.application.model.normative.NormativeDocumentHit;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AssistantNormativeRagServiceTest {

    @Test
    void tryDirectAnswer_returnsFormattedReplyWhenHitsExist() {
        AssistantNormativeRagService service = new AssistantNormativeRagService(
                (query, templateType, limit) -> List.of(
                        new NormativeDocumentHit(
                                "Diagnóstico institucional — CEUB",
                                "CEUB",
                                "Autoevaluación",
                                "Diagnóstico institucional",
                                "https://duea.umss.edu.bo/normativa/ceub/diagnostico-institucional",
                                "El diagnóstico institucional CEUB caracteriza la realidad universitaria.",
                                0.9)),
                true,
                3);

        AssistantChatResult result = service.tryDirectAnswer(
                "¿Qué dice la normativa sobre diagnóstico institucional?",
                "CEUB").orElseThrow();

        assertThat(result.path()).isEqualTo(AssistantResolutionPath.RAG);
        assertThat(result.toolId()).isEqualTo(AssistantToolRegistry.SEARCH_NORMATIVE_DOCS_ID);
        assertThat(result.sourceTables()).contains("normative_document");
        assertThat(result.reply()).contains("Diagnóstico institucional");
        assertThat(result.reply()).contains("RAG");
    }

    @Test
    void tryDirectAnswer_emptyWhenDisabled() {
        AssistantNormativeRagService service = new AssistantNormativeRagService(
                (query, templateType, limit) -> List.of(
                        new NormativeDocumentHit("T", "CEUB", null, null, null, "body", 1.0)),
                false,
                3);

        assertThat(service.tryDirectAnswer("normativa CEUB", null)).isEmpty();
    }
}
