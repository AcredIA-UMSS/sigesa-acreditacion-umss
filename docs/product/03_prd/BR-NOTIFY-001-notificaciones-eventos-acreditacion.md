# Business Requirement — BR-NOTIFY-001: Notificación de Eventos de Acreditación a [CC] y [JD]

## 1. Control de documento

| Campo | Valor |
|-------|-------|
| **ID del requerimiento** | BR-NOTIFY-001 |
| **Versión** | 1.0 (Borrador para validación) |
| **Estado** | Propuesto |
| **Producto** | SIGESA / AcredIA — Sistema de automatización del ciclo de acreditación (UMSS) |
| **Módulo funcional** | MOD-NOTIFY |
| **Autor** | Senior Business Analyst |
| **Fecha** | 12/11/2026 |
| **Idioma del documento** | Español (términos de dominio en códigos de actor del glosario FSD) |
| **Capa documental** | Capa viva (`docs/product/`) — no modifica `docs/baseline/` |
| **Trazabilidad funcional** | FSD-UC-015, PRD-US-017, PRD-US-018, PRD-US-019, BRD-REQ-011, FSD-BR-13 |

---

## 2. Propósito y contexto de negocio

### 2.1 Propósito

Definir los requerimientos de negocio para que el sistema SIGESA notifique de forma oportuna y confiable a los actores involucrados en el ciclo de acreditación (CEUB / ARCU-SUR) sobre eventos críticos del estado de documentos (Evidencia, Indicador, Fase y Proceso), garantizando que los Coordinadores de Carrera ([CC]) y la Jefatura DUEA ([JD]) reciban la información por el canal adecuado: correo institucional y/o notificación interna del sistema al iniciar sesión.

### 2.2 Problema de negocio (pain point)

Según el PRD y el FSD revisados, los procesos de acreditación dependen actualmente de canales dispersos (correo, mensajería informal, Excel) que generan:

- **Incertidumbre del [CC]:** Cuando un [TD] observa o rechaza una Evidencia/Indicador, el [CC] no recibe aviso por ningún canal y se entera días después, al borde del vencimiento del cronograma.
- **Falta de visibilidad del [JD]:** Cuando un proceso de acreditación se cierra exitosamente, el [JD] no recibe notificación automática; debe entrar al sistema y consultar el panel semáforo para descubrirlo.
- **Riesgo de incumplimiento de plazos:** BRD-REQ-011 establece que las alertas automáticas mitigan el principal riesgo de retraso, con eventos críticos notificados en ≤ 15 minutos.

### 2.3 Alcance del requerimiento

**Dentro del alcance:**

1. Notificar a [CC] por correo institucional y notificación interna cuando ocurran eventos sobre documentos/Evidencias/Indicadores asociados a su carrera: rechazo, observaciones, demora (plazo próximo/vencido), aprobación.
2. Notificar a [JD] cuando un proceso de acreditación se cierre exitosamente, por **doble canal**: correo institucional a su buzón de correo + notificación interna del sistema visible al iniciar sesión.
3. Notificar a [TD] cuando se carga una nueva Evidencia o una nueva versión (subsanación) en su bandeja de revisión (BR-NOTIFY-001.3 / PRD-US-019).

**Fuera de alcance (se declara explícitamente):**

- Notificaciones push de navegador, WhatsApp, Telegram u otros canales de mensajería instantánea.
- Notificaciones al [P] (público sin login).
- Notificaciones al [EE] como destinatario de alertas de este requerimiento (el [EE] sí participa en la regla de cierre de proceso: aprueba indicadores; no recibe las notificaciones de este BR).
- Modificación de plantillas de correo CNACU oficiales (sí se usan plantillas HTML institucionales SIGESA/UMSS propias del sistema).

---

## 3. Actores y glosario de dominio

| Símbolo | Nombre | Código (EN) | Responsabilidad en este requerimiento |
|---------|--------|-------------|----------------------------------------|
| [CC] | Coordinador de Carrera | `ProgramCoordinator` | Recibe notificaciones de eventos de sus Evidencias/Indicadores (rechazo, observación, demora, aprobación). |
| [TD] | Técnico DUEA | `DueaTechnician` | Genera eventos de rechazo/aprobación/observación que disparan notificación a [CC]. Recibe notificación cuando se carga nueva Evidencia o nueva versión (subsanación) de su bandeja. |
| [JD] | Jefatura DUEA | `DueaAdministrator` | Recibe notificación de cierre exitoso de proceso de acreditación por correo + interna al iniciar sesión. |
| [P] | Público | `Public` | No aplica a este requerimiento. |

