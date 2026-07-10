package com.umss.sigesa.application.service;

import com.umss.sigesa.application.port.out.TemplateRepositoryPort;
import com.umss.sigesa.domain.exception.TemplateNotValidException;
import com.umss.sigesa.domain.model.Taxonomy;
import com.umss.sigesa.domain.model.Template;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivateTemplateServiceTest {

    @Mock
    private TemplateRepositoryPort templateRepository;

    @InjectMocks
    private ActivateTemplateService service;

    @Test
    @DisplayName("Activa plantilla validada y persiste periodo")
    void activate_validTemplate_persistsActivation() {
        UUID templateId = UUID.randomUUID();
        Template validated = new Template(templateId, true, new Taxonomy("CEUB-2026.1"));
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(validated));
        when(templateRepository.save(any(Template.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Template result = service.activate(templateId, "2026-1");

        assertThat(result.getActivePeriod()).isEqualTo("2026-1");
        assertThat(result.getActivatedAt()).isNotNull();

        ArgumentCaptor<Template> captor = ArgumentCaptor.forClass(Template.class);
        verify(templateRepository).save(captor.capture());
        assertThat(captor.getValue().getActivePeriod()).isEqualTo("2026-1");
    }

    @Test
    @DisplayName("Rechaza activación de plantilla no validada")
    void activate_draftTemplate_throws422DomainError() {
        UUID templateId = UUID.randomUUID();
        Template draft = new Template(templateId, false, new Taxonomy("DRAFT-0.1"));
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.activate(templateId, "2026-1"))
                .isInstanceOf(TemplateNotValidException.class);
    }
}
