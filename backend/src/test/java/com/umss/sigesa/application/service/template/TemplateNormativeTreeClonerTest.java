package com.umss.sigesa.application.service.template;

import com.umss.sigesa.application.port.out.TemplateNormativeStructurePort;
import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplateLevel2Node;
import com.umss.sigesa.domain.model.TemplateLevel3Node;
import com.umss.sigesa.domain.model.TemplateNormativeIndicator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateNormativeTreeClonerTest {

    @Mock
    private TemplateNormativeStructurePort structurePort;

    private TemplateNormativeTreeCloner cloner;

    @BeforeEach
    void setUp() {
        cloner = new TemplateNormativeTreeCloner(structurePort);
    }

    @Test
    void shouldCloneFullNormativeTreeToTargetTemplate() {
        UUID targetTemplateId = UUID.randomUUID();
        UUID level1Id = UUID.randomUUID();
        UUID level2Id = UUID.randomUUID();
        UUID level3Id = UUID.randomUUID();

        when(structurePort.saveLevel1(eq(targetTemplateId), any(TemplateLevel1Node.class)))
                .thenAnswer(invocation -> {
                    TemplateLevel1Node node = invocation.getArgument(1);
                    node.setId(level1Id);
                    return node;
                });
        when(structurePort.saveLevel2(eq(level1Id), any(TemplateLevel2Node.class)))
                .thenAnswer(invocation -> {
                    TemplateLevel2Node node = invocation.getArgument(1);
                    node.setId(level2Id);
                    return node;
                });
        when(structurePort.saveLevel3(eq(level2Id), any(TemplateLevel3Node.class)))
                .thenAnswer(invocation -> {
                    TemplateLevel3Node node = invocation.getArgument(1);
                    node.setId(level3Id);
                    return node;
                });
        when(structurePort.saveIndicator(eq(level3Id), any(TemplateNormativeIndicator.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        List<TemplateLevel1Node> sourceTree = List.of(
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

        cloner.cloneFromTemplate(targetTemplateId, sourceTree);

        ArgumentCaptor<TemplateNormativeIndicator> indicatorCaptor =
                ArgumentCaptor.forClass(TemplateNormativeIndicator.class);
        verify(structurePort).saveIndicator(eq(level3Id), indicatorCaptor.capture());

        TemplateNormativeIndicator cloned = indicatorCaptor.getValue();
        assertEquals("IND-01", cloned.getCode());
        assertEquals("Indicador piloto", cloned.getDescription());
        assertEquals(new BigDecimal("0.15"), cloned.getWeight());
        assertEquals("https://duea.umss.edu.bo/guia/ind-01", cloned.getReferenceUrl());
    }
}
