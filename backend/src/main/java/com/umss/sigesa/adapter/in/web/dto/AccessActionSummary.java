package com.umss.sigesa.adapter.in.web.dto;

import com.umss.sigesa.adapter.out.persistance.entity.AuditLogJpaEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Etiquetas de creación, modificación y eliminación para la columna del reporte.
 * Consultas, ingresos y salidas no se listan aquí: ya tienen columnas propias.
 */
public final class AccessActionSummary {

    private static final int MAX_LABELS = 8;

    private AccessActionSummary() {
    }

    public static Map<UUID, List<String>> bySession(List<AuditLogJpaEntity> entries) {
        Map<UUID, List<String>> grouped = new LinkedHashMap<>();
        if (entries == null) {
            return grouped;
        }
        for (AuditLogJpaEntity entry : entries) {
            if (entry.getSessionId() == null) {
                continue;
            }
            String label = labelOf(entry);
            if (label == null) {
                continue;
            }
            List<String> labels = grouped.computeIfAbsent(entry.getSessionId(), ignored -> new ArrayList<>());
            if (!labels.contains(label) && labels.size() < MAX_LABELS) {
                labels.add(label);
            }
        }
        return grouped;
    }

    static String labelOf(AuditLogJpaEntity entry) {
        String action = entry.getAction() == null ? "" : entry.getAction();
        return switch (action) {
            case "USER_REGISTERED" -> "Crear usuario";
            case "USER_ACTIVATED" -> "Activar usuario";
            case "USER_DEACTIVATED" -> "Desactivar usuario";
            case "EVIDENCE_UPLOADED" -> "Crear evidencia";
            case "EVIDENCE_RETRACTED" -> "Eliminar evidencia";
            case "DELETE_DENIED" -> "Eliminar evidencia denegado";
            case "REPORT_REQUESTED" -> "Generar reporte";
            case "CREATE" -> "Crear " + elementName(entry.getEntityType());
            case "UPDATE" -> "Modificar " + elementName(entry.getEntityType());
            case "DELETE" -> "Eliminar " + elementName(entry.getEntityType());
            default -> null;
        };
    }

    private static String elementName(String entityType) {
        if (entityType == null || entityType.isBlank()) {
            return "elemento";
        }
        return switch (entityType.toUpperCase(Locale.ROOT)) {
            case "USUARIOS", "USER" -> "usuario";
            case "PROCESOS", "PROCESS" -> "proceso";
            case "EVIDENCIAS", "EVIDENCE", "DOCUMENT" -> "evidencia";
            case "REPORTES", "REPORT" -> "reporte";
            case "DICTAMENES", "DICTAMEN" -> "dictamen";
            case "EVALUACIONES", "EVALUATION" -> "evaluación";
            case "NOTICIAS", "NEWS" -> "noticia";
            default -> "elemento";
        };
    }
}
