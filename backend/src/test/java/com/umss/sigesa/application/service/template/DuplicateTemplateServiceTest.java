package com.umss.sigesa.application.service.template;

import com.umss.sigesa.application.port.out.TemplateManagementPort;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DuplicateTemplateServiceTest {

    @Mock
    private TemplateManagementPort templateManagementPort;

    @InjectMocks
    private DuplicateTemplateService duplicateTemplateService;

    @Test
    void shouldCreateDraftCopyWithNewId() {
        UUID sourceId = UUID.randomUUID();
        Template source = Template.builder()
                .id(sourceId)
                .name("CEUB 2026")
                .description("Original")
                .type("CEUB")
                .status(TemplateStatus.PUBLISHED)
                .phases(List.of(TemplatePhase.builder()
                        .name("Fase")
                        .order(1)
                        .subphases(List.of(TemplateSubphase.builder()
                                .name("Sub")
                                .order(1)
                                .referenceUrl("https://duea.umss.edu.bo/ref/sub")
                                .build()))
                        .build()))
                .build();

        when(templateManagementPort.findByIdForEdit(sourceId)).thenReturn(java.util.Optional.of(source));
        when(templateManagementPort.save(any(Template.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Template copy = duplicateTemplateService.duplicate(sourceId);

        assertNotEquals(sourceId, copy.getId());
        assertEquals(TemplateStatus.DRAFT, copy.getStatus());
        assertEquals("Copia de CEUB 2026", copy.getName());
        assertEquals(1, copy.getPhases().size());
    }
}
