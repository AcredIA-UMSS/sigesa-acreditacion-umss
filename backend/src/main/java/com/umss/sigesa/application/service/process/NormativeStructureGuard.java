package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.domain.exception.IndicatorIncompleteException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.exception.ProcessStructureOrderConflictException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.domain.model.Level2Node;
import com.umss.sigesa.domain.model.Level3Node;
import com.umss.sigesa.domain.model.NormativeIndicator;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class NormativeStructureGuard {

    private static final Pattern HTTPS_URL = Pattern.compile("^https://.+");

    private final ProcessStructureGuard processStructureGuard;

    public NormativeStructureGuard(ProcessStructureGuard processStructureGuard) {
        this.processStructureGuard = processStructureGuard;
    }

    public AccreditationProcess loadActiveProcess(ProcessQueryPort processQueryPort, UUID processId) {
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        processStructureGuard.ensureProcessActive(process);
        return process;
    }

    public void ensureIndicatorComplete(NormativeIndicator indicator) {
        if (indicator.getCode() == null || indicator.getCode().isBlank()) {
            throw new IndicatorIncompleteException("El indicador requiere code.");
        }
        if (indicator.getWeight() == null || indicator.getWeight().compareTo(BigDecimal.ZERO) < 0) {
            throw new IndicatorIncompleteException("El indicador requiere weight >= 0.");
        }
        if (indicator.getReferenceUrl() == null || indicator.getReferenceUrl().isBlank()
                || !HTTPS_URL.matcher(indicator.getReferenceUrl().trim()).matches()) {
            throw new IndicatorIncompleteException("El indicador requiere referenceUrl HTTPS válido.");
        }
        if (indicator.getDescription() == null || indicator.getDescription().isBlank()) {
            throw new IndicatorIncompleteException("El indicador requiere description.");
        }
        if (indicator.getOrder() == null) {
            throw new IndicatorIncompleteException("El indicador requiere order.");
        }
    }

    public void ensureUniqueLevel1Order(List<Level1Node> nodes, Integer order, UUID excludeId) {
        ensureUniqueOrder(order, excludeId, nodes, Level1Node::getId, Level1Node::getOrder, "Nivel 1");
    }

    public void ensureUniqueLevel2Order(List<Level2Node> nodes, Integer order, UUID excludeId) {
        ensureUniqueOrder(order, excludeId, nodes, Level2Node::getId, Level2Node::getOrder, "Nivel 2");
    }

    public void ensureUniqueLevel3Order(List<Level3Node> nodes, Integer order, UUID excludeId) {
        ensureUniqueOrder(order, excludeId, nodes, Level3Node::getId, Level3Node::getOrder, "Nivel 3");
    }

    public void ensureUniqueIndicatorOrder(List<NormativeIndicator> indicators, Integer order, UUID excludeId) {
        ensureUniqueOrder(order, excludeId, indicators, NormativeIndicator::getId, NormativeIndicator::getOrder,
                "Indicador");
    }

    private <T> void ensureUniqueOrder(Integer order, UUID excludeId, List<T> nodes,
                                       java.util.function.Function<T, UUID> idFn,
                                       java.util.function.Function<T, Integer> orderFn,
                                       String label) {
        if (order == null) {
            throw new ProcessStructureOrderConflictException("El orden de " + label + " es obligatorio.");
        }
        if (nodes != null) {
            for (T node : nodes) {
                if (excludeId != null && excludeId.equals(idFn.apply(node))) {
                    continue;
                }
                if (order.equals(orderFn.apply(node))) {
                    throw new ProcessStructureOrderConflictException("Orden duplicado en " + label + ".");
                }
            }
        }
    }
}
