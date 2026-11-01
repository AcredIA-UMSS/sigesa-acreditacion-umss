package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.domain.model.Level2Node;
import com.umss.sigesa.domain.model.Level3Node;
import com.umss.sigesa.domain.model.NormativeIndicator;

import java.math.BigDecimal;
import java.util.UUID;

public interface NormativeStructureUseCases {

    Level1Node addLevel1(UUID processId, String name, Integer order, String description);

    Level1Node updateLevel1(UUID processId, UUID level1Id, String name, Integer order, String description);

    void deleteLevel1(UUID processId, UUID level1Id);

    Level2Node addLevel2(UUID level1Id, String name, Integer order, String description);

    Level2Node updateLevel2(UUID level2Id, String name, Integer order, String description);

    void deleteLevel2(UUID level2Id);

    Level3Node addLevel3(UUID level2Id, String name, Integer order, String description);

    Level3Node updateLevel3(UUID level3Id, String name, Integer order, String description);

    void deleteLevel3(UUID level3Id);

    NormativeIndicator addIndicator(UUID level3Id, String code, String description, BigDecimal weight,
                                   Integer order, String referenceUrl);

    NormativeIndicator updateIndicator(UUID indicatorId, String code, String description, BigDecimal weight,
                                       Integer order, String referenceUrl);

    void deleteIndicator(UUID indicatorId);
}
