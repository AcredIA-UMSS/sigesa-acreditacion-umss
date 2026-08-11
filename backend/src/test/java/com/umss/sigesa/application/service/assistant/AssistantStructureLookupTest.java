package com.umss.sigesa.application.service.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.application.model.process.EnrichedProcessDetail;
import com.umss.sigesa.domain.model.Phase;
import com.umss.sigesa.domain.model.Subphase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssistantStructureLookupTest {

    private static final UUID PHASE_ONE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PHASE_TWO_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void findPhase_resolvesPlaceholderUuidFase1ByOrder() throws Exception {
        var args = objectMapper.readTree("""
                {"phaseId":"UUID_FASE_1"}
                """);

        Phase phase = AssistantStructureLookup.findPhase(args, detailWithPhases());

        assertThat(phase.getId()).isEqualTo(PHASE_ONE_ID);
        assertThat(phase.getOrder()).isEqualTo(1);
    }

    @Test
    void findPhase_resolvesPhaseOrderDirectly() throws Exception {
        var args = objectMapper.readTree("""
                {"phaseOrder":2}
                """);

        Phase phase = AssistantStructureLookup.findPhase(args, detailWithPhases());

        assertThat(phase.getId()).isEqualTo(PHASE_TWO_ID);
    }

    @Test
    void findPhase_resolvesNaturalLanguageFaseName() throws Exception {
        var args = objectMapper.readTree("""
                {"phaseName":"Fase 2"}
                """);

        Phase phase = AssistantStructureLookup.findPhase(args, detailWithPhases());

        assertThat(phase.getName()).isEqualTo("Fase 2");
    }

    @Test
    void findPhase_invalidPlaceholderWithoutOrderFailsHelpfully() {
        var args = objectMapper.createObjectNode().put("phaseId", "UUID_FASE_X");

        assertThatThrownBy(() -> AssistantStructureLookup.findPhase(args, detailWithPhases()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("phaseOrder");
    }

    @Test
    void planCreateSubphaseOrder_withExistingSubphases_assignsNextAvailable() {
        Phase phase = Phase.builder()
                .id(PHASE_ONE_ID)
                .name("Fase 1")
                .order(1)
                .subphases(List.of(
                        Subphase.builder().name("subfase 1").order(1).build(),
                        Subphase.builder().name("subfase 2").order(2).build(),
                        Subphase.builder().name("subfase 3").order(3).build()))
                .build();

        AssistantStructureLookup.SubphaseOrderPlan plan =
                AssistantStructureLookup.planCreateSubphaseOrder(phase, 1);

        assertThat(plan.existingCount()).isEqualTo(3);
        assertThat(plan.maxExistingOrder()).isEqualTo(3);
        assertThat(plan.assignedOrder()).isEqualTo(4);
        assertThat(plan.ignoredLlmOrder()).isEqualTo(1);
        assertThat(plan.confirmationMessage("Fase 1", "Evidencia docente", "https://umss.edu.bo/ejemplo"))
                .contains("último orden: **3**")
                .contains("orden 4");
    }

    @Test
    void planCreateSubphaseOrder_emptyPhase_startsAtOne() {
        Phase phase = Phase.builder().id(PHASE_ONE_ID).name("Fase 1").order(1).build();

        AssistantStructureLookup.SubphaseOrderPlan plan =
                AssistantStructureLookup.planCreateSubphaseOrder(phase, null);

        assertThat(plan.existingCount()).isZero();
        assertThat(plan.assignedOrder()).isEqualTo(1);
    }

    private static EnrichedProcessDetail detailWithPhases() {
        Phase phaseOne = Phase.builder()
                .id(PHASE_ONE_ID)
                .name("Diagnóstico")
                .order(1)
                .build();
        Phase phaseTwo = Phase.builder()
                .id(PHASE_TWO_ID)
                .name("Fase 2")
                .order(2)
                .build();
        return new EnrichedProcessDetail(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "INF-SIS",
                "Ingeniería de Sistemas",
                UUID.randomUUID(),
                "Plantilla CEUB",
                "CEUB",
                "ACTIVE",
                null,
                List.of(phaseOne, phaseTwo),
                null);
    }
}
