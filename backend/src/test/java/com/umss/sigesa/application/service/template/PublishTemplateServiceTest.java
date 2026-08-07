package com.umss.sigesa.application.service.template;

import com.umss.sigesa.application.port.out.TemplateManagementPort;
import com.umss.sigesa.domain.exception.TemplateNotFoundException;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplatePhase;
import com.umss.sigesa.domain.model.TemplateStatus;
import com.umss.sigesa.domain.model.TemplateSubphase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublishTemplateServiceTest {

    @Mock
    private TemplateManagementPort templateManagementPort;

    @Mock
    private TemplateStructureValidator validator;

    @InjectMocks
    private PublishTemplateService publishTemplateService;

    @Test
    void shouldPublishDraftTemplate() {
        UUID templateId = UUID.randomUUID();
        Template draft = draftTemplate(templateId);

        when(templateManagementPort.findByIdForEdit(templateId)).thenReturn(Optional.of(draft));
        when(templateManagementPort.save(any(Template.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Template published = publishTemplateService.publish(templateId);

        assertEquals(TemplateStatus.PUBLISHED, published.getStatus());
        verify(templateManagementPort).save(any(Template.class));
    }

    @Test
    void shouldThrowWhenTemplateNotFound() {
        UUID templateId = UUID.randomUUID();
        when(templateManagementPort.findByIdForEdit(templateId)).thenReturn(Optional.empty());

        assertThrows(TemplateNotFoundException.class, () -> publishTemplateService.publish(templateId));
    }

    private Template draftTemplate(UUID templateId) {
        return Template.builder()
                .id(templateId)
                .name("CEUB Piloto")
                .type("CEUB")
                .status(TemplateStatus.DRAFT)
                .phases(List.of(TemplatePhase.builder()
                        .name("Autoevaluación")
                        .order(1)
                        .subphases(List.of(TemplateSubphase.builder()
                                .name("Diagnóstico")
                                .order(1)
                                .referenceUrl("https://duea.umss.edu.bo/guia/diagnostico")
                                .build()))
                        .build()))
                .build();
    }
}
