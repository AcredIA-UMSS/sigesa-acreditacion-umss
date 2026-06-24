# ADR_011: SQS FIFO para control de concurrencia en cierre de Phase

| Campo | Valor |
|-------|-------|
| **Canonico** | [ADR-0011](../../adr/ADR-0011-sqs-fifo-phase-closure.md) |
| **Estado** | **Aceptada** |
| **Fecha** | 2026-05-25 |
| **Alcance** | Orchestration Service · `indicator_state_history` · `PhaseCloseRule` |
| **Trazabilidad** | `hybrid_architecture.md` §2.4 · `04_state_machine.md` §3 Hard Constraint · PC-SIG-14 REGLA 3 |
| **Supersede** | Ninguno (decisión v1.0 cloud para cierre concurrente de Phase) |

## Contexto

La Hard Constraint documentada en `04_state_machine.md` §3 establece que una Phase transiciona a COMPLETADA si y solo si `COUNT(Indicators WHERE state = APROBADO) == COUNT(Indicators TOTAL)`. Esta verificacion parece trivial en el monolito modular (ADR_002), donde una transaccion PostgreSQL con nivel SERIALIZABLE garantiza la lectura correcta del conteo. Sin embargo, en la arquitectura distribuida del contrato PC-SIG-14, el Orchestration Service es un proceso separado que reacciona a eventos `IndicatorApproved` publicados por Audit Service.

El problema de concurrencia se manifiesta en el siguiente escenario real: una Phase tiene 12 Indicators. Los ultimos dos estan en estado SUBSANADO. El [TD] aprueba ambos con un intervalo de 200ms. EventBridge enruta dos eventos `IndicatorApproved` al Orchestration Service de forma casi simultanea. Si el Orchestration Service tiene dos instancias activas (escalado horizontal) o si dos invocaciones Lambda se ejecutan en paralelo, ambas pueden leer el conteo antes de que la otra instancia haya procesado su aprobacion, y ambas obtendrian `COUNT(APROBADO) = 11`, fallando la condicion `11 == 12` en ambas. En ese caso, la Phase no se cerraria a pesar de que todos los Indicators estan APROBADO. El error es silencioso: no hay excepcion, solo un estado incorrecto que requiere intervencion manual.

Este escenario se agrava porque la arquitectura Append-Only impide cualquier mecanismo de lock optimista sobre una fila de estado existente: no hay fila de "estado actual de Phase" que se pueda bloquear con `SELECT FOR UPDATE` en un INSERT-only.

## Alternativas consideradas

| Alternativa | Pros | Contras | Veredicto |
|-------------|------|---------|-----------|
| **A. SQS FIFO con MessageGroupId = phaseId** | Garantia de orden total por Phase; procesamiento secuencial sin contention; managed service AWS | Latencia adicional de SQS (~100ms por mensaje); no hay paralelismo dentro del mismo phaseId | **Elegida** |
| **B. Optimistic Locking en tabla `phase_state`** | Simple de implementar con SQL `UPDATE ... WHERE version = N` | Requiere una fila mutable de "estado actual de Phase", violando el patron Append-Only para estados | Rechazada |
| **C. Distributed Lock con Redis (Redlock)** | Flexibilidad; bajo overhead si Redis ya existe en stack | Dependencia adicional; complejidad operativa; riesgo de deadlock si el proceso muere con lock adquirido | Rechazada |
| **D. Agregacion eventual con debounce timer** | Sin cambios en infraestructura de mensajeria | Introduce latencia artificial de N segundos antes de evaluar cierre; puede perder eventos durante el debounce | Rechazada |

## Decision

Se adopta **SQS FIFO con `MessageGroupId = phaseId`** como mecanismo de control de concurrencia en el Orchestration Service, con las siguientes especificaciones:

**Regla 1 — Encolamiento FIFO obligatorio para eventos de cierre.** EventBridge no enruta `IndicatorApproved` directamente al Orchestration Service. En su lugar, una regla de EventBridge dirige estos eventos a una cola SQS FIFO. El Orchestration Service consume exclusivamente de esa cola.

```
EventBridge → [Rule: source=audit-service, type=IndicatorApproved]
            → SQS FIFO Queue (sigesa-phase-closure.fifo)
                 MessageGroupId = phaseId
            → Orchestration Service (consumer)
```

