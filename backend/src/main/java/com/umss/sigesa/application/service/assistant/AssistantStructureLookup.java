package com.umss.sigesa.application.service.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.umss.sigesa.application.model.process.EnrichedProcessDetail;
import com.umss.sigesa.domain.model.Phase;
import com.umss.sigesa.domain.model.Subphase;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resuelve fases y subfases en tools del asistente tolerando placeholders del LLM
 * (p. ej. {@code UUID_FASE_1}) y referencias naturales ({@code Fase 1}).
 */
final class AssistantStructureLookup {

    private static final Pattern ORDER_HINT = Pattern.compile(
            "(?:fase|phase|uuid_fase)[\\s_-]*(\\d+)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private AssistantStructureLookup() {
    }

    static Phase findPhase(JsonNode args, EnrichedProcessDetail detail) {
        if (detail.phases() == null || detail.phases().isEmpty()) {
            throw new IllegalArgumentException("El proceso no tiene fases.");
        }

        if (args.hasNonNull("phaseOrder")) {
            int order = args.get("phaseOrder").asInt();
            return findPhaseByOrder(detail.phases(), order);
        }

        if (args.hasNonNull("phaseId")) {
            Phase byId = resolvePhaseReference(args.get("phaseId").asText(), detail.phases());
            if (byId != null) {
                return byId;
            }
        }

        if (args.hasNonNull("phaseName")) {
            Phase byName = resolvePhaseReference(args.get("phaseName").asText(), detail.phases());
            if (byName != null) {
                return byName;
            }
        }

        throw new IllegalArgumentException(
                "No se encontró la fase. Use phaseOrder (1, 2, …), phaseName exacto o phaseId UUID real "
                        + "obtenido de list_process_structure. No invente identificadores.");
    }

    static Subphase findSubphase(JsonNode args, Phase phase) {
        if (phase.getSubphases() == null || phase.getSubphases().isEmpty()) {
            throw new IllegalArgumentException("La fase no tiene subfases.");
        }

        if (args.hasNonNull("subphaseId")) {
            Subphase byId = resolveSubphaseReference(args.get("subphaseId").asText(), phase.getSubphases());
            if (byId != null) {
                return byId;
            }
        }

        if (args.hasNonNull("subphaseName")) {
            Subphase byName = resolveSubphaseReference(args.get("subphaseName").asText(), phase.getSubphases());
            if (byName != null) {
                return byName;
            }
        }

        throw new IllegalArgumentException(
                "No se encontró la subfase. Use subphaseName o subphaseId UUID real. No invente identificadores.");
    }

    /**
     * Calcula el siguiente orden libre para CREATE. Una sola pasada (max + 1), sin reintentos.
     */
    static SubphaseOrderPlan planCreateSubphaseOrder(Phase phase, Integer llmRequestedOrder) {
        List<Subphase> subphases = phase.getSubphases() == null ? List.of() : phase.getSubphases();
        int existingCount = subphases.size();
        int maxExistingOrder = subphases.stream()
                .map(Subphase::getOrder)
                .filter(order -> order != null)
                .max(Comparator.naturalOrder())
                .orElse(0);
        int assignedOrder = maxExistingOrder + 1;

        Integer ignoredOrder = null;
        if (llmRequestedOrder != null && !llmRequestedOrder.equals(assignedOrder)) {
            ignoredOrder = llmRequestedOrder;
        }

        return new SubphaseOrderPlan(existingCount, maxExistingOrder, assignedOrder, ignoredOrder);
    }

