package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.out.DocumentAssetPort;
import com.umss.sigesa.application.port.out.PrimarySurveyBatchPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.exception.ValidationException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.DocumentAsset;
import com.umss.sigesa.domain.model.PrimarySurveyBatch;
import com.umss.sigesa.domain.model.RegisterPrimarySurveyBatchCommand;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterPrimarySurveyBatchService — M8")
class RegisterPrimarySurveyBatchServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private PrimarySurveyBatchPort surveyBatchPort;
    @Mock
    private DocumentAssetPort documentAssetPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    private RegisterPrimarySurveyBatchService service;

    @BeforeEach
    void setUp() {
        service = new RegisterPrimarySurveyBatchService(
                processQueryPort, surveyBatchPort, documentAssetPort, assignmentRepository);
    }

    @Test
    void register_success() {
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        stubProcess(processId, careerId);
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, careerId, LocalDateTime.now(), null)));
        when(surveyBatchPort.save(any())).thenAnswer(invocation -> {
            PrimarySurveyBatch batch = invocation.getArgument(0);
            batch.setId(UUID.randomUUID());
            return batch;
        });

        PrimarySurveyBatch saved = service.register(new RegisterPrimarySurveyBatchCommand(
                processId, "ESTUDIANTES", 120, null, userId, List.of("CC"), List.of()));

        assertEquals("ESTUDIANTES", saved.getAudience());
        assertEquals(120, saved.getResponsesCount());
        assertEquals(processId, saved.getProcessId());
    }

    @Test
    void register_rejectsResponsesCountBelowOne() {
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        stubProcess(processId, careerId);
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, careerId, LocalDateTime.now(), null)));

        assertThrows(ValidationException.class, () -> service.register(new RegisterPrimarySurveyBatchCommand(
                processId, "ESTUDIANTES", 0, null, userId, List.of("CC"), List.of())));
    }

    @Test
    void register_rejectsBlankAudience() {
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        stubProcess(processId, careerId);
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, careerId, LocalDateTime.now(), null)));

        assertThrows(ValidationException.class, () -> service.register(new RegisterPrimarySurveyBatchCommand(
                processId, "  ", 10, null, userId, List.of("CC"), List.of())));
    }

    @Test
    void register_assetFromOtherProcessDenied() {
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        stubProcess(processId, careerId);
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, careerId, LocalDateTime.now(), null)));
        DocumentAsset foreign = DocumentAsset.create(
                UUID.randomUUID(), "otro.pdf", "key", "abcd1234ffff", userId, LocalDateTime.now());
        when(documentAssetPort.findById(assetId)).thenReturn(Optional.of(foreign));

        assertThrows(ProgramScopeDeniedException.class, () -> service.register(
                new RegisterPrimarySurveyBatchCommand(
                        processId, "ESTUDIANTES", 10, assetId, userId, List.of("CC"), List.of())));
    }

    private void stubProcess(UUID processId, UUID careerId) {
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder().id(processId).careerId(careerId).status("ACTIVE").build()));
    }
}
