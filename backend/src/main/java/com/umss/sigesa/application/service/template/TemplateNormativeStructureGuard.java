package com.umss.sigesa.application.service.template;

import com.umss.sigesa.application.port.out.TemplateManagementPort;
import com.umss.sigesa.application.service.process.NormativeStructureGuard;
import com.umss.sigesa.domain.exception.TemplateIndicatorIncompleteException;
import com.umss.sigesa.domain.exception.TemplateNotEditableException;
import com.umss.sigesa.domain.exception.TemplateNotFoundException;
import com.umss.sigesa.domain.exception.TemplateOrderConflictException;
import com.umss.sigesa.domain.model.NormativeIndicator;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplateLevel2Node;
import com.umss.sigesa.domain.model.TemplateLevel3Node;
import com.umss.sigesa.domain.model.TemplateNormativeIndicator;
import com.umss.sigesa.domain.model.TemplateStatus;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class TemplateNormativeStructureGuard {

    private final NormativeStructureGuard normativeStructureGuard;

    public TemplateNormativeStructureGuard(NormativeStructureGuard normativeStructureGuard) {
        this.normativeStructureGuard = normativeStructureGuard;
    }

    public Template loadDraftTemplate(TemplateManagementPort templateManagementPort, UUID templateId) {
        Template template = templateManagementPort.findByIdForEdit(templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Plantilla no encontrada con ID: " + templateId));
        if (template.getStatus() != TemplateStatus.DRAFT) {
            throw new TemplateNotEditableException(
                    "Solo se puede editar la estructura normativa de plantillas en estado DRAFT.");
        }
        return template;
    }

    public void ensureIndicatorComplete(TemplateNormativeIndicator indicator) {
        try {
            normativeStructureGuard.ensureIndicatorComplete(NormativeIndicator.builder()
                    .code(indicator.getCode())
                    .description(indicator.getDescription())
                    .weight(indicator.getWeight())
                    .order(indicator.getOrder())
                    .referenceUrl(indicator.getReferenceUrl())
                    .build());
        } catch (com.umss.sigesa.domain.exception.IndicatorIncompleteException ex) {
            throw new TemplateIndicatorIncompleteException(ex.getMessage());
        }
    }

    public void ensureUniqueLevel1Order(List<TemplateLevel1Node> nodes, Integer order, UUID excludeId) {
        ensureUniqueOrder(order, excludeId, nodes, TemplateLevel1Node::getId, TemplateLevel1Node::getOrder, "Nivel 1");
    }

    public void ensureUniqueLevel2Order(List<TemplateLevel2Node> nodes, Integer order, UUID excludeId) {
        ensureUniqueOrder(order, excludeId, nodes, TemplateLevel2Node::getId, TemplateLevel2Node::getOrder, "Nivel 2");
    }

    public void ensureUniqueLevel3Order(List<TemplateLevel3Node> nodes, Integer order, UUID excludeId) {
        ensureUniqueOrder(order, excludeId, nodes, TemplateLevel3Node::getId, TemplateLevel3Node::getOrder, "Nivel 3");
    }

    public void ensureUniqueIndicatorOrder(List<TemplateNormativeIndicator> indicators, Integer order, UUID excludeId) {
        ensureUniqueOrder(order, excludeId, indicators, TemplateNormativeIndicator::getId,
                TemplateNormativeIndicator::getOrder, "Indicador");
    }

    private <T> void ensureUniqueOrder(Integer order, UUID excludeId, List<T> nodes,
                                       Function<T, UUID> idFn,
                                       Function<T, Integer> orderFn,
                                       String label) {
        if (order == null) {
            throw new TemplateOrderConflictException("El orden de " + label + " es obligatorio.");
        }
        if (nodes != null) {
            for (T node : nodes) {
                if (excludeId != null && excludeId.equals(idFn.apply(node))) {
                    continue;
                }
                if (order.equals(orderFn.apply(node))) {
                    throw new TemplateOrderConflictException("Orden duplicado en " + label + ".");
                }
            }
        }
    }
}
