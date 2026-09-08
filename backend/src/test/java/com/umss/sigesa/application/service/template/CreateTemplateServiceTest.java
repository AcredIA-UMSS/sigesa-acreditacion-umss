package com.umss.sigesa.application.service.template;

import com.umss.sigesa.application.port.out.TemplateManagementPort;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTemplateServiceTest {

    @Mock
    private TemplateManagementPort templateManagementPort;

    @Mock
    private TemplateStructureValidator validator;

    @InjectMocks
    private CreateTemplateService createTemplateService;

    @Test
    void shouldCreateDraftTemplate() {
        Template request = Template.builder()
                .name("CEUB Piloto")
                .type("CEUB")
                .build();

        when(templateManagementPort.save(any(Template.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Template created = createTemplateService.create(request);

        assertNotNull(created.getId());
        assertEquals(TemplateStatus.DRAFT, created.getStatus());
        assertEquals("CEUB", created.getType());
        verify(validator).validateType("CEUB");
        verify(templateManagementPort).save(any(Template.class));
    }
}
