package com.umss.sigesa.application.service;

import com.umss.sigesa.application.port.out.AccreditationProcessRepositoryPort;
import com.umss.sigesa.config.DevSeedData;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ProcessStatus;
import com.umss.sigesa.domain.model.ProcessType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetProcessService Unit Tests")
class GetProcessServiceTest {

    @Mock
    private AccreditationProcessRepositoryPort processRepository;

    @InjectMocks
    private GetProcessService service;

    @Test
    @DisplayName("Retorna detalle cuando el proceso existe")
    void getById_existingProcess() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 15, 10, 0);
        AccreditationProcess process = new AccreditationProcess(
                DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE,
                DevSeedData.TEMPLATE_CEUB_2026,
                DevSeedData.PROGRAM_INF_SIS,
                DevSeedData.PERIOD_2026_1,
                ProcessType.CEUB,
                ProcessStatus.ACTIVE,
                DevSeedData.TAXONOMY_CEUB_VERSION,
                createdAt
        );

        when(processRepository.findById(DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE))
                .thenReturn(Optional.of(process));

        var result = service.getById(DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE);

        assertTrue(result.isPresent());
        assertEquals(ProcessStatus.ACTIVE, result.get().status());
        assertEquals(DevSeedData.PERIOD_2026_1, result.get().period());
    }

    @Test
    @DisplayName("Retorna vacío cuando el proceso no existe")
    void getById_missingProcess() {
        when(processRepository.findById(DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE))
                .thenReturn(Optional.empty());

        assertTrue(service.getById(DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE).isEmpty());
    }
}
