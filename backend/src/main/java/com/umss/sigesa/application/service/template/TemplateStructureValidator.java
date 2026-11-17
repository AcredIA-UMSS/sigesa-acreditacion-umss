package com.umss.sigesa.application.service.template;

import com.umss.sigesa.domain.exception.TemplateOrderConflictException;
import com.umss.sigesa.domain.exception.TemplateStructureIncompleteException;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplateLevel2Node;
import com.umss.sigesa.domain.model.TemplateLevel3Node;
import com.umss.sigesa.domain.model.TemplateNormativeIndicator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TemplateStructureValidator {

    public void validateType(String type) {
        if (type == null || type.isBlank()) {
            throw new TemplateStructureIncompleteException("El tipo de plantilla es obligatorio.");
        }
        String normalized = type.trim().toUpperCase();
        if (!normalized.equals("CEUB") && !normalized.equals("ARCU-SUR")) {
            throw new TemplateStructureIncompleteException("Tipo de plantilla no permitido. Solo CEUB o ARCU-SUR.");
        }
    }

    public void validateForPublish(Template template, List<TemplateLevel1Node> level1Nodes,
                                   TemplateNormativeStructureGuard guard) {
        validateType(template.getType());
        validateNormativeTreeForPublish(level1Nodes, guard);
    }

    public void validateNormativeTreeForPublish(List<TemplateLevel1Node> level1Nodes,
                                                TemplateNormativeStructureGuard guard) {
        if (level1Nodes == null || level1Nodes.isEmpty()) {
            throw new TemplateStructureIncompleteException(
                    "La plantilla debe tener al menos un indicador en el árbol normativo v2 para publicarse.");
        }

        validateNormativeOrders(level1Nodes);

        for (TemplateLevel1Node level1 : level1Nodes) {
            List<TemplateLevel2Node> level2Nodes = level1.getLevel2Nodes();
            if (level2Nodes != null) {
                for (TemplateLevel2Node level2 : level2Nodes) {
                    List<TemplateLevel3Node> level3Nodes = level2.getLevel3Nodes();
                    if (level3Nodes != null) {
                        for (TemplateLevel3Node level3 : level3Nodes) {
                            List<TemplateNormativeIndicator> indicators = level3.getIndicators();
                            if (indicators != null) {
                                for (TemplateNormativeIndicator indicator : indicators) {
                                    guard.ensureIndicatorComplete(indicator);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (countIndicators(level1Nodes) < 1) {
            throw new TemplateStructureIncompleteException(
                    "La plantilla debe tener al menos un indicador en el árbol normativo v2 para publicarse.");
        }
    }

    private void validateNormativeOrders(List<TemplateLevel1Node> level1Nodes) {
        Set<Integer> level1Orders = new HashSet<>();
        for (TemplateLevel1Node level1 : level1Nodes) {
            if (level1.getOrder() == null || !level1Orders.add(level1.getOrder())) {
                throw new TemplateOrderConflictException("Orden duplicado en Nivel 1.");
            }
            validateLevel2Orders(level1.getLevel2Nodes());
        }
    }

    private void validateLevel2Orders(List<TemplateLevel2Node> level2Nodes) {
        if (level2Nodes == null || level2Nodes.isEmpty()) {
            return;
        }
        Set<Integer> level2Orders = new HashSet<>();
        for (TemplateLevel2Node level2 : level2Nodes) {
            if (level2.getOrder() == null || !level2Orders.add(level2.getOrder())) {
                throw new TemplateOrderConflictException("Orden duplicado en Nivel 2.");
            }
            validateLevel3Orders(level2.getLevel3Nodes());
        }
    }

    private void validateLevel3Orders(List<TemplateLevel3Node> level3Nodes) {
        if (level3Nodes == null || level3Nodes.isEmpty()) {
            return;
        }
        Set<Integer> level3Orders = new HashSet<>();
        for (TemplateLevel3Node level3 : level3Nodes) {
            if (level3.getOrder() == null || !level3Orders.add(level3.getOrder())) {
                throw new TemplateOrderConflictException("Orden duplicado en Nivel 3.");
            }
            validateIndicatorOrders(level3.getIndicators());
        }
    }

    private void validateIndicatorOrders(List<TemplateNormativeIndicator> indicators) {
        if (indicators == null || indicators.isEmpty()) {
            return;
        }
        Set<Integer> indicatorOrders = new HashSet<>();
        for (TemplateNormativeIndicator indicator : indicators) {
            if (indicator.getOrder() == null || !indicatorOrders.add(indicator.getOrder())) {
                throw new TemplateOrderConflictException("Orden duplicado en Indicador.");
            }
        }
    }

    private int countIndicators(List<TemplateLevel1Node> level1Nodes) {
        int count = 0;
        for (TemplateLevel1Node level1 : level1Nodes) {
            if (level1.getLevel2Nodes() == null) {
                continue;
            }
            for (TemplateLevel2Node level2 : level1.getLevel2Nodes()) {
                if (level2.getLevel3Nodes() == null) {
                    continue;
                }
                for (TemplateLevel3Node level3 : level2.getLevel3Nodes()) {
                    if (level3.getIndicators() != null) {
                        count += level3.getIndicators().size();
                    }
                }
            }
        }
        return count;
    }
}
