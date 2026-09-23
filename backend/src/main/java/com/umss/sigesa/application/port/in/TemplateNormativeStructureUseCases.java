package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplateLevel2Node;
import com.umss.sigesa.domain.model.TemplateLevel3Node;
import com.umss.sigesa.domain.model.TemplateNormativeIndicator;

import java.math.BigDecimal;
import java.util.UUID;

public interface TemplateNormativeStructureUseCases {

    TemplateLevel1Node addLevel1(UUID templateId, String name, Integer order, String description);

    TemplateLevel1Node updateLevel1(UUID templateId, UUID level1Id, String name, Integer order, String description);

    void deleteLevel1(UUID templateId, UUID level1Id);

    TemplateLevel2Node addLevel2(UUID level1Id, String name, Integer order, String description);

    TemplateLevel2Node updateLevel2(UUID level2Id, String name, Integer order, String description);

    void deleteLevel2(UUID level2Id);

    TemplateLevel3Node addLevel3(UUID level2Id, String name, Integer order, String description);

    TemplateLevel3Node updateLevel3(UUID level3Id, String name, Integer order, String description);

    void deleteLevel3(UUID level3Id);

    TemplateNormativeIndicator addIndicator(UUID level3Id, String code, String description, BigDecimal weight,
                                            Integer order, String referenceUrl);

    TemplateNormativeIndicator updateIndicator(UUID indicatorId, String code, String description, BigDecimal weight,
                                               Integer order, String referenceUrl);

    void deleteIndicator(UUID indicatorId);
}
