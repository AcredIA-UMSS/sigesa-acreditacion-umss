package com.umss.sigesa.application.service.template;

import com.umss.sigesa.domain.exception.TemplateIndicatorIncompleteException;
import com.umss.sigesa.domain.exception.TemplateOrderConflictException;
import com.umss.sigesa.domain.exception.TemplateStructureIncompleteException;
import com.umss.sigesa.domain.exception.TemplateSubphaseLinkRequiredException;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplateLevel2Node;
import com.umss.sigesa.domain.model.TemplateLevel3Node;
import com.umss.sigesa.domain.model.TemplateNormativeIndicator;
import com.umss.sigesa.domain.model.TemplatePhase;
import com.umss.sigesa.domain.model.TemplateSubphase;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class TemplateStructureValidator {

    private static final Pattern HTTPS_URL = Pattern.compile("^https://.+");

    public void validateType(String type) {
        if (type == null || type.isBlank()) {
            throw new TemplateStructureIncompleteException("El tipo de plantilla es obligatorio.");
        }
        String normalized = type.trim().toUpperCase();
        if (!normalized.equals("CEUB") && !normalized.equals("ARCU-SUR")) {
            throw new TemplateStructureIncompleteException("Tipo de plantilla no permitido. Solo CEUB o ARCU-SUR.");
        }
    }

    public void validateSubphaseLinks(Template template) {
        if (template.getPhases() == null) {
            return;
        }
        for (TemplatePhase phase : template.getPhases()) {
            if (phase.getSubphases() == null) {
                continue;
            }
            for (TemplateSubphase subphase : phase.getSubphases()) {
                ensureReferenceUrl(subphase.getReferenceUrl());
                ensureRequirements(subphase.getRequirements());
            }
        }
    }

    public void validateOrders(Template template) {
        if (template.getPhases() == null || template.getPhases().isEmpty()) {
            return;
        }
        Set<Integer> phaseOrders = new HashSet<>();
        for (TemplatePhase phase : template.getPhases()) {
            if (phase.getOrder() == null || !phaseOrders.add(phase.getOrder())) {
                throw new TemplateOrderConflictException("Orden de fase duplicado en la plantilla.");
            }
            validateSubphaseOrders(phase.getSubphases());
        }
    }

    public void validateForPublish(Template template, List<TemplateLevel1Node> level1Nodes,
                                   TemplateNormativeStructureGuard guard) {
        validateType(template.getType());
        validateNormativeTreeForPublish(level1Nodes, guard);
    }

    /**
     * Validación legacy fases/subfases (coexistencia M5). No usada en publicación v2.0.
     */
    public void validateLegacyPhasesForPublish(Template template) {
        validateType(template.getType());
        validateOrders(template);

        List<TemplatePhase> phases = template.getPhases();
        if (phases == null || phases.isEmpty()) {
            throw new TemplateStructureIncompleteException(
                    "La plantilla debe tener al menos una fase para publicarse.");
        }

        int subphaseCount = 0;
        for (TemplatePhase phase : phases) {
            List<TemplateSubphase> subphases = phase.getSubphases();
            if (subphases == null || subphases.isEmpty()) {
                throw new TemplateStructureIncompleteException(
                        "Cada fase debe tener al menos una subfase para publicarse.");
            }
            subphaseCount += subphases.size();
            for (TemplateSubphase subphase : subphases) {
                ensureReferenceUrl(subphase.getReferenceUrl());
                ensureRequirements(subphase.getRequirements());
            }
        }

        if (subphaseCount < 1) {
            throw new TemplateStructureIncompleteException(
                    "La plantilla debe tener al menos una subfase para publicarse.");
        }
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

    private void validateSubphaseOrders(List<TemplateSubphase> subphases) {
        if (subphases == null || subphases.isEmpty()) {
            return;
        }
        Set<Integer> subphaseOrders = new HashSet<>();
        for (TemplateSubphase subphase : subphases) {
            if (subphase.getOrder() == null || !subphaseOrders.add(subphase.getOrder())) {
                throw new TemplateOrderConflictException("Orden de subfase duplicado en la misma fase.");
            }
        }
    }

    private void ensureReferenceUrl(String referenceUrl) {
        if (referenceUrl == null || referenceUrl.isBlank() || !HTTPS_URL.matcher(referenceUrl.trim()).matches()) {
            throw new TemplateSubphaseLinkRequiredException(
                    "Cada subfase debe incluir un referenceUrl HTTPS válido.");
        }
    }

    private void ensureRequirements(String requirements) {
        if (requirements == null || requirements.isBlank()) {
            throw new TemplateSubphaseLinkRequiredException(
                    "Cada subfase debe incluir requisitos de completitud (requisitos_subfase).");
        }
    }
}
