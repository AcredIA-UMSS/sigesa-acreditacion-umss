package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.NotificationModuleSnapshot;

import java.time.Duration;

/**
 * Contrato del módulo de notificaciones (E3). Quien lo usa no sabe si corre
 * en proceso o por HTTP. {@code deadline} es el presupuesto total de la llamada
 * (equivalente a AbortSignal en la plantilla Nest).
 */
public interface NotificationModulePort {

    NotificationModuleSnapshot inspect(Duration deadline);
}
