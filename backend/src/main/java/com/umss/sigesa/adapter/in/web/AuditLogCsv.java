package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.domain.model.AuditEvent;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

final class AuditLogCsv {

    private static final ZoneId DISPLAY_ZONE = ZoneId.of("America/La_Paz");

    private AuditLogCsv() {
    }

    static String write(List<AuditEvent> events) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Fecha,Hora,Usuario,Acción,Módulo,Identificador,Descripción,Resultado,IP\n");
        for (AuditEvent event : events) {
            OffsetDateTime displayed = event.occurredAt() == null
                    ? null
                    : event.occurredAt().atZone(DISPLAY_ZONE).toOffsetDateTime();
            String date = displayed == null ? "" : displayed.toLocalDate().toString();
            String time = displayed == null ? "" : displayed.toLocalTime().toString();
            csv.append(cell(date)).append(',')
                    .append(cell(time)).append(',')
                    .append(cell(event.actorEmail())).append(',')
                    .append(cell(event.action().name())).append(',')
                    .append(cell(event.resourceType().name())).append(',')
                    .append(cell(event.resourceId() == null ? "" : event.resourceId().toString())).append(',')
                    .append(cell(event.description())).append(',')
                    .append(cell(event.result().name())).append(',')
                    .append(cell(event.ipAddress()))
                    .append('\n');
        }
        return csv.toString();
    }

    private static String cell(String value) {
        String raw = value == null ? "" : value;
        String guarded = raw.startsWith("=") || raw.startsWith("+") || raw.startsWith("-") || raw.startsWith("@")
                ? "'" + raw
                : raw;
        return "\"" + guarded.replace("\"", "\"\"") + "\"";
    }
}
