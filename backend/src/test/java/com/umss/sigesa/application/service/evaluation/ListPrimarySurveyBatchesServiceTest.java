package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.out.PrimarySurveyBatchPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.PrimarySurveyBatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListPrimarySurveyBatchesServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private PrimarySurveyBatchPort surveyBatchPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    private ListPrimarySurveyBatchesService service;

    @BeforeEach
    void setUp() {
        service = new ListPrimarySurveyBatchesService(processQueryPort, surveyBatchPort, assignmentRepository);
    }

    @Test
    void list_returnsBatches() {
        UUID processId = UUID.randomUUID();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder().id(processId).careerId(UUID.randomUUID()).status("ACTIVE").build()));
        when(surveyBatchPort.findByProcessId(processId)).thenReturn(List.of(
                PrimarySurveyBatch.builder().id(UUID.randomUUID()).processId(processId).audience("ESTUDIANTES").build()));

        List<PrimarySurveyBatch> result = service.list(processId, UUID.randomUUID(), List.of("TD"), List.of());
        assertEquals(1, result.size());
        assertEquals("ESTUDIANTES", result.getFirst().getAudience());
    }
}