**Términos de dominio (glosario FSD):**

- **Evidencia**: prueba documental normativa versionada (prohibido usar "archivo" o "file" genérico).
- **Indicador**: elemento verificable con estados (`PENDIENTE`, `SUBIDO`, `OBSERVADO`, `SUBSANADO`, `APROBADO`) y Evidencia asociada.
- **Proceso**: ciclo CEUB o ARCU-SUR de una carrera en una gestión.
- **Fase**: etapa mayor del proceso.
- **Cierre exitoso de proceso**: transición del Proceso a estado cerrado con todos los Indicadores/Entregables resueltos según reglas de negocio.
- **Canal institucional**: correo con dominio `@umss.edu.bo` (FSD-BR-12).

---

## 4. Requerimientos de negocio (funcionales)

Cada requerimiento es verificable y trazable. Los criterios de aceptación se expresan en Gherkin (sección 6).

### BR-NOTIFY-001.1 — Notificación a [CC] de eventos de documento/Evidencia/Indicador

**Enunciado:** El sistema SHALL notificar al [CC], por correo institucional y notificación interna del sistema, cuando ocurra cualquiera de los siguientes eventos sobre una Evidencia/Indicador de su carrera:

| Evento | Descripción | Canal [CC] | Origen del evento |
|--------|-------------|------------|-------------------|
| Rechazo | Indicador/Evidencia rechazado por [TD] con justificación | Correo + interno | FSD-UC-008 (rechazo) |
| Observaciones | Indicador pasa a `OBSERVADO` con observación activa de [TD] | Correo + interno | FSD-UC-008 (observación) |
| Demora | Plazo de Fase/Indicador próximo o vencido | Correo + interno | Job de alertas (PRD-US-018) |
| Aprobación | Indicador aprobado por [TD] | Correo + interno | FSD-UC-009 (aprobación) |

**Reglas de negocio asociadas:**
- Solo [CC] asignado a la carrera del Indicador recibe la notificación (FSD-BR-09: [CC] solo ve su carrera).
- El correo institucional debe tener dominio `@umss.edu.bo` (FSD-BR-12).
- Eventos críticos notificados en ≤ 15 minutos (BRD-REQ-011, FSD-BR-13).
- Cada notificación incluye un enlace profundo (deep link) al Indicador y observación asociada, sin reautenticación adicional si la sesión está activa (PRD-US-017).

### BR-NOTIFY-001.2 — Notificación a [JD] de cierre exitoso de proceso de acreditación (doble canal)

**Enunciado:** El sistema SHALL notificar al [JD] cuando un Proceso de acreditación se cierre exitosamente, mediante **doble canal**:

1. **Correo institucional** al buzón de correo del [JD] (`@umss.edu.bo`), enviado de forma asíncrona fuera de la transacción de negocio (patrón outbox).
2. **Notificación interna del sistema**, visible al [JD] cuando inicie sesión (in-app / bandeja de notificaciones al login).

**Reglas de negocio asociadas:**
- El disparador es el cierre exitoso del Proceso (todos los Indicadores/Entregables resueltos, sin pendientes que bloqueen el cierre).
- El correo y la notificación interna deben incluir el identificador del Proceso, la carrera/facultad, la fecha de cierre y un enlace profundo al detalle del proceso cerrado.
- El [JD] debe poder consultar el historial de notificaciones de cierre de proceso recibido.
- SLA: notificación crítica ≤ 15 minutos desde el cierre efectivo (BRD-REQ-011, FSD-BR-13).
- El envío de correo no debe bloquear ni retener conexiones del pool Hikari durante la transacción principal de negocio (desacoplamiento I/O, FEAT-05).

### BR-NOTIFY-001.3 — Notificación a [TD] de nueva Evidencia cargada o nueva versión

**Enunciado:** El sistema SHALL notificar al [TD] asignado, por correo institucional y notificación interna del sistema, cuando ocurra cualquiera de los siguientes eventos relacionados con Evidencias de su bandeja de revisión:

