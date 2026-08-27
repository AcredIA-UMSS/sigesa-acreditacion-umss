package com.umss.sigesa.application.service.template;

import com.umss.sigesa.domain.exception.TemplateOrderConflictException;
import com.umss.sigesa.domain.exception.TemplateStructureIncompleteException;
import com.umss.sigesa.domain.exception.TemplateSubphaseLinkRequiredException;
import com.umss.sigesa.domain.model.Template;
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

    public void validateForPublish(Template template) {
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
