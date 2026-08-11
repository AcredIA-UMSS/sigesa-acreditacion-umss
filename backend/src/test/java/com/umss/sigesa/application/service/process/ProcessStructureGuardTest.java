package com.umss.sigesa.application.service.process;

import com.umss.sigesa.domain.exception.ProcessNotEditableException;
import com.umss.sigesa.domain.exception.ProcessStructureOrderConflictException;
import com.umss.sigesa.domain.exception.SubphaseLinkRequiredException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Phase;
import com.umss.sigesa.domain.model.Subphase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProcessStructureGuardTest {

    private ProcessStructureGuard guard;

    @BeforeEach
    void setUp() {
        guard = new ProcessStructureGuard();
    }

    @Test
    void shouldAllowActiveProcess() {
        AccreditationProcess process = AccreditationProcess.builder().status("ACTIVE").build();
        assertDoesNotThrow(() -> guard.ensureProcessActive(process));
    }

    @Test
    void shouldRejectCompletedProcess() {
        AccreditationProcess process = AccreditationProcess.builder().status("COMPLETED").build();
        assertThrows(ProcessNotEditableException.class, () -> guard.ensureProcessActive(process));
    }

    @Test
    void shouldRejectClosedProcess() {
        AccreditationProcess process = AccreditationProcess.builder().status("CLOSED").build();
        assertThrows(ProcessNotEditableException.class, () -> guard.ensureProcessActive(process));
    }

    @Test
    void shouldRequireHttpsReferenceUrl() {
        assertThrows(SubphaseLinkRequiredException.class, () -> guard.ensureReferenceUrl("http://insecure.example.com"));
        assertThrows(SubphaseLinkRequiredException.class, () -> guard.ensureReferenceUrl(""));
        assertDoesNotThrow(() -> guard.ensureReferenceUrl("https://duea.umss.edu.bo/ref"));
    }

    @Test
    void shouldDetectDuplicatePhaseOrder() {
        UUID existingPhaseId = UUID.randomUUID();
        AccreditationProcess process = AccreditationProcess.builder()
                .status("ACTIVE")
                .phases(List.of(Phase.builder().id(existingPhaseId).order(1).build()))
                .build();

        assertThrows(ProcessStructureOrderConflictException.class,
                () -> guard.ensureUniquePhaseOrder(process, 1, null));
        assertDoesNotThrow(() -> guard.ensureUniquePhaseOrder(process, 2, null));
    }

    @Test
    void shouldDetectDuplicateSubphaseOrder() {
        UUID existingSubphaseId = UUID.randomUUID();
        Phase phase = Phase.builder()
                .subphases(List.of(Subphase.builder().id(existingSubphaseId).order(1).build()))
                .build();

        assertThrows(ProcessStructureOrderConflictException.class,
                () -> guard.ensureUniqueSubphaseOrder(phase, 1, null));
        assertDoesNotThrow(() -> guard.ensureUniqueSubphaseOrder(phase, 2, null));
    }
}