**Regla 2 — Garantia de orden total por Phase.** Todos los mensajes con el mismo `MessageGroupId` (es decir, todos los `IndicatorApproved` de la misma Phase) se procesan en orden estricto de llegada y de forma secuencial: SQS FIFO garantiza que el siguiente mensaje del grupo no se entrega hasta que el consumidor confirma (`DeleteMessage`) el anterior. Esto elimina el escenario de dos instancias evaluando el conteo en paralelo para la misma Phase.

**Regla 3 — Transaccion REPEATABLE READ en la query de conteo.** Dentro del handler, el query de conteo se ejecuta en una transaccion PostgreSQL con nivel de aislamiento `REPEATABLE READ`. Esto garantiza que la lectura de `COUNT(APROBADO)` y `COUNT(TOTAL)` es consistente dentro de la misma transaccion, sin interferencia de inserciones concurrentes de otras Phases.

**Regla 4 — Idempotencia del handler.** Antes de evaluar la Hard Constraint, el handler verifica si el evento (por `correlationId`) ya fue procesado en la tabla `processed_events`. Si ya existe, descarta el mensaje sin error. Esto protege contra reentregas de SQS (at-least-once delivery).

**Regla 5 — Dead Letter Queue obligatoria.** La cola FIFO tiene una Dead Letter Queue (DLQ) configurada con `maxReceiveCount = 3`. Si el handler falla tres veces para el mismo mensaje, el mensaje pasa a DLQ y se genera una alarma en CloudWatch para intervencion manual. Esto evita que un Indicator con datos inconsistentes bloquee indefinidamente el procesamiento de la Phase.

## Consecuencias

### Positivas

- La Hard Constraint de `04_state_machine.md` §3 se cumple de forma determinista sin posibilidad de race condition entre aprobaciones concurrentes de la misma Phase.
- El modelo de datos permanece puramente Append-Only: no se necesita ninguna fila mutable de "estado de Phase" para el mecanismo de locking.
- SQS FIFO es un servicio managed que no requiere gestion de infraestructura adicional por parte del equipo.

### Negativas

- El paralelismo dentro de una misma Phase es imposible: si hay 50 Indicators APROBADO casi simultaneamente, sus eventos se procesan de forma secuencial en SQS FIFO. La latencia total para cerrar la Phase es proporcional al numero de eventos acumulados en el grupo. Para el volumen de SIGESA (decenas de Indicators por Phase, no miles), esta latencia es aceptable.
- El debugging de mensajes en DLQ requiere acceso a la consola AWS o herramientas de observabilidad; el equipo debe definir un runbook de recuperacion.

### Impacto en actores

| Actor | Efecto |
|-------|--------|
| **[CC]** | Notificacion de Phase completada puede llegar con un retraso de segundos respecto a la ultima aprobacion; no hay impacto funcional |
| **[TD]** | Dashboard de Phase se actualiza de forma eventual (segundos), no inmediata |
| **[JD]** | Cierre de Phase es siempre correcto: nunca un cierre prematuro ni un cierre omitido |

## Validacion

- Test de concurrencia: publicar 12 eventos `IndicatorApproved` para la misma Phase con intervalo de 50ms; verificar que `PhaseCompleted` se emite exactamente una vez y que `COUNT(APROBADO) == 12` al momento de la emision.
- Test de idempotencia: reenviar el mismo `correlationId` a la cola; verificar que `PhaseCompleted` no se emite por segunda vez.
- Test de DLQ: inyectar un evento malformado; verificar que tras 3 reintentos el mensaje llega a DLQ y se genera alarma CloudWatch.

## Referencias

- [`docs/05_dti/hybrid_architecture.md`](../hybrid_architecture.md) §2.4, §5 REGLA 3
- [`team/alexAlvarez/docs/context/04_state_machine.md`](../../../team/alexAlvarez/docs/context/04_state_machine.md) §3 Hard Constraint
- [`docs/05_dti/adrs/ADR_010_event_driven_choreography.md`](ADR_010_event_driven_choreography.md)
- [`docs/05_dti/adrs/ADR_012_ddl_indicator_state_history.md`](ADR_012_ddl_indicator_state_history.md)
