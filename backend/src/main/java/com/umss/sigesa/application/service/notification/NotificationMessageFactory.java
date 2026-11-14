package com.umss.sigesa.application.service.notification;

import com.umss.sigesa.domain.model.notification.DomainNotificationCommand;
import com.umss.sigesa.domain.model.notification.NotificationEventType;

final class NotificationMessageFactory {

    private NotificationMessageFactory() {
    }

    static MessageBundle build(DomainNotificationCommand command) {
        String indicatorId = firstNonBlank(command.payloadValue("indicatorId"), command.aggregateId().toString());
        String observationId = command.payloadValue("observationId");
        String processId = command.processId() != null
                ? command.processId().toString()
                : command.payloadValue("processId");
        String level1Id = firstNonBlank(command.payloadValue("level1Id"), "");
        String programId = command.programId() != null
                ? command.programId().toString()
                : command.payloadValue("programId");

        String indicatorLabel = label(
                command.payloadValue("indicatorLabel"),
                command.payloadValue("indicatorCode"),
                shortId(indicatorId));
        String programLabel = label(
                command.payloadValue("programName"),
                command.payloadValue("careerName"),
                command.payloadValue("programCode"),
                shortId(programId));
        String processLabel = label(
                command.payloadValue("processLabel"),
                command.payloadValue("templateName"),
                shortId(processId));
        String level1Label = label(
                command.payloadValue("level1Name"),
                shortId(level1Id.isBlank() ? command.aggregateId().toString() : level1Id));

        return switch (command.eventType()) {
            case IndicatorRejected -> new MessageBundle(
                    "Atención: Indicador observado",
                    "El indicador " + indicatorLabel + " fue observado y requiere subsanación.",
                    "Indicador observado (" + indicatorLabel + ")",
                    "Se registró una observación sobre el indicador " + indicatorLabel
                            + " del programa " + programLabel
                            + " (proceso " + processLabel + ")."
                            + " Revise la justificación y cargue la evidencia de subsanación.",
                    deepLinkIndicator(processId, indicatorId, observationId));
            case IndicatorApproved -> new MessageBundle(
                    "Indicador aprobado",
                    "El indicador " + indicatorLabel + " fue aprobado.",
                    "Confirmación: Indicador aprobado (" + indicatorLabel + ")",
                    "El indicador " + indicatorLabel + " del programa " + programLabel
                            + " (proceso " + processLabel + ") cumple el estándar documental y fue aprobado.",
                    deepLinkIndicator(processId, indicatorId, null));
            case EvidenceUploaded -> new MessageBundle(
                    "Nueva evidencia",
                    "Se cargó evidencia para el indicador " + indicatorLabel + ".",
                    "Nueva evidencia registrada - Indicador " + indicatorLabel,
                    "Se ha registrado una nueva evidencia para el indicador " + indicatorLabel
                            + " en el programa " + programLabel + " (proceso " + processLabel + ").",
                    "/dashboard/technician/inbox?programId=" + programId + "&status=SUBIDO");
            case EvidenceSubsanated -> new MessageBundle(
                    "Evidencia subsanada",
                    "Se subsanó el indicador " + indicatorLabel + ".",
                    "Evidencia subsanada - Indicador " + indicatorLabel,
                    "Se cargó una versión de subsanación para el indicador " + indicatorLabel
                            + " del programa " + programLabel + " (proceso " + processLabel + ").",
                    "/dashboard/technician/inbox?programId=" + programId
                            + "&status=SUBSANADO"
                            + (observationId.isBlank() ? "" : "&observationId=" + observationId));
            case DeadlineApproaching -> new MessageBundle(
                    "Plazo próximo",
                    "Hay un plazo próximo en el programa " + programLabel + ".",
                    "Alerta de plazo próximo — " + programLabel,
                    "Existe un plazo de acreditación que vence pronto en el programa " + programLabel
                            + (indicatorLabel.isBlank() || "—".equals(indicatorLabel)
                            ? ""
                            : " (indicador " + indicatorLabel + ")")
                            + ". Revise el indicador o nivel asociado.",
                    deepLinkIndicator(processId, indicatorId, observationId));
            case ProcessClosed -> new MessageBundle(
                    "Proceso cerrado",
                    "El proceso de acreditación " + processLabel + " está cerrado.",
                    "Proceso de acreditación cerrado — " + processLabel,
                    "El proceso " + processLabel + " del programa " + programLabel
                            + " quedó cerrado. Consulte el semáforo ejecutivo.",
                    "/executive/semaphore?processId=" + processId);
            case Level1Completed -> new MessageBundle(
                    "Nivel 1 completado",
                    "Se completó el Nivel 1 " + level1Label + ".",
                    "Hito alcanzado: Dimensión completada — " + level1Label,
                    "El Nivel 1 " + level1Label + " del proceso " + processLabel
                            + " (programa " + programLabel + ") fue marcado como COMPLETADA.",
                    "/procesos/" + processId + "/nivel1/"
                            + (level1Id.isBlank() ? command.aggregateId() : level1Id));
            case ProcessAssigned -> new MessageBundle(
                    "Proceso asignado",
                    "Usted fue asignado como responsable del proceso " + processLabel + ".",
                    "Asignación de proceso — " + processLabel,
                    "Se le asignó como Coordinador responsable del proceso " + processLabel
                            + " del programa " + programLabel
                            + ". Ingrese al detalle del proceso para continuar la gestión documental.",
                    "/procesos/" + processId);
            case ProcessUnassigned -> new MessageBundle(
                    "Responsabilidad retirada",
                    "Ya no es responsable del proceso " + processLabel + ".",
                    "Retiro de responsabilidad — " + processLabel,
                    "Se le retiró como Coordinador responsable del proceso " + processLabel
                            + " del programa " + programLabel
                            + ". Esta es una notificación informativa; ya no tiene acceso de gestión a ese proceso.",
                    "");
        };
    }

