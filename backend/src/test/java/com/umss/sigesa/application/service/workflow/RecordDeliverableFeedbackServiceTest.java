package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.StageNotFoundException;
import com.umss.sigesa.domain.model.MethodologicalStage;
import com.umss.sigesa.domain.model.StageDeliverable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecordDeliverableFeedbackService")
class RecordDeliverableFeedbackServiceTest {

    @Mock
    private MethodologicalStagePort stagePort;

    private RecordDeliverableFeedbackService service;

    @BeforeEach
    void setUp() {
        service = new RecordDeliverableFeedbackService(stagePort);
    }

    @Test
    @DisplayName("el técnico guarda la retroalimentación del entregable")
    void recordsFeedbackForTechnician() {
        UUID processId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        UUID deliverableId = UUID.randomUUID();
        StageDeliverable deliverable = StageDeliverable.builder().id(deliverableId).stageId(stageId).build();
        when(stagePort.findByIdAndProcessId(stageId, processId)).thenReturn(Optional.of(MethodologicalStage.builder().id(stageId).build()));
        when(stagePort.findDeliverableById(deliverableId)).thenReturn(Optional.of(deliverable));

        StageDeliverable saved = service.record(processId, stageId, deliverableId, "  Falta la firma de la resolución.  ", "TD");

        verify(stagePort).recordDeliverableFeedback(deliverableId, "Falta la firma de la resolución.");
        assertEquals(deliverableId, saved.getId());
    }

    @Test
    @DisplayName("otro rol no registra retroalimentación")
    void rejectsOtherRoles() {
        UUID processId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        UUID deliverableId = UUID.randomUUID();

        assertThrows(InvalidRoleException.class, () ->
                service.record(processId, stageId, deliverableId, "Observación suficiente.", "CC"));
        verify(stagePort, never()).recordDeliverableFeedback(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("un entregable de otra etapa no recibe retroalimentación")
    void rejectsDeliverableFromAnotherStage() {
        UUID processId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        UUID deliverableId = UUID.randomUUID();
        when(stagePort.findByIdAndProcessId(stageId, processId)).thenReturn(Optional.of(MethodologicalStage.builder().id(stageId).build()));
        when(stagePort.findDeliverableById(deliverableId)).thenReturn(Optional.of(
                StageDeliverable.builder().id(deliverableId).stageId(UUID.randomUUID()).build()));

        assertThrows(StageNotFoundException.class, () ->
                service.record(processId, stageId, deliverableId, "Observación suficiente.", "TD"));
    }
}
