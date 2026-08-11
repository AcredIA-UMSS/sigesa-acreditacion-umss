package com.umss.sigesa.application.service.template;

import com.umss.sigesa.domain.exception.TemplateOrderConflictException;
import com.umss.sigesa.domain.exception.TemplateStructureIncompleteException;
import com.umss.sigesa.domain.exception.TemplateSubphaseLinkRequiredException;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplatePhase;
import com.umss.sigesa.domain.model.TemplateSubphase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TemplateStructureValidatorTest {

    private TemplateStructureValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TemplateStructureValidator();
    }

    @Test
    void shouldRejectSubphaseWithoutHttpsLink() {
        Template template = validTemplateBuilder()
                .phases(List.of(TemplatePhase.builder()
                        .name("Fase")
                        .order(1)
                        .subphases(List.of(TemplateSubphase.builder()
                                .name("Sub")
                                .order(1)
                                .referenceUrl("http://inseguro.example.com")
                                .build()))
                        .build()))
                .build();

        assertThrows(TemplateSubphaseLinkRequiredException.class,
                () -> validator.validateSubphaseLinks(template));
    }

    @Test
    void shouldRejectDuplicatePhaseOrder() {
        Template template = validTemplateBuilder()
                .phases(List.of(
                        phaseWithSubphase("F1", 1),
                        phaseWithSubphase("F2", 1)
                ))
                .build();

        assertThrows(TemplateOrderConflictException.class, () -> validator.validateOrders(template));
    }

    @Test
    void shouldRejectPublishWithoutSubphases() {
        Template template = validTemplateBuilder()
                .phases(List.of(TemplatePhase.builder()
                        .name("Fase vacía")
                        .order(1)
                        .subphases(List.of())
                        .build()))
                .build();

        assertThrows(TemplateStructureIncompleteException.class, () -> validator.validateForPublish(template));
    }

    @Test
    void shouldAcceptValidPublishStructure() {
        Template template = validTemplateBuilder()
                .phases(List.of(phaseWithSubphase("Fase", 1)))
                .build();

        assertDoesNotThrow(() -> validator.validateForPublish(template));
    }

    private Template.TemplateBuilder validTemplateBuilder() {
        return Template.builder().name("Test").type("CEUB");
    }

    private TemplatePhase phaseWithSubphase(String phaseName, int order) {
        return TemplatePhase.builder()
                .name(phaseName)
                .order(order)
                .subphases(List.of(TemplateSubphase.builder()
                        .name("Subfase")
                        .order(1)
                        .referenceUrl("https://duea.umss.edu.bo/guia/test")
                        .build()))
                .build();
    }
}