    static String aggregateKey(DomainNotificationCommand command) {
        if (command.eventType() == NotificationEventType.DeadlineApproaching) {
            String day = command.payloadValue("deadlineDay");
            return command.aggregateId() + ":" + (day.isBlank() ? "undated" : day);
        }
        // Assign/unassign can repeat for the same process+user; include occurrenceId so each
        // action enqueues a fresh EMAIL (otherwise saveIfAbsent skips forever after first SENT).
        if (command.eventType() == NotificationEventType.ProcessAssigned
                || command.eventType() == NotificationEventType.ProcessUnassigned) {
            String occurrence = command.payloadValue("occurrenceId");
            if (occurrence != null && !occurrence.isBlank()) {
                return command.aggregateId() + ":" + occurrence.trim();
            }
        }
        return command.aggregateId().toString();
    }

    private static String deepLinkIndicator(String processId, String indicatorId, String observationId) {
        // Prefer process-scoped deep link; fallback keeps indicator id for legacy enqueue paths.
        String base = (processId == null || processId.isBlank())
                ? "/procesos?indicatorId=" + indicatorId
                : "/procesos/" + processId + "/indicadores/" + indicatorId;
        if (observationId != null && !observationId.isBlank()) {
            return base + "?observationId=" + observationId;
        }
        return base;
    }

    private static String label(String... candidates) {
        if (candidates == null) {
            return "—";
        }
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate.trim();
            }
        }
        return "—";
    }

    private static String shortId(String id) {
        if (id == null || id.isBlank()) {
            return "—";
        }
        return id.length() <= 8 ? id : id.substring(0, 8);
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback == null ? "" : fallback;
    }

    record MessageBundle(
            String inAppTitle,
            String inAppBody,
            String emailSubject,
            String emailBody,
            String deepLink
    ) {
    }
}
