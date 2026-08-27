package com.umss.sigesa.application.service.process;

import com.umss.sigesa.domain.exception.ProcessNotEditableException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.exception.ProcessStructureOrderConflictException;
import com.umss.sigesa.domain.exception.SubphaseLinkRequiredException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Phase;
import com.umss.sigesa.domain.model.Subphase;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class ProcessStructureGuard {

    private static final Pattern HTTPS_URL = Pattern.compile("^https://.+");

    public void ensureProcessActive(AccreditationProcess process) {
        if (process == null || !"ACTIVE".equals(process.getStatus())) {
            throw new ProcessNotEditableException(
                    "La estructura del proceso solo puede editarse mientras el proceso está ACTIVE.");
        }
    }

    public void ensureRequirements(String requirements) {
        if (requirements == null || requirements.isBlank()) {
            throw new SubphaseLinkRequiredException(
                    "Cada subfase debe incluir requisitos de completitud (requisitos_subfase).");
        }
    }

    public void ensureReferenceUrl(String referenceUrl) {
        if (referenceUrl == null || referenceUrl.isBlank()
                || !HTTPS_URL.matcher(referenceUrl.trim()).matches()) {
            throw new SubphaseLinkRequiredException(
                    "Cada subfase debe incluir un referenceUrl HTTPS válido.");
        }
    }

    public void ensureUniquePhaseOrder(AccreditationProcess process, Integer order, UUID excludePhaseId) {
        if (order == null) {
            throw new ProcessStructureOrderConflictException("El orden de la fase es obligatorio.");
        }
        Set<Integer> orders = new HashSet<>();
        for (Phase phase : process.getPhases()) {
            if (excludePhaseId != null && excludePhaseId.equals(phase.getId())) {
                continue;
            }
            if (phase.getOrder() == null || !orders.add(phase.getOrder())) {
                throw new ProcessStructureOrderConflictException("Orden de fase duplicado en el proceso.");
            }
        }
        if (!orders.add(order)) {
            throw new ProcessStructureOrderConflictException("Orden de fase duplicado en el proceso.");
        }
    }

    public void ensureUniqueSubphaseOrder(Phase phase, Integer order, UUID excludeSubphaseId) {
        if (order == null) {
            throw new ProcessStructureOrderConflictException("El orden de la subfase es obligatorio.");
        }
        Set<Integer> orders = new HashSet<>();
        List<Subphase> subphases = phase.getSubphases();
        if (subphases != null) {
            for (Subphase subphase : subphases) {
                if (excludeSubphaseId != null && excludeSubphaseId.equals(subphase.getId())) {
                    continue;
                }
                if (subphase.getOrder() == null || !orders.add(subphase.getOrder())) {
                    throw new ProcessStructureOrderConflictException(
                            "Orden de subfase duplicado en la misma fase.");
                }
            }
        }
        if (!orders.add(order)) {
            throw new ProcessStructureOrderConflictException("Orden de subfase duplicado en la misma fase.");
        }
    }

    public Phase findPhase(AccreditationProcess process, UUID phaseId) {
        return process.getPhases().stream()
                .filter(phase -> phaseId.equals(phase.getId()))
                .findFirst()
                .orElseThrow(() -> new ProcessNotFoundException("Fase no encontrada en el proceso: " + phaseId));
    }

    public Subphase findSubphase(Phase phase, UUID subphaseId) {
        return phase.getSubphases().stream()
                .filter(subphase -> subphaseId.equals(subphase.getId()))
                .findFirst()
                .orElseThrow(() -> new ProcessNotFoundException("Subfase no encontrada en la fase: " + subphaseId));
    }
}
