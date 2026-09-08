package com.umss.sigesa.application.service.template;

import com.umss.sigesa.domain.exception.TemplateOrderConflictException;
import com.umss.sigesa.domain.exception.TemplateStructureIncompleteException;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplateLevel2Node;
import com.umss.sigesa.domain.model.TemplateLevel3Node;
import com.umss.sigesa.domain.model.TemplateNormativeIndicator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
    void shouldRejectDuplicateLevel1Order() {
        List<TemplateLevel1Node> tree = List.of(
                TemplateLevel1Node.builder().name("N1-A").order(1).build(),
                TemplateLevel1Node.builder().name("N1-B").order(1).build());

        assertThrows(TemplateOrderConflictException.class,
                () -> validator.validateNormativeTreeForPublish(tree, structureGuard));
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
        List<TemplateLevel1Node> tree = validNormativeTree();

        assertDoesNotThrow(() -> validator.validateForPublish(template, tree, structureGuard));
    }

    @Test
    void shouldRejectPublishWithIncompleteNormativeIndicator() {
        Template template = validTemplateBuilder().build();
        List<TemplateLevel1Node> tree = List.of(
                TemplateLevel1Node.builder()
                        .name("Área académica")
                        .order(1)
                        .level2Nodes(List.of(
                                TemplateLevel2Node.builder()
                                        .name("Variable docente")
                                        .order(1)
                                        .level3Nodes(List.of(
                                                TemplateLevel3Node.builder()
                                                        .name("Sub-variable")
                                                        .order(1)
                                                        .indicators(List.of(
                                                                TemplateNormativeIndicator.builder()
                                                                        .code("")
                                                                        .description("Sin código")
                                                                        .weight(new BigDecimal("0.15"))
                                                                        .order(1)
                                                                        .referenceUrl("https://duea.umss.edu.bo/guia/ind-01")
                                                                        .build()))
                                                        .build()))
                                        .build()))
                        .build());

        assertThrows(com.umss.sigesa.domain.exception.TemplateIndicatorIncompleteException.class,
                () -> validator.validateForPublish(template, tree, structureGuard));
    }

    private List<TemplateLevel1Node> validNormativeTree() {
        return List.of(
                TemplateLevel1Node.builder()
                        .name("Área académica")
                        .order(1)
                        .level2Nodes(List.of(
                                TemplateLevel2Node.builder()
                                        .name("Variable docente")
                                        .order(1)
                                        .level3Nodes(List.of(
                                                TemplateLevel3Node.builder()
                                                        .name("Sub-variable")
                                                        .order(1)
                                                        .indicators(List.of(
                                                                TemplateNormativeIndicator.builder()
                                                                        .code("IND-01")
                                                                        .description("Indicador piloto")
                                                                        .weight(new BigDecimal("0.15"))
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
}