| Evento | Descripción | Canal [TD] | Origen del evento |
|--------|-------------|------------|-------------------|
| Nueva Evidencia cargada | [CC] carga una Evidencia v1 en un Indicador de su carrera | Correo + interno | FSD-UC-004 (carga de Evidencia) |
| Nueva versión (subsanación) | [CC] carga una nueva versión de Evidencia enlazada a una observación | Correo + interno | FSD-UC-006 (subsanación) |

**Reglas de negocio asociadas:**
- Solo el [TD] asignado a la carrera del Indicador recibe la notificación (FSD-BR-09).
- El correo institucional debe tener dominio `@umss.edu.bo` (FSD-BR-12).
- Eventos críticos notificados en ≤ 15 minutos (BRD-REQ-011, FSD-BR-13).
- Cada notificación incluye un enlace profundo a la bandeja de revisión filtrada por carrera/estado (PRD-US-019).

---

## 5. Requerimientos no funcionales

| ID | Categoría | Requerimiento | Métrica / Umbral | Origen |
|----|-----------|---------------|------------------|--------|
| BR-NOTIFY-NFR-001 | Rendimiento | Notificación de evento crítico entregada | ≤ 15 min | BRD-REQ-011, FSD-BR-13 |
| BR-NOTIFY-NFR-002 | Seguridad | Correo solo a direcciones `@umss.edu.bo` | 100 % | FSD-BR-12 |
| BR-NOTIFY-NFR-003 | Seguridad | Credenciales SMTP por variables de entorno, nunca hardcodeadas | 0 secretos en código | 12-Factor F12-01 / F12-05 |
| BR-NOTIFY-NFR-004 | Resiliencia | Envío SMTP desacoplado de la transacción JPA (outbox) | Hikari liberado tras COMMIT | FEAT-05, CON-02/CON-03 |
| BR-NOTIFY-NFR-005 | Resiliencia | Reintentos de correo con backoff + jitter ante falla SMTP | Hasta 5 intentos, luego FAILED | FEAT-05, CON-04 |
| BR-NOTIFY-NFR-006 | Concurrencia | Worker multi-instancia seguro con `SKIP LOCKED` | N ≥ 1 instancias sin duplicidad | FEAT-05, F12-03/CON-01 |
| BR-NOTIFY-NFR-007 | Operabilidad | Registro de fallas y reintentos a stdout (sin secretos) | Logs a stdout | FEAT-05, F12-04 |
| BR-NOTIFY-NFR-008 | Calidad | Cobertura de pruebas unitarias e integración | > 90 % JaCoCo | AGENTS.md |

---

## 6. Criterios de aceptación (Gherkin)

### BR-NOTIFY-001.1 — Notificaciones a [CC]

```gherkin
# language: es
@BR-NOTIFY-001.1 @FSD-UC-015 @PRD-US-017 @PRD-US-018
Característica: Notificaciones a [CC] sobre eventos de Evidencia/Indicador

  Regla: El [CC] recibe correo institucional y notificación interna ante rechazo, observación, demora y aprobación.

  Escenario: [CC] recibe notificación de rechazo de Indicador
    Dado un [TD] autenticado revisando un Indicador de la carrera X del [CC]
    Cuando el [TD] rechaza el Indicador con justificación válida
    Entonces el Indicador pasa a estado OBSERVADO
    Y el [CC] recibe un correo institucional con el asunto de rechazo
    Y el [CC] recibe una notificación interna del sistema
    Y ambas notificaciones incluyen un enlace profundo al Indicador y observación
    Y la entrega ocurre en un máximo de 15 minutos

  Escenario: [CC] recibe notificación de observaciones pendientes
    Dado un Indicador en estado OBSERVADO con observación activa de [TD]
    Cuando el [CC] consulta su dashboard de observaciones abiertas
    Entonces el [CC] ve la observación con plazo más próximo primero
    Y el [CC] ha recibido notificación previa por correo e interna

  Escenario: [CC] recibe alerta de plazo próximo
    Dado una Fase con fecha límite en 3 días y configuración de alerta activa
    Cuando el job de alertas se ejecuta
    Entonces el [CC] recibe correo institucional con enlace directo al Indicador
    Y el [CC] recibe notificación interna del sistema

  Escenario: [CC] recibe notificación de aprobación de Indicador
    Dado un [TD] con Evidencia conforme aprueba un Indicador
    Cuando la aprobación se confirma
    Entonces el Indicador pasa a estado APROBADO
    Y el [CC] recibe correo institucional y notificación interna en ≤ 15 minutos

  Escenario: [CC] no recibe notificaciones de carreras ajenas
    Dado un [CC] asignado a la carrera A
    Cuando un [TD] rechaza un Indicador de la carrera B
    Entonces el [CC] no recibe ninguna notificación del evento de la carrera B
```

