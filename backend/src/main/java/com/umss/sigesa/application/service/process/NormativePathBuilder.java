package com.umss.sigesa.application.service.process;

import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.domain.model.Level2Node;
import com.umss.sigesa.domain.model.Level3Node;
import com.umss.sigesa.domain.model.NormativeIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

final class NormativePathBuilder {

    private NormativePathBuilder() {
    }

    static Optional<List<String>> buildPath(List<Level1Node> level1Nodes, UUID indicatorId) {
        if (level1Nodes == null) {
            return Optional.empty();
        }
        for (Level1Node level1 : level1Nodes) {
            Optional<List<String>> path = walkLevel1(level1, indicatorId);
            if (path.isPresent()) {
                return path;
            }
        }
        return Optional.empty();
    }

    private static Optional<List<String>> walkLevel1(Level1Node level1, UUID indicatorId) {
        if (level1.getLevel2Nodes() == null) {
            return Optional.empty();
        }
        for (Level2Node level2 : level1.getLevel2Nodes()) {
            Optional<List<String>> path = walkLevel2(level1.getName(), level2, indicatorId);
            if (path.isPresent()) {
                return path;
            }
        }
        return Optional.empty();
    }

    private static Optional<List<String>> walkLevel2(String level1Name, Level2Node level2, UUID indicatorId) {
        if (level2.getLevel3Nodes() == null) {
            return Optional.empty();
        }
        for (Level3Node level3 : level2.getLevel3Nodes()) {
            if (level3.getIndicators() == null) {
                continue;
            }
            for (NormativeIndicator indicator : level3.getIndicators()) {
                if (indicatorId.equals(indicator.getId())) {
                    List<String> path = new ArrayList<>();
                    path.add(level1Name);
                    path.add(level2.getName());
                    path.add(level3.getName());
                    path.add(indicator.getCode());
                    return Optional.of(path);
                }
            }
        }
        return Optional.empty();
    }
}
