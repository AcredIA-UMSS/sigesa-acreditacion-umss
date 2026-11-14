package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplateLevel2Node;
import com.umss.sigesa.domain.model.TemplateLevel3Node;
import com.umss.sigesa.domain.model.TemplateNormativeIndicator;

import java.util.List;
import java.util.UUID;

/**
 * Persistencia de mutaciones sobre el árbol normativo v2 en plantilla DRAFT.
 */
public interface TemplateNormativeStructurePort {

    TemplateLevel1Node saveLevel1(UUID templateId, TemplateLevel1Node node);

    TemplateLevel1Node updateLevel1(UUID templateId, UUID level1Id, TemplateLevel1Node node);

    void deleteLevel1(UUID templateId, UUID level1Id);

    TemplateLevel2Node saveLevel2(UUID level1Id, TemplateLevel2Node node);

    TemplateLevel2Node updateLevel2(UUID level1Id, UUID level2Id, TemplateLevel2Node node);

    void deleteLevel2(UUID level1Id, UUID level2Id);

    TemplateLevel3Node saveLevel3(UUID level2Id, TemplateLevel3Node node);

    TemplateLevel3Node updateLevel3(UUID level2Id, UUID level3Id, TemplateLevel3Node node);

    void deleteLevel3(UUID level2Id, UUID level3Id);

    TemplateNormativeIndicator saveIndicator(UUID level3Id, TemplateNormativeIndicator indicator);

    TemplateNormativeIndicator updateIndicator(UUID level3Id, UUID indicatorId, TemplateNormativeIndicator indicator);

    void deleteIndicator(UUID level3Id, UUID indicatorId);

    List<TemplateNormativeIndicator> findIndicatorsUnderLevel1(UUID level1Id);

    UUID findTemplateIdByLevel1(UUID level1Id);

    UUID findTemplateIdByLevel2(UUID level2Id);

    UUID findTemplateIdByLevel3(UUID level3Id);

    UUID findTemplateIdByIndicator(UUID indicatorId);

    UUID findLevel1IdByLevel2(UUID level2Id);

    UUID findLevel2IdByLevel3(UUID level3Id);

    UUID findLevel3IdByIndicator(UUID indicatorId);
}
