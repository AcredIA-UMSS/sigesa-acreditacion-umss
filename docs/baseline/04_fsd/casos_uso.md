# Casos de uso — SIGESA / AcredIA

## Control de versión

| Campo | Valor |
|-------|-------|
| **Versión** | Dorada v1.0 |
| **Timestamp** | `2026-05-16T18:30:00-04:00` |
| **Fuente maestra** | [`FSD.md`](FSD.md) |
| **Trazabilidad** | [`matriz_trazabilidad.md`](../../matriz_trazabilidad.md) · [`docs/03_prd/PRD.md`](../03_prd/PRD.md) §5 |
| **Gherkin** | [`gherkin.md`](gherkin.md) |
| **Diagramas** | [`07_diagramas/`](07_diagramas/README.md) |

> Especificación detallada de los **18 casos de uso** (`FSD-UC-001`…`018`). Comportamiento verificable; sin prescripción de stack.

---

## Índice

| ID | Nombre | Actor principal | PRD-US | Diagrama | Release |
|----|--------|---------------|--------|----------|---------|
| [FSD-UC-001](#fsd-uc-001--autenticación-y-sesión) | Autenticación y sesión | Usuario interno | 001, 003 | [secuencia](07_diagramas/seq-001-001-autenticacion-secuencia.mmd) | v1.0 |
| [FSD-UC-002](#fsd-uc-002--gestión-de-usuarios-jd) | Gestión de usuarios [JD] | [JD] | 002 | — | v1.0 |
| [FSD-UC-003](#fsd-uc-003--plantillas-y-proceso-ceubarcu-sur) | Plantillas y Proceso CEUB/ARCU-SUR | [JD] | 023 | [secuencia](07_diagramas/seq-003-003-010-proceso-y-cierre-fase-secuencia.mmd) | v1.0 |
| [FSD-UC-004](#fsd-uc-004--cargar-evidencia) | Cargar Evidencia | [CC] | 005, 025 | [secuencia](07_diagramas/seq-004-004-008-carga-y-observacion-secuencia.mmd) | v1.0 |
| [FSD-UC-005](#fsd-uc-005--versionado-y-bloqueo-de-borrado) | Versionado y bloqueo de borrado | [CC], [TD] | 007, 008 | [estados](07_diagramas/state-004-004-005-estados-evidencia.mmd) | v1.0 |
| [FSD-UC-006](#fsd-uc-006--subsanar-evidencia) | Subsanar Evidencia | [CC] | 006 | [secuencia](07_diagramas/seq-006-006-subsanar-evidencia-secuencia.mmd) | v1.0 |
| [FSD-UC-007](#fsd-uc-007--buscar-evidencia) | Buscar Evidencia | [CC], [TD] | 004 | — | v1.0 |
| [FSD-UC-008](#fsd-uc-008--rechazar-indicador) | Rechazar Indicador | [TD] | 009 | [secuencia](07_diagramas/seq-004-004-008-carga-y-observacion-secuencia.mmd) | v1.0 |
| [FSD-UC-009](#fsd-uc-009--aprobar-indicador) | Aprobar Indicador | [TD] | 010 | [estados](07_diagramas/state-006-006-008-009-estados-indicador.mmd) | v1.0 |
| [FSD-UC-010](#fsd-uc-010--avanzarcerrar-fase) | Avanzar/cerrar Fase | [TD] | 011 | [estados](07_diagramas/state-010-010-cierre-fase-estados.mmd) | v1.0 |
| [FSD-UC-011](#fsd-uc-011--dashboard-cc-y-observaciones) | Dashboard [CC] y observaciones | [CC] | 012, 015 | — | v1.0 |
| [FSD-UC-012](#fsd-uc-012--bandeja-auditoría-td) | Bandeja auditoría [TD] | [TD] | 014 | — | v1.0 |
| [FSD-UC-013](#fsd-uc-013--panel-semáforo-jd) | Panel semáforo [JD] | [JD] | 013 | — | v1.0 |
| [FSD-UC-014](#fsd-uc-014--reporte-ejecutivo-pdf) | Reporte ejecutivo PDF | [JD] | 021 | — | v1.0 |
| [FSD-UC-015](#fsd-uc-015--notificaciones-y-alertas) | Notificaciones y alertas | Sistema | 017–019 | — | v1.0 |
| [FSD-UC-016](#fsd-uc-016--portal-público) | Portal público | [P] | 016, 020 | — | v1.1 |
| [FSD-UC-017](#fsd-uc-017--bitácora-de-auditoría) | Bitácora de auditoría | [JD] | 022 | — | v1.0 |
| [FSD-UC-018](#fsd-uc-018--importación-masiva) | Importación masiva | [CC] | 024 | — | v1.1 |

---

## FSD-UC-001 — Autenticación y sesión

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-001 · PRD-US-001, 003 · BRD-REQ-001 · MRD-N-09 |
| **Módulo** | MOD-AUTH |
| **Reglas** | FSD-BR-12 |

**Actor principal:** Usuario interno ([CC], [TD], [JD])

**Precondiciones:** Cuenta registrada con correo `@umss.edu.bo`; rol asignado.

**Disparador:** Submit en pantalla `/login`.

**Flujo principal:**
1. Usuario ingresa credenciales.
2. Sistema valida vía `LocalAuthAdapter` (v1.0); v1.1 `LdapAuthAdapter` (ADR-0003).
3. Sistema crea sesión JWT con rol y alcance (carrera/facultad).
4. Redirige al dashboard según rol.
5. Registra `AUDIT_LOGIN` (UC-017).

**Flujos alternos:**
- **A1 — Credenciales inválidas:** 401 genérico; no revelar si el usuario existe.
- **A2 — Sin rol:** 403; acceso denegado.
- **E3 — Sin sesión en acción sensible:** 401; sin cambios de estado (US-003).

**Postcondiciones:** Sesión activa con permisos acotados al rol.

**Datos:** entrada `email`, `password`; salida `sessionToken`, `role`, `programScope`.

---

## FSD-UC-002 — Gestión de usuarios [JD]

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-001 · PRD-US-002 |
| **Módulo** | MOD-AUTH |

**Actor principal:** [JD]

**Precondiciones:** [JD] autenticado.

**Flujo principal:**
1. [JD] accede a `/admin/users`.
2. Registra usuario con correo UMSS, rol y carrera (si [CC]).
3. Sistema crea cuenta inactiva hasta primer acceso.
4. Asocia `user_program_assignment` con alcance mínimo necesario.

**Flujos alternos:**
- **A1 — Revocación:** desactiva cuenta; usuario no puede login; historial en auditoría conservado.

**Postcondiciones:** Usuario creado o desactivado; evento en bitácora.

---

## FSD-UC-003 — Plantillas y Proceso CEUB/ARCU-SUR

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-002, 004, 016 · PRD-US-023 |
| **Reglas** | FSD-BR-08, FSD-BR-17 |

**Actor principal:** [JD]

**Precondiciones:** Plantilla CEUB o ARCU-SUR validada por comité normativo.

**Flujo principal:**
1. [JD] activa plantilla para periodo vigente (`POST /templates/{id}/activate`).
2. Sistema fija taxonomía Fase → Dimensión → Criterio → Indicador para nuevos Procesos.
3. [JD] o proceso automático crea `AccreditationProcess` para carrera.
4. Sistema valida un solo Proceso activo por tipo/carrera/periodo.

**Flujos alternos:**
- **A1 — Proceso duplicado:** rechaza con `PROCESS_ALREADY_ACTIVE`.
- **A2 — Proceso en curso:** conserva plantilla con la que inició; no migra retroactivamente.

**Postcondiciones:** Proceso activo con instancias de Fase e Indicador.

---

## FSD-UC-004 — Cargar Evidencia

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-005, 022 · PRD-US-005, 025 |
| **Reglas** | FSD-BR-01, FSD-BR-03, FSD-BR-18 |

**Actor principal:** [CC]

**Precondiciones:** Indicador en `PENDIENTE` u `OBSERVADO`; permiso sobre carrera.

**Flujo principal:**
1. [CC] navega Proceso → Fase → Indicador.
2. Adjunta Evidence y metadatos obligatorios (`indicatorId`, `criterionId`, `description`).
3. Sistema valida tipo/tamaño; calcula SHA-256.
4. Evidence Service persiste `Evidence` v1 y publica `EvidenceUploaded`.
5. Audit Service inserta transición `PENDIENTE → SUBIDO` en `indicator_state_history`.
6. Notification Service notifica al [TD] (UC-015).
7. Si Evidence > 5 MB: barra de progreso y carga asíncrona (US-025).

**Excepciones:** sin Indicador → 400; formato inválido → 422.

**Postcondiciones:** `evidenceId`, `version=1`, `contentHash`, evento `EvidenceUploaded`.

---

## FSD-UC-005 — Versionado y bloqueo de borrado

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-006, 007 · PRD-US-007, 008 |
| **Reglas** | FSD-BR-02, FSD-BR-15 |

**Actores:** [CC], [TD] (consulta); cualquier rol (intento DELETE bloqueado).

**Flujo principal (consulta):**
1. Usuario abre historial de versiones de una Evidencia.
2. Sistema lista versiones ordenadas; deriva la vigente por `version DESC` y `supersedesId`.
3. Versiones anteriores en solo lectura.

**Flujo excepción (append-only):**
1. Cualquier rol invoca `DELETE /evidences/{id}` sobre Evidencia aprobada.
2. Sistema responde `409 EVIDENCE_IMMUTABLE`.
3. Registra `AUDIT_DELETE_DENIED` en bitácora.

---

## FSD-UC-006 — Subsanar Evidencia

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-008 · PRD-US-006 |
| **Reglas** | FSD-BR-06 |

**Actor principal:** [CC]

**Precondiciones:** Indicador `OBSERVADO`; existe `observationId` activo.

**Flujo principal:**
1. [CC] abre observación desde dashboard o enlace de correo.
2. Carga nueva versión (`POST /evidences/{id}/versions`) con `observationId`.
3. Sistema persiste v2 con `supersedesVersion`; v1 intacta.
4. Audit Service inserta transición `OBSERVADO → SUBSANADO`; Notification Service notifica [TD].

**Postcondiciones:** Cadena de versiones trazable a observación origen.

---

## FSD-UC-007 — Buscar Evidencia

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-015 · PRD-US-004 · NFR-002 |
| **Módulo** | MOD-EVIDENCE |

**Actores:** [CC] (alcance carrera), [TD] (global)

**Flujo principal:**
1. Usuario aplica filtros: carrera, Fase, Indicador, texto, gestión.
2. Sistema consulta índice de búsqueda.
3. Presenta resultados paginados con enlace directo a Evidencia/Indicador.

**Criterio de éxito:** tarea E2E mediana ≤ 2 min (piloto).

**Flujo alterno:** sin resultados → mensaje con sugerencia de ampliar filtros.

---

## FSD-UC-008 — Rechazar Indicador

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-008, 009 · PRD-US-009 |
| **Reglas** | FSD-BR-04, FSD-BR-05 |

**Actor principal:** [TD]

**Flujo principal:**
1. [TD] revisa Indicador en `SUBIDO` o `SUBSANADO`.
2. Ingresa justificación (mín. 20 caracteres).
3. Sistema crea `Observation` e inserta transición `SUBIDO|SUBSANADO → OBSERVADO` en `indicator_state_history`.
4. Notifica [CC] en ≤ 15 min (FSD-BR-13).

**Excepción:** justificación vacía → `422 JUSTIFICATION_REQUIRED`.

---

## FSD-UC-009 — Aprobar Indicador

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-009 · PRD-US-010 |
| **Reglas** | FSD-BR-04 |

**Actor principal:** [TD]

**Flujo principal:**
1. [TD] valida Evidencia conforme.
2. `POST /indicators/{id}/approve`.
3. Audit Service inserta transición a `APROBADO` en `indicator_state_history`.
4. Publica `IndicatorApproved`; Notification Service notifica [CC] y Orchestration Service evalúa cierre de Phase (UC-010).

**Excepción:** [CC] intenta aprobar → `403 FORBIDDEN_ROLE`.

---

## FSD-UC-010 — Avanzar/cerrar Fase

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-010, 017 · PRD-US-011 |
| **Reglas** | FSD-BR-07 |

**Actor principal:** [TD]

**Precondiciones:** Todos los Indicadores de la Fase en `APROBADO`.

**Flujo principal:**
1. Orchestration Service consume `IndicatorApproved` desde SQS FIFO (`MessageGroupId = phaseId`).
2. Sistema verifica conteo sobre `indicator_current_view`: indicadores aprobados = total.
3. Si corresponde, inserta `phase_state_history` con `COMPLETADA` y publica `PhaseCompleted`.

**Excepción:** pendientes → `409 FASE_CIERRE_BLOQUEADO` + lista de Indicadores.

---

## FSD-UC-011 — Dashboard [CC] y observaciones

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-012 · PRD-US-012, 015 |
| **Reglas** | FSD-BR-09 |

**Actor principal:** [CC]

**Flujo principal:**
1. [CC] abre `/coordinator/dashboard`.
2. Sistema muestra avance por Fase solo de su carrera.
3. Lista observaciones abiertas ordenadas por plazo ascendente.
4. Acceso en ≤ 3 clics a formulario de subsanación (BRD-REQ-026).

---

## FSD-UC-012 — Bandeja auditoría [TD]

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-012 · PRD-US-014 |

**Actor principal:** [TD]

**Flujo principal:**
1. [TD] abre `/technician/inbox`.
2. Filtra por carrera, Fase, estado (`SUBIDO`, `SUBSANADO`, etc.).
3. Accede a revisión, rechazo o aprobación del Indicador.

**Criterio:** filtro representativo en ≤ 2 min (BRD-REQ-026).

---

## FSD-UC-013 — Panel semáforo [JD]

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-011 · PRD-US-013 |

**Actor principal:** [JD]

**Flujo principal:**
1. [JD] abre `/executive/semaphore`.
2. Sistema calcula semáforo por carrera/facultad (Rojo/Amarillo/Verde) según reglas de completitud y vencimientos.
3. Vista consolidada disponible en ≤ 2 min sin soporte ad-hoc.

---

## FSD-UC-014 — Reporte ejecutivo PDF

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-014 · PRD-US-021 |
| **Reglas** | FSD-BR-14 |

**Actor principal:** [JD]

**Flujo principal:**
1. [JD] aplica filtros en panel ejecutivo.
2. `POST /reports/executive/pdf` (≤ 2 clics desde contexto de trabajo).
3. Sistema genera PDF con timestamp, filtros y marca institucional.
4. P95 generación ≤ 5 min (NFR-003).

---

## FSD-UC-015 — Notificaciones y alertas

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-013 · PRD-US-017–019 |
| **Reglas** | FSD-BR-13 |

**Actor:** Sistema (outbox) → correo UMSS

**Eventos:**
- Aprobación/rechazo de Indicador → [CC]
- Plazo próximo (job programado) → [CC]
- Nueva Evidencia cargada → [TD]

**Patrón:** `notification_outbox` + worker SMTP; SLA ≤ 15 min para eventos críticos.

---

## FSD-UC-016 — Portal público

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-019, 026 · PRD-US-016, 020 |
| **Reglas** | FSD-BR-10 |

**Actor principal:** [P] (anónimo)

**Flujo principal:**
1. Visitante consulta `GET /public/programs/{slug}`.
2. Solo registros con `published=true`.
3. Opcional: descarga certificado si [JD] publicó (US-020).

**Invariante:** cero borradores u observaciones internas visibles.

---

## FSD-UC-017 — Bitácora de auditoría

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-018 · PRD-US-022 |

**Actor principal:** [JD]

**Flujo principal:**
1. [JD] consulta `GET /audit/logs` con filtros (actor, acción, rango fechas).
2. Sistema devuelve log append-only (login, transiciones, DELETE denegado, etc.).
3. Exportación CSV para auditoría externa.

---

## FSD-UC-018 — Importación masiva

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-020 · PRD-US-024 |

**Actor principal:** [CC]

**Flujo principal:**
1. [CC] descarga plantilla CSV institucional.
2. `POST /imports/evidences` con archivo validado.
3. Sistema crea borradores vinculados a Indicadores fila a fila.
4. Reporte de filas rechazadas con causa.

**Excepción:** columnas obligatorias faltantes → rechazo total sin registros parciales.

---

## Registro de cambios

| Versión | Fecha | Cambio |
|---------|-------|--------|
| Dorada v1.0 | 2026-05-16 | Extracción desde FSD.md; 18 casos de uso completos |
