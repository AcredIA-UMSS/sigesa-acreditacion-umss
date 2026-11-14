package com.umss.sigesa.application.service.observation;

import com.umss.sigesa.application.model.observation.ObservationActor;
import com.umss.sigesa.application.model.observation.ObservationDraft;
import com.umss.sigesa.application.port.out.EvidenceLookupPort;
import com.umss.sigesa.application.port.out.ProcessObservationRepositoryPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.domain.exception.ForbiddenObservationRoleException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.observation.ComplianceLevel;
import com.umss.sigesa.domain.model.observation.ObservationKind;
import com.umss.sigesa.domain.model.observation.ObservationPriority;
import com.umss.sigesa.domain.model.observation.ObservationStatus;
import com.umss.sigesa.domain.model.observation.ProcessObservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessObservationServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-04T15:00:00Z");

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private EvidenceLookupPort evidenceLookupPort;
    @Mock
    private ProcessObservationRepositoryPort observationRepository;

    private ProcessObservationService service;
    private UUID processId;
    private UUID careerId;
    private UUID evidenceId;
    private UUID technicianId;
    private UUID coordinatorId;

    @BeforeEach
    void setUp() {
        service = new ProcessObservationService(
                processQueryPort, evidenceLookupPort, observationRepository, Clock.fixed(NOW, ZoneOffset.UTC));
        processId = UUID.randomUUID();
        careerId = UUID.randomUUID();
        evidenceId = UUID.randomUUID();
        technicianId = UUID.randomUUID();
        coordinatorId = UUID.randomUUID();
    }

    @Test
    @DisplayName("el técnico crea un borrador y el coordinador de otra carrera no lo ve")
    void alcanceDeCarrera() {
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(process()));
        when(evidenceLookupPort.findById(evidenceId)).thenReturn(Optional.of(evidence()));
        when(observationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ProcessObservation created = service.create(processId, draft(), technician());
        assertEquals(ObservationStatus.BORRADOR, created.getStatus());

        assertThrows(ProcessNotFoundException.class,
                () -> service.list(processId, null, new ObservationActor(coordinatorId, "CC", List.of(UUID.randomUUID()))));
    }

    @Test
    @DisplayName("el coordinador no registra observaciones")
    void coordinadorNoCrea() {
        assertThrows(ForbiddenObservationRoleException.class,
                () -> service.create(processId, draft(), new ObservationActor(coordinatorId, "CC", List.of(careerId))));
    }

    @Test
    @DisplayName("enviada, revisada y respondida conserva el mensaje")
    void hiloDeRespuesta() {
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(process()));
        when(evidenceLookupPort.findById(evidenceId)).thenReturn(Optional.of(evidence()));
        when(observationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ProcessObservation created = service.create(processId, draft(), technician());
        when(observationRepository.findById(created.getId())).thenReturn(Optional.of(created));

        service.submit(processId, created.getId(), technician());
        service.startReview(processId, created.getId(), coordinator());
        ProcessObservation replied = service.reply(processId, created.getId(), "Falta el plano", coordinator());

        assertEquals(ObservationStatus.RESPONDIDA, replied.getStatus());
        assertEquals(1, replied.getMessages().size());
        assertEquals("CC", replied.getMessages().getFirst().authorRole());
    }

    private AccreditationProcess process() {
        AccreditationProcess process = new AccreditationProcess();
        process.setId(processId);
        process.setCareerId(careerId);
        return process;
    }

    private EvidenceLookupPort.EvidenceRef evidence() {
        return new EvidenceLookupPort.EvidenceRef(
                evidenceId, UUID.randomUUID(), careerId, "IND-1", "Infraestructura");
    }

    private ObservationDraft draft() {
        return new ObservationDraft(
                evidenceId,
                "El plano no corresponde al criterio",
                ObservationKind.CONTENIDO,
                ObservationPriority.MEDIA,
                ComplianceLevel.NO_CUMPLE,
                "Cargar el plano vigente");
    }

    private ObservationActor technician() {
        return new ObservationActor(technicianId, "TD", List.of());
    }

    private ObservationActor coordinator() {
        return new ObservationActor(coordinatorId, "CC", List.of(careerId));
    }
}
