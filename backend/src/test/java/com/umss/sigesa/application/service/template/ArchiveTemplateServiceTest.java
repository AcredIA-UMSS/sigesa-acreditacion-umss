package com.umss.sigesa.application.service.template;

import com.umss.sigesa.application.port.out.TemplateManagementPort;
import com.umss.sigesa.domain.exception.TemplateNotFoundException;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArchiveTemplateServiceTest {

    @Mock
    private TemplateManagementPort templateManagementPort;

    @InjectMocks
    private ArchiveTemplateService service;

    @Test
    void shouldArchivePublishedTemplate() {
        UUID templateId = UUID.randomUUID();
        Template template = Template.builder()
                .id(templateId)
                .name("CEUB")
                .status(TemplateStatus.PUBLISHED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(templateManagementPort.findByIdForEdit(templateId)).thenReturn(Optional.of(template));
        when(templateManagementPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Template archived = service.archive(templateId);

        assertThat(archived.getStatus()).isEqualTo(TemplateStatus.ARCHIVED);
        verify(templateManagementPort).save(template);
    }

    @Test
    void shouldThrowWhenTemplateDoesNotExist() {
        UUID templateId = UUID.randomUUID();
        when(templateManagementPort.findByIdForEdit(templateId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.archive(templateId))
                .isInstanceOf(TemplateNotFoundException.class);
        verify(templateManagementPort, never()).save(any());
    }
}