    static String buildPhaseCatalogPrompt(EnrichedProcessDetail detail) {
        if (detail.phases() == null || detail.phases().isEmpty()) {
            return "(sin fases)";
        }
        StringBuilder sb = new StringBuilder();
        detail.phases().stream()
                .sorted(Comparator.comparing(Phase::getOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .forEach(phase -> sb.append("- order=")
                        .append(phase.getOrder())
                        .append(" phaseId=")
                        .append(phase.getId())
                        .append(" name=\"")
                        .append(phase.getName())
                        .append("\"\n"));
        return sb.toString().trim();
    }

    private static Phase resolvePhaseReference(String raw, List<Phase> phases) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();

        Optional<UUID> uuid = parseUuid(trimmed);
        if (uuid.isPresent()) {
            return phases.stream()
                    .filter(phase -> phase.getId().equals(uuid.get()))
                    .findFirst()
                    .orElse(null);
        }

        Optional<Integer> orderHint = extractOrderHint(trimmed);
        if (orderHint.isPresent()) {
            try {
                return findPhaseByOrder(phases, orderHint.get());
            } catch (IllegalArgumentException ignored) {
                // fall through to name match
            }
        }

        return findPhaseByName(trimmed, phases);
    }

    private static Subphase resolveSubphaseReference(String raw, List<Subphase> subphases) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();

        Optional<UUID> uuid = parseUuid(trimmed);
        if (uuid.isPresent()) {
            return subphases.stream()
                    .filter(subphase -> subphase.getId().equals(uuid.get()))
                    .findFirst()
                    .orElse(null);
        }

        String normalized = trimmed.toLowerCase(Locale.ROOT);
        List<Subphase> matches = subphases.stream()
                .filter(subphase -> subphase.getName() != null
                        && subphase.getName().trim().toLowerCase(Locale.ROOT).equals(normalized))
                .toList();
        if (matches.size() == 1) {
            return matches.getFirst();
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException("Hay varias subfases con ese nombre. Indique subphaseId.");
        }
        return null;
    }

    private static Phase findPhaseByOrder(List<Phase> phases, int order) {
        List<Phase> matches = phases.stream()
                .filter(phase -> phase.getOrder() != null && phase.getOrder() == order)
                .toList();
        if (matches.isEmpty()) {
            throw new IllegalArgumentException("No hay fase con order=" + order + ".");
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException("Hay varias fases con order=" + order + ". Indique phaseId.");
        }
        return matches.getFirst();
    }

    private static Phase findPhaseByName(String raw, List<Phase> phases) {
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        List<Phase> matches = phases.stream()
                .filter(phase -> phase.getName() != null
                        && phase.getName().trim().toLowerCase(Locale.ROOT).equals(normalized))
                .toList();
        if (matches.isEmpty()) {
            return null;
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException("Hay varias fases con ese nombre. Indique phaseOrder o phaseId.");
        }
        return matches.getFirst();
    }

    private static Optional<Integer> extractOrderHint(String raw) {
        Matcher matcher = ORDER_HINT.matcher(raw.trim());
        if (matcher.find()) {
            return Optional.of(Integer.parseInt(matcher.group(1)));
        }
        return Optional.empty();
    }

    private static Optional<UUID> parseUuid(String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    record SubphaseOrderPlan(
            int existingCount,
            int maxExistingOrder,
            int assignedOrder,
            Integer ignoredLlmOrder
    ) {

        String confirmationMessage(String phaseName, String subphaseName, String referenceUrl) {
            StringBuilder sb = new StringBuilder();
            if (existingCount == 0) {
                sb.append("La fase «").append(phaseName)
                        .append("» no tiene subfases todavía. Se creará «")
                        .append(subphaseName).append("» con **orden 1** (primera subfase).");
            } else {
                sb.append("La fase «").append(phaseName).append("» tiene **")
                        .append(existingCount).append("** subfase(s) (último orden: **")
                        .append(maxExistingOrder).append("**). Se creará «")
                        .append(subphaseName).append("» con **orden ")
                        .append(assignedOrder).append("** (siguiente disponible).");
            }
            if (ignoredLlmOrder != null) {
                sb.append("\n(Se ignoró order=").append(ignoredLlmOrder)
                        .append(" propuesto por el modelo; «Fase N» es la fase contenedora, no el orden de subfase.)");
            }
            sb.append("\nEnlace: ").append(referenceUrl);
            return sb.toString();
        }
    }
}
