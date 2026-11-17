package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.application.port.in.NotificationModulePort;
import com.umss.sigesa.domain.model.NotificationModuleSnapshot;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Ruta E3 que <strong>usa</strong> el módulo de notificaciones.
 * La ruta de control (no usa el módulo) es {@code GET /api/health}.
 */
@RestController
@RequestMapping("/api/v1/e3/notifications")
@Tag(name = "E3 notifications", description = "Experimento monolito vs servicio extraído")
public class NotificationE3Controller {

    private final NotificationModulePort notificationModulePort;
    private final Duration deadline;

    public NotificationE3Controller(
            NotificationModulePort notificationModulePort,
            @Value("${sigesa.extraction.deadline-ms:800}") int deadlineMs) {
        this.notificationModulePort = notificationModulePort;
        this.deadline = Duration.ofMillis(deadlineMs);
    }

    @GetMapping
    @Operation(summary = "Inventario del outbox vía NotificationModulePort (in_process o http)")
    public ResponseEntity<NotificationModuleSnapshot> inspect() {
        return ResponseEntity.ok(notificationModulePort.inspect(deadline));
    }
}
