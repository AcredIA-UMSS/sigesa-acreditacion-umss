package com.umss.sigesa.application.service.template;

import com.umss.sigesa.application.port.out.TemplateManagementPort;
import com.umss.sigesa.domain.exception.TemplateInUseException;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteTemplateServiceTest {

    @Mock
    private TemplateManagementPort templateManagementPort;

    @InjectMocks
    private DeleteTemplateService deleteTemplateService;

    @Test
    void shouldDeleteDraftTemplateWithoutProcesses() {
        UUID templateId = UUID.randomUUID();
        Template draft = Template.builder()
                .id(templateId)
                .name("Draft")
                .type("CEUB")
                .status(TemplateStatus.DRAFT)
                .build();

        when(templateManagementPort.findByIdForEdit(templateId)).thenReturn(Optional.of(draft));
        when(templateManagementPort.existsProcessByTemplateId(templateId)).thenReturn(false);

        deleteTemplateService.delete(templateId);

        verify(templateManagementPort).delete(templateId);
    }

    @Test
    void shouldRejectDeleteWhenTemplateReferencedByProcess() {
        UUID templateId = UUID.randomUUID();
        Template draft = Template.builder()
                .id(templateId)
                .status(TemplateStatus.DRAFT)
                .build();

        when(templateManagementPort.findByIdForEdit(templateId)).thenReturn(Optional.of(draft));
        when(templateManagementPort.existsProcessByTemplateId(templateId)).thenReturn(true);

        assertThrows(TemplateInUseException.class, () -> deleteTemplateService.delete(templateId));
        verify(templateManagementPort, never()).delete(templateId);
    }
}
