package com.umss.sigesa.application.service.template;

import com.umss.sigesa.domain.exception.TemplateOrderConflictException;
import com.umss.sigesa.domain.exception.TemplateStructureIncompleteException;
import com.umss.sigesa.domain.exception.TemplateSubphaseLinkRequiredException;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplatePhase;
import com.umss.sigesa.domain.model.TemplateSubphase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TemplateStructureValidatorTest {

    private TemplateStructureValidator validator;
    private TemplateNormativeStructureGuard structureGuard;

    @Mock
    private com.umss.sigesa.application.service.process.ProcessStructureGuard processStructureGuard;

    @BeforeEach
    void setUp() {
        validator = new TemplateStructureValidator();
        structureGuard = new TemplateNormativeStructureGuard(
                new com.umss.sigesa.application.service.process.NormativeStructureGuard(processStructureGuard));
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

        assertThrows(TemplateStructureIncompleteException.class,
                () -> validator.validateLegacyPhasesForPublish(template));
    }

    @Test
    void shouldAcceptValidLegacyPublishStructure() {
        Template template = validTemplateBuilder()
                .phases(List.of(phaseWithSubphase("Fase", 1)))
                .build();

        assertDoesNotThrow(() -> validator.validateLegacyPhasesForPublish(template));
    }

    @Test
    void shouldRejectPublishWithoutNormativeIndicators() {
        Template template = validTemplateBuilder().build();

        assertThrows(TemplateStructureIncompleteException.class,
                () -> validator.validateForPublish(template, List.of(), structureGuard));
    }

    @Test
    void shouldAcceptValidNormativeTreeForPublish() {
        Template template = validTemplateBuilder().build();
        List<com.umss.sigesa.domain.model.TemplateLevel1Node> tree = validNormativeTree();

        assertDoesNotThrow(() -> validator.validateForPublish(template, tree, structureGuard));
    }

    @Test
    void shouldRejectPublishWithIncompleteNormativeIndicator() {
        Template template = validTemplateBuilder().build();
        List<com.umss.sigesa.domain.model.TemplateLevel1Node> tree = List.of(
                com.umss.sigesa.domain.model.TemplateLevel1Node.builder()
                        .name("Área académica")
                        .order(1)
                        .level2Nodes(List.of(
                                com.umss.sigesa.domain.model.TemplateLevel2Node.builder()
                                        .name("Variable docente")
                                        .order(1)
                                        .level3Nodes(List.of(
                                                com.umss.sigesa.domain.model.TemplateLevel3Node.builder()
                                                        .name("Sub-variable")
                                                        .order(1)
                                                        .indicators(List.of(
                                                                com.umss.sigesa.domain.model.TemplateNormativeIndicator.builder()
                                                                        .code("")
                                                                        .description("Sin código")
                                                                        .weight(new java.math.BigDecimal("0.15"))
                                                                        .order(1)
                                                                        .referenceUrl("https://duea.umss.edu.bo/guia/ind-01")
                                                                        .build()))
                                                        .build()))
                                        .build()))
                        .build());

        assertThrows(com.umss.sigesa.domain.exception.TemplateIndicatorIncompleteException.class,
                () -> validator.validateForPublish(template, tree, structureGuard));
    }

    private List<com.umss.sigesa.domain.model.TemplateLevel1Node> validNormativeTree() {
        return List.of(
                com.umss.sigesa.domain.model.TemplateLevel1Node.builder()
                        .name("Área académica")
                        .order(1)
                        .level2Nodes(List.of(
                                com.umss.sigesa.domain.model.TemplateLevel2Node.builder()
                                        .name("Variable docente")
                                        .order(1)
                                        .level3Nodes(List.of(
                                                com.umss.sigesa.domain.model.TemplateLevel3Node.builder()
                                                        .name("Sub-variable")
                                                        .order(1)
                                                        .indicators(List.of(
                                                                com.umss.sigesa.domain.model.TemplateNormativeIndicator.builder()
                                                                        .code("IND-01")
                                                                        .description("Indicador piloto")
                                                                        .weight(new java.math.BigDecimal("0.15"))
                                                                        .order(1)
                                                                        .referenceUrl("https://duea.umss.edu.bo/guia/ind-01")
                                                                        .build()))
                                                        .build()))
                                        .build()))
                        .build());
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
                        .requirements("Documento de evidencia requerido")
                        .build()))
                .build();
    }
}
