# ADR_010: Coreografía Event-Driven con AWS EventBridge para desacoplamiento de servicios

| Campo | Valor |
|-------|-------|
| **Canonico** | [ADR-0010](../../adr/ADR-0010-event-driven-choreography.md) |
| **Estado** | **Aceptada** |
| **Fecha** | 2026-05-25 |
| **Alcance** | Evidence Service · Audit Service · Orchestration Service · Notification Service |
| **Trazabilidad** | `hybrid_architecture.md` §2 · `04_state_machine.md` §3 · `02_parte_dificil.txt` Resultado esperado |
| **Supersede** | Supersede parcialmente ADR_002 para v1.0 cloud; mantiene el principio de modularidad interna |

## Contexto

La arquitectura monolito modular adoptada en ADR_002 fue la base inicial. La decisión institucional actual promueve la arquitectura cloud distribuida como v1.0 oficial. El ciclo crítico documentado en `02_parte_dificil.txt` —el "ida y vuelta" de Evidence entre [CC] y [TD]— genera tres problemas operativos que justifican separar responsabilidades mediante eventos:

**Problema 1: Acoplamiento de responsabilidades.** En el monolito, el controlador HTTP que recibe la carga de Evidence también actualiza el estado del Indicator. Esto mezcla dos dominios que el FSD y la máquina de estados tratan como separados: la gestión del archivo binario (responsabilidad del CC) y la validación normativa del contenido (responsabilidad del TD). Si la lógica de validación cambia (ej. nueva normativa ARCU-SUR), el módulo de carga debe modificarse, aumentando la superficie de riesgo.

**Problema 2: Notificaciones síncronas frágiles.** El envío de alertas SMTP al [TD] tras una carga de Evidence ocurre dentro de la misma transacción HTTP del monolito. Un fallo del servidor SMTP bloquea la respuesta al [CC] o, peor, confirma la carga pero no envía la notificación. En el contexto institucional de la UMSS, donde los plazos de subsanación son fatales, una notificación perdida puede equivaler a una carrera que no avanza en el proceso.

**Problema 3: Escalabilidad durante picos.** Las semanas de cierre de acreditación concentran la actividad de múltiples carreras simultáneamente. Un monolito con procesamiento síncrono de notificaciones, validaciones y actualizaciones de estado en la misma solicitud HTTP introduce latencia acumulada y riesgo de timeout.

La decisión documentada en este ADR establece el patrón de comunicación entre servicios para SIGESA v1.0 cloud. ADR_002 queda como antecedente de modularidad interna, no como arquitectura de despliegue vigente.

## Alternativas consideradas

| Alternativa | Pros | Contras | Veredicto |
|-------------|------|---------|-----------|
| **A. Coreografía con AWS EventBridge** | Desacoplamiento total; routing basado en patrones de evento; fanout nativo; managed service | Requiere infraestructura AWS; debugging de flujos async más complejo | **Elegida** |
| **B. Orquestación con AWS Step Functions** | Visibilidad de flujo; reintentos configurables | Acoplamiento central: el orquestador conoce todos los pasos; cambios en un servicio requieren actualizar el orquestador | Rechazada |
| **C. Llamadas HTTP directas entre servicios (REST sincrónico)** | Simple de implementar; traza HTTP directa | Acoplamiento fuerte; si Audit Service falla, Evidence Service no puede responder al CC | Rechazada |
| **D. Apache Kafka** | Alto throughput; replay de eventos | Over-engineering para el volumen de SIGESA; costo operativo elevado para UMSS | Rechazada |

## Decision

Se adopta **coreografía con AWS EventBridge** como patrón de comunicación inter-servicio, con las siguientes reglas:

**Regla 1 — Frontera de responsabilidad por evento.** Cada servicio publica eventos al completar su responsabilidad y no invoca directamente a otros servicios. El contrato de evento es la única interfaz pública entre servicios.

| Servicio emisor | Evento | Servicios consumidores |
|-----------------|--------|----------------------|
| Evidence Service | `EvidenceUploaded` | Audit Service, Notification Service |
| Evidence Service | `EvidenceSubsanated` | Audit Service, Notification Service |
| Audit Service | `IndicatorApproved` | Orchestration Service, Notification Service |
| Audit Service | `IndicatorObserved` | Notification Service |
| Orchestration Service | `PhaseCompleted` | Notification Service |

**Regla 2 — Evidence Service no actualiza estado de Indicator.** Este es el invariante de desacoplamiento más importante. Evidence Service publica `EvidenceUploaded` y termina. La transición del Indicator de PENDIENTE a SUBIDO es responsabilidad de Audit Service al consumir ese evento. Esta separación garantiza que una refactorización del módulo de carga no afecte la máquina de estados.

**Regla 3 — Esquema de evento mínimo y versionado.** Cada evento incluye `type`, `version` (del esquema de evento), `timestamp`, `correlationId` (para trazabilidad end-to-end) y el payload específico del dominio. Los consumidores validan `version` antes de procesar.

**Regla 4 — Idempotencia obligatoria en consumidores.** Dado que EventBridge garantiza entrega at-least-once, cada handler de evento debe ser idempotente: procesar el mismo evento dos veces produce el mismo resultado. Se implementa mediante verificación de `correlationId` en tabla `processed_events` antes de aplicar cambios.

## Consecuencias

### Positivas

- Evidence Service y Audit Service pueden evolucionar de forma independiente sin coordinar despliegues.
- Notification Service puede agregar nuevos canales (ej. webhook, SMS) sin modificar los servicios de negocio.
- El event log en EventBridge proporciona trazabilidad temporal completa de cada acción del sistema, cumpliendo el requisito de auditoría normativa.

### Negativas

- La consistencia eventual reemplaza la consistencia inmediata: entre la publicación de `EvidenceUploaded` y la actualización del estado por Audit Service existe una ventana de milisegundos en la que el estado del Indicator es PENDIENTE aunque la Evidence ya exista en RDS y S3.
- El debugging de flujos fallidos requiere herramientas adicionales (CloudWatch Logs, X-Ray) comparado con el stack trace síncrono del monolito.

### Impacto en actores

| Actor | Efecto |
|-------|--------|
| **[CC]** | Respuesta HTTP inmediata tras carga; no espera validación ni SMTP |
| **[TD]** | Notificación llega en segundos sin bloquear al CC; procesamiento async |
| **[JD]** | Dashboard puede mostrar estado "SUBIDO - pendiente validación" durante la ventana async |

## Validacion

- Integration test: publicar evento `EvidenceUploaded` sintético en EventBridge de staging; verificar que Audit Service inserta registro en `indicator_state_history` y Notification Service llama al stub SMTP en menos de 2s.
- Idempotencia: enviar el mismo `correlationId` dos veces; verificar que `indicator_state_history` contiene exactamente un registro.
- Boundary test: Evidence Service no debe importar ni referenciar ninguna tabla o interfaz de Audit Service; verificado en CI mediante análisis de dependencias (`madge` o equivalente).

## Referencias

- [`docs/05_dti/hybrid_architecture.md`](../hybrid_architecture.md) §2.2, §2.3
- [`team/alexAlvarez/docs/context/04_state_machine.md`](../../../team/alexAlvarez/docs/context/04_state_machine.md) §2
- [`docs/05_dti/adrs/ADR_002_monolito_modular.md`](ADR_002_monolito_modular.md) (antecedente supersedido parcialmente)
- [`docs/05_dti/adrs/ADR_011_sqs_fifo_phase_closure.md`](ADR_011_sqs_fifo_phase_closure.md) (race conditions)