### BR-NOTIFY-001.2 — Notificación a [JD] de cierre exitoso de proceso

```gherkin
# language: es
@BR-NOTIFY-001.2 @FSD-UC-010 @PRD-US-011
Característica: Notificación a [JD] de cierre exitoso de proceso de acreditación

  Regla: Al cerrarse exitosamente un proceso, el [JD] recibe notificación por correo institucional y notificación interna al iniciar sesión.

  Escenario: [JD] recibe correo al cerrarse un proceso exitosamente
    Dado un [JD] autenticado con correo institucional @umss.edu.bo
    Cuando un Proceso de acreditación de una carrera se cierra exitosamente (todos los indicadores resueltos)
    Entonces el [JD] recibe un correo institucional en su buzón de correo
    Y el correo incluye el identificador del proceso, carrera, facultad y fecha de cierre
    Y el correo incluye un enlace profundo al detalle del proceso cerrado
    Y la entrega ocurre en un máximo de 15 minutos

  Escenario: [JD] ve notificación interna al iniciar sesión
    Dado un proceso de acreditación cerrado exitosamente y una notificación de cierre generada para el [JD]
    Cuando el [JD] inicia sesión en el sistema
    Entonces el sistema muestra la notificación interna de cierre de proceso en la bandeja/notificaciones del [JD]
    Y la notificación está marcada como no leída hasta que el [JD] la consulte

  Escenario: [JD] puede consultar historial de notificaciones de cierre
    Dado múltiples procesos cerrados con notificaciones generadas para el [JD]
    Cuando el [JD] consulta el historial de notificaciones de cierre de proceso
    Entonces el sistema lista todas las notificaciones de cierre con proceso, carrera, fecha y estado

  Escenario: No se envía correo si el cierre del proceso está bloqueado
    Dado una Fase con al menos un Indicador no APROBADO
    Cuando el [TD] intenta cerrar el Proceso
    Entonces el sistema rechaza el cierre (FASE_CIERRE_BLOQUEADO)
    Y no se genera notificación de cierre para el [JD]
    Y no se envía correo institucional
```


### BR-NOTIFY-001.3 — Notificaciones a [TD] de nueva Evidencia / nueva versión

```gherkin
# language: es
@BR-NOTIFY-001.3 @FSD-UC-004 @FSD-UC-006 @PRD-US-019
Característica: Notificaciones a [TD] de nueva Evidencia o nueva versión

  Regla: El [TD] recibe correo institucional y notificación interna cuando se carga nueva Evidencia o nueva versión en su bandeja de revisión.

  Escenario: [TD] recibe notificación de nueva Evidencia cargada por [CC]
    Dado un [CC] autenticado carga una Evidencia v1 en un Indicador de la carrera X asignada al [TD]
    Cuando el registro queda en estado Pendiente de revisión
    Entonces el [TD] asignado recibe un correo institucional con enlace a su bandeja filtrada
    Y el [TD] asignado recibe una notificación interna del sistema
    Y la entrega ocurre en un máximo de 15 minutos

  Escenario: [TD] recibe notificación de nueva versión (subsanación)
    Dado un Indicador en estado OBSERVADO con observación activa O-123
    Cuando el [CC] carga una nueva versión de Evidencia enlazada a O-123
    Entonces el [TD] asignado recibe un correo institucional con la nueva versión disponible
    Y el [TD] asignado recibe una notificación interna del sistema

  Escenario: [TD] no recibe notificaciones de carreras ajenas
    Dado un [TD] asignado a la carrera A
    Cuando un [CC] carga una Evidencia en un Indicador de la carrera B
    Entonces el [TD] no recibe ninguna notificación del evento de la carrera B
```


---

## 7. Matriz de trazabilidad

