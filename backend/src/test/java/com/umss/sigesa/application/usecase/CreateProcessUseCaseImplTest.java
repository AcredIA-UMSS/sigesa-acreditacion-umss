package com.umss.sigesa.application.usecase;

import com.umss.sigesa.application.port.out.AccreditationProcessPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.domain.exception.ProcessAlreadyActiveException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplatePhase;
import com.umss.sigesa.domain.model.TemplateSubphase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProcessUseCaseImplTest {

    @Mock
    private AccreditationProcessPort processPort;

    @Mock
    private TemplatePort templatePort;

    @InjectMocks
    private CreateProcessUseCaseImpl useCase;

    private UUID careerId;
    private UUID templateId;
    private Template template;

    @BeforeEach
    void setUp() {
        careerId = UUID.randomUUID();
        templateId = UUID.randomUUID();

        // Inicialización segura para los Builders de Lombok
        TemplateSubphase subphase = TemplateSubphase.builder()
                .name("Sub 1")
                .order(1)
                .build();

        List<TemplateSubphase> subphasesList = new ArrayList<>();
        subphasesList.add(subphase);

        TemplatePhase phase = TemplatePhase.builder()
                .name("Fase 1")
                .order(1)
                .subphases(subphasesList)
                .build();

        List<TemplatePhase> phasesList = new ArrayList<>();
        phasesList.add(phase);

        template = Template.builder()
                .id(templateId)
                .name("CEUB")
                .type("CEUB")
                .phases(phasesList)
                .build();
    }

    @Test
    void shouldThrowExceptionIfActiveProcessExists() {
        // Arrange
        when(processPort.existsActiveProcessByCareer(careerId)).thenReturn(true);

        // Act & Assert
        assertThrows(ProcessAlreadyActiveException.class, () -> useCase.createProcess(careerId, templateId));

        // Verificamos que no se intentó buscar la plantilla ni guardar el proceso
        verify(templatePort, never()).findById(any());
        verify(processPort, never()).save(any());
    }

    @Test
    void shouldCreateProcessSuccessfully() {
        // Arrange
        when(processPort.existsActiveProcessByCareer(careerId)).thenReturn(false);
        when(templatePort.findById(templateId)).thenReturn(Optional.of(template));

        // Simulamos que al guardar retorna la misma entidad que recibió
        when(processPort.save(any(AccreditationProcess.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AccreditationProcess result = useCase.createProcess(careerId, templateId);

        // Assert
        assertNotNull(result);
        assertEquals(com.umss.sigesa.domain.model.ProcessStatus.ACTIVE, result.getStatus());
        assertEquals(careerId, result.getCareerId());
        assertEquals(1, result.getPhases().size());
        assertEquals("Fase 1", result.getPhases().get(0).getName());
        assertEquals(1, result.getPhases().get(0).getSubphases().size());

        // Verificamos que el puerto de guardado fue llamado exactamente 1 vez
        verify(processPort, times(1)).save(any(AccreditationProcess.class));
    }
}