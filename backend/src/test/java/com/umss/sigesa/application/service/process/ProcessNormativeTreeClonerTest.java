package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.out.NormativeStructurePort;
import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.domain.model.Level2Node;
import com.umss.sigesa.domain.model.Level3Node;
import com.umss.sigesa.domain.model.NormativeIndicator;
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
class ProcessNormativeTreeClonerTest {

    @Mock
    private NormativeStructurePort structurePort;

    private ProcessNormativeTreeCloner cloner;

    @BeforeEach
    void setUp() {
        cloner = new ProcessNormativeTreeCloner(structurePort);
    }

    @Test
    void shouldCloneFullNormativeTreeFromTemplate() {
        UUID processId = UUID.randomUUID();
        UUID level1Id = UUID.randomUUID();
        UUID level2Id = UUID.randomUUID();
        UUID level3Id = UUID.randomUUID();

        when(structurePort.saveLevel1(eq(processId), any(Level1Node.class))).thenAnswer(invocation -> {
            Level1Node node = invocation.getArgument(1);
            node.setId(level1Id);
            return node;
        });
        when(structurePort.saveLevel2(eq(level1Id), any(Level2Node.class))).thenAnswer(invocation -> {
            Level2Node node = invocation.getArgument(1);
            node.setId(level2Id);
            return node;
        });
        when(structurePort.saveLevel3(eq(level2Id), any(Level3Node.class))).thenAnswer(invocation -> {
            Level3Node node = invocation.getArgument(1);
            node.setId(level3Id);
            return node;
        });
        when(structurePort.saveIndicator(eq(level3Id), any(NormativeIndicator.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        List<TemplateLevel1Node> templateTree = List.of(
                TemplateLevel1Node.builder()
                        .name("Área académica")
                        .order(1)
                        .description("Nivel 1")
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

        cloner.cloneFromTemplate(processId, templateTree);

        ArgumentCaptor<NormativeIndicator> indicatorCaptor = ArgumentCaptor.forClass(NormativeIndicator.class);
        verify(structurePort).saveIndicator(eq(level3Id), indicatorCaptor.capture());

        NormativeIndicator cloned = indicatorCaptor.getValue();
        assertEquals("IND-01", cloned.getCode());
        assertEquals("Indicador piloto", cloned.getDescription());
        assertEquals(new BigDecimal("0.15"), cloned.getWeight());
        assertEquals("https://duea.umss.edu.bo/guia/ind-01", cloned.getReferenceUrl());
    }
}