| Requerimiento BR | Origen BRD/MRD | User Story PRD | Caso de uso FSD | Regla de negocio FSD | Feature |
|------------------|----------------|----------------|-----------------|----------------------|---------|
| BR-NOTIFY-001.1 | BRD-REQ-011 | PRD-US-017, PRD-US-018 | FSD-UC-015 | FSD-BR-13 | FEAT-05 |
| BR-NOTIFY-001.2 | BRD-REQ-011, BRD-REQ-014 | PRD-US-011 | FSD-UC-010 | FSD-BR-07 | FEAT-05 (extensión) |
| BR-NOTIFY-001.3 | BRD-REQ-011 | PRD-US-019 | FSD-UC-004, FSD-UC-006 | FSD-BR-04 | FEAT-05 |
| BR-NOTIFY-NFR-001 | BRD-REQ-011 | PRD-US-017 | FSD-UC-015 | FSD-BR-13 | FEAT-05 |
| BR-NOTIFY-NFR-002 | BRD-RB-13 | PRD-US-001 | FSD-UC-001 | FSD-BR-12 | MOD-AUTH |

**Nota de extensión:** Este documento extiende el alcance actual de FSD-UC-015 / FEAT-05. Agrega [JD] como destinatario del evento "cierre exitoso de proceso" con doble canal (correo + interno) y confirma [TD] como destinatario de eventos de nueva Evidencia / nueva versión (PRD-US-019), ambos ya previstos en el diseño de outbox pero explicitados como requerimiento de negocio aquí.

---

## 8. Supuestos y restricciones

| ID | Supuesto / Restricción | Detalle |
|----|------------------------|---------|
| S-01 | Correo UMSS activo | Todos los usuarios piloto ([CC], [TD], [JD]) tienen correo institucional `@umss.edu.bo` activo (BRD-ASM-02). |
| S-02 | Acta DUEA | SIGESA es canal oficial de Evidencia y notificación en el piloto (BRD-ASM-04). |
| S-03 | Proceso cerrado definido | **DECIDIDO (Q-01):** el cierre exitoso de una acreditación ocurre cuando **todos los Indicadores** del Proceso fueron **aprobados por los evaluadores externos ([EE])**. El cierre de un Indicador significa que la Evidencia/documento adyacente cumple los estándares esperados de la acreditación (depende de carrera, proceso, plantilla normativa). |
| S-04 | Servidor SMTP interno | Se dispone de un contenedor SMTP interno (`sigesa-smtp`) para desarrollo y pruebas del piloto, configurado vía variables de entorno (`SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, etc.). Véase `docker-compose.yml` y `smtp/Dockerfile`. |
| R-01 | No ERP | SIGESA no es ERP ni reemplaza SIIS/RRHH (BRD-CST-07, FSD-BR-16). |
| R-02 | Dominio de correo | Solo correos `@umss.edu.bo` (FSD-BR-12, BRD-RB-13). |
| R-03 | Baseline inmutable | Este documento no modifica `docs/baseline/`; se aloja en la capa viva `docs/product/`. |

---

## 9. Fuera de alcance

- Notificaciones push de navegador o mensajería WhatsApp/Telegram.
- Notificaciones al [P] (público sin login) ni al [EE] (evaluador externo).
- Modificación de plantillas de correo CNACU.
- Canales de notificación distintos a correo institucional + notificación interna del sistema.

---

## 10. Decisiones resueltas (2026-11-12)

| ID | Decisión | Detalle |
|----|----------|---------|
| Q-01 | Cierre de acreditación | Un Proceso se cierra exitosamente cuando **todos los Indicadores** fueron **aprobados por [EE]**. El cierre de un Indicador = la Evidencia adyacente cumple los estándares de la acreditación (contexto carrera/proceso/plantilla). No se confunde con freeze ni con `Level1Completed`. |
| Q-02 | Bandeja in-app | **Popup/popover simple** (no pantalla completa). Mensajes **cortos**. Al pulsar, deep link al referente (p. ej. rechazo → Indicador de esa Evidencia). El **correo** puede ser más largo y usa **plantilla HTML** con colores/fuentes del design system institucional. |
| Q-03 | Deep link [JD] | Con **sesión activa**, el [JD] aterriza en el **panel semáforo** (puede supervisar **más de una** acreditación). Al pulsar el semáforo de una carrera/proceso, redirige al **proceso específico**. |

---

## 11. Aprobación

| Rol | Nombre | Firma / Fecha | Veredicto |
|-----|--------|---------------|-----------|
| Product Owner / PM | — | — | — |
| Jefatura DUEA [JD] | — | — | — |
| Tech Lead | — | — | — |
| QA | — | — | — |

---

*Documento canónico SIGESA — UMSS, Cochabamba, Bolivia, 2026. Capa viva (`docs/product/`). No modificar `docs/baseline/`.*