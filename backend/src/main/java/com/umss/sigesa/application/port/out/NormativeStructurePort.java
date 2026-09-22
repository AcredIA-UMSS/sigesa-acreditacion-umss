package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.domain.model.Level2Node;
import com.umss.sigesa.domain.model.Level3Node;
import com.umss.sigesa.domain.model.NormativeIndicator;

import java.util.List;
import java.util.UUID;

/**
 * Persistencia de mutaciones sobre el árbol normativo v2 en proceso ACTIVE.
 */
public interface NormativeStructurePort {

    Level1Node saveLevel1(UUID processId, Level1Node node);

    Level1Node updateLevel1(UUID processId, UUID level1Id, Level1Node node);

    void deleteLevel1(UUID processId, UUID level1Id);

    Level2Node saveLevel2(UUID level1Id, Level2Node node);

    Level2Node updateLevel2(UUID level1Id, UUID level2Id, Level2Node node);

    void deleteLevel2(UUID level1Id, UUID level2Id);

    Level3Node saveLevel3(UUID level2Id, Level3Node node);

    Level3Node updateLevel3(UUID level2Id, UUID level3Id, Level3Node node);

    void deleteLevel3(UUID level2Id, UUID level3Id);

    NormativeIndicator saveIndicator(UUID level3Id, NormativeIndicator indicator);

    NormativeIndicator updateIndicator(UUID level3Id, UUID indicatorId, NormativeIndicator indicator);

    void deleteIndicator(UUID level3Id, UUID indicatorId);

    List<NormativeIndicator> findIndicatorsUnderLevel1(UUID level1Id);

    UUID findProcessIdByLevel1(UUID level1Id);

    UUID findProcessIdByLevel2(UUID level2Id);

    UUID findProcessIdByLevel3(UUID level3Id);

    UUID findProcessIdByIndicator(UUID indicatorId);

    UUID findLevel1IdByLevel2(UUID level2Id);

    UUID findLevel2IdByLevel3(UUID level3Id);

    UUID findLevel3IdByIndicator(UUID indicatorId);
}
