# Reglas de negocio — SIGESA / AcredIA

## Control de versión

| Campo | Valor |
|-------|-------|
| **Versión** | Dorada v1.0 |
| **Timestamp** | `2026-05-16T18:30:00-04:00` |
| **Fuente maestra** | [`FSD.md`](FSD.md) §6 · [`docs/01_brd/BRD.md`](../01_brd/BRD.md) |
| **Casos de uso** | [`casos_uso.md`](casos_uso.md) |

> Catálogo normativo **`FSD-BR-01`…`18`**. Toda implementación y prueba de dominio debe respetarlas; conflicto con documento inferior → prevalece esta tabla y el BRD.

---

## Índice por tipo

| Tipo | IDs |
|------|-----|
| Validación | BR-01, BR-05, BR-07 |
| Política | BR-02, BR-03, BR-10, BR-14, BR-16, BR-18 |
| Autorización | BR-04, BR-09, BR-12, BR-19 |
| Trazabilidad | BR-06, BR-15 |
| Estado | BR-07 |
| Negocio | BR-08, BR-17, BR-20, BR-21 |
| SLA | BR-13 |
| Ética | BR-11 |
| Alcance | BR-16 |
| Estructura | BR-22, BR-23 |

---

## Catálogo detallado

### FSD-BR-01 — Evidencia siempre ligada a Indicador/Criterio

| Campo | Valor |
|-------|-------|
| **Tipo** | Validación |
| **Origen BRD** | BRD-RB-06 |
| **UC** | UC-004 |
| **Enunciado** | Ninguna Evidencia puede persistirse sin `indicatorId` válido y clasificación en la taxonomía (Criterio/Indicador del marco activo). |
| **Violación** | `400 EVIDENCE_UNCLASSIFIED` |
| **Verificación** | Test API carga sin `indicatorId`; UAT formulario incompleto (PRD-US-005). |

---

### FSD-BR-02 — Append-only: no borrado físico de aprobados

| Campo | Valor |
|-------|-------|
| **Tipo** | Política |
| **Origen BRD** | BRD-CST-01, BRD-REQ-007 |
| **UC** | UC-005 |
| **Enunciado** | Evidencia en estado aprobado o con versión aprobada no admite `DELETE` físico. Subsanación exclusivamente por nueva versión. |
| **Violación** | `409 EVIDENCE_IMMUTABLE` + `AUDIT_DELETE_DENIED` |
| **Verificación** | TC-SAD-001; NFR-017; ADR-0001. |

---

### FSD-BR-03 — Solo [CC] carga (salvo delegación auditada)

| Campo | Valor |
|-------|-------|
| **Tipo** | Política |
| **Origen BRD** | BRD-RB-15, BRD-REQ-005 |
| **UC** | UC-004 |
| **Enunciado** | Rol [TD] no puede sustituir la carga operativa del [CC] salvo delegación formal registrada en auditoría. |
| **Violación** | `403 FORBIDDEN_ROLE` |
| **Verificación** | Matriz RBAC endpoint `POST /evidences`. |

---

### FSD-BR-04 — Solo [TD] aprueba/rechaza Indicador

| Campo | Valor |
|-------|-------|
| **Tipo** | Autorización |
| **Origen BRD** | BRD-REQ-009 |
| **UC** | UC-008, UC-009 |
| **Enunciado** | Transiciones de validación normativa del Indicador (`APROBADO`, `OBSERVADO`) exclusivas de [TD]. [JD] no sustituye dictamen técnico salvo política institucional explícita documentada en ADR. |
| **Violación** | `403 FORBIDDEN_ROLE` (TC-SAD-004 si [CC] aprueba). |

---

### FSD-BR-05 — Rechazo requiere justificación

| Campo | Valor |
|-------|-------|
| **Tipo** | Validación |
| **Origen BRD** | BRD-REQ-008 |
| **UC** | UC-008 |
| **Enunciado** | Todo rechazo crea `Observation` con `justification` no vacía (mínimo configurable, default 20 caracteres). |
| **Violación** | `422 JUSTIFICATION_REQUIRED` |
| **Verificación** | TC-SAD-003; PRD-US-009. |

---

### FSD-BR-06 — Subsanación enlaza observationId

| Campo | Valor |
|-------|-------|
| **Tipo** | Trazabilidad |
| **Origen BRD** | BRD-RB-16 |
| **UC** | UC-006 |
| **Enunciado** | Cada versión subsanatoria debe referenciar el `observationId` que originó la corrección (`supersedes_id` + `observation_id` en DDL). |
| **Violación** | `400 SUBSANATION_WITHOUT_OBSERVATION` |
| **Verificación** | PRD-US-006; consulta detalle versión 2. |

---

### FSD-BR-07 — Cierre de Fase solo si todos APROBADO

| Campo | Valor |
|-------|-------|
| **Tipo** | Estado |
| **Origen BRD** | BRD-CST-03, BRD-REQ-014 |
| **UC** | UC-010 |
| **Enunciado** | `COUNT(indicadores_fase) = COUNT(indicadores WHERE estado = APROBADO)` antes de cerrar Fase. |
| **Violación** | `409 FASE_CIERRE_BLOQUEADO` + lista pendientes |
| **Verificación** | TC-SAD-002; PRD-US-011. |

---

### FSD-BR-08 — Un Proceso activo por tipo/carrera/periodo

| Campo | Valor |
|-------|-------|
| **Tipo** | Negocio |
| **Origen BRD** | BRD-RB-02, BRD-REQ-013 |
| **UC** | UC-003 |
| **Enunciado** | Máximo un `AccreditationProcess` activo por combinación modalidad (CEUB/ARCU-SUR), carrera y periodo/gestión. |
| **Violación** | `409 PROCESS_ALREADY_ACTIVE` |
| **Verificación** | Índice único parcial en DDL; TC-03c. |

---

### FSD-BR-09 — [CC] solo ve su carrera

| Campo | Valor |
|-------|-------|
| **Tipo** | Seguridad |
| **Origen BRD** | BRD-CST-04 |
| **UC** | UC-007, UC-011, UC-019 |
| **Enunciado** | Consultas y mutaciones de [CC] filtradas por `academic_program_id` de su asignación. |
| **Violación** | `403 FORBIDDEN_SCOPE`; 0 incidentes críticos (NFR-009). |
| **Verificación** | Test aislamiento carrera A vs B. |

---

### FSD-BR-10 — Portal sin borradores

| Campo | Valor |
|-------|-------|
| **Tipo** | Publicación |
| **Origen BRD** | BRD-REQ-016 |
| **UC** | UC-016 |
| **Enunciado** | Endpoints públicos solo exponen entidades con `published=true` autorizadas por [JD]. |
| **Violación** | Contenido no publicado invisible (404 o lista filtrada). |
| **Verificación** | PRD-US-016 escenario portal sin borradores. |

---

### FSD-BR-11 — Dictamen final solo humano [JD]/[TD]

| Campo | Valor |
|-------|-------|
| **Tipo** | Ética |
| **Origen BRD** | BRD-RB-14 |
| **UC** | — (transversal) |
| **Enunciado** | Ningún agente IA ni regla automática emite dictamen de acreditación vinculante. IA solo sugiere con trazabilidad (v2+). |
| **Verificación** | Revisión de alcance release; ausencia de endpoint auto-dictamen. |

---

### FSD-BR-12 — Correo solo @umss.edu.bo

| Campo | Valor |
|-------|-------|
| **Tipo** | Seguridad |
| **Origen BRD** | BRD-RB-13 |
| **UC** | UC-001, UC-002 |
| **Enunciado** | Cuentas internas con dominio institucional UMSS. |
| **Violación** | Alta (UC-002): `422 INVALID_EMAIL_DOMAIN`. Login (UC-001 A1): dominio inválido, vacío o credencial incorrecta → mismo `401 AUTH_INVALID_CREDENTIALS` genérico (no revelar causa). |

---

### FSD-BR-13 — Notificación crítica ≤ 15 min

| Campo | Valor |
|-------|-------|
| **Tipo** | SLA |
| **Origen BRD** | BRD-REQ-011 |
| **UC** | UC-015 |
| **Enunciado** | Eventos críticos (rechazo, aprobación, nueva Evidencia pendiente revisión) encolados y enviados en ≤ 15 min (NFR-004). |
| **Verificación** | Métricas `notification_outbox.sent_at - created_at`. |

---

### FSD-BR-14 — Reporte externo solo con autorización [JD]

| Campo | Valor |
|-------|-------|
| **Tipo** | Política |
| **Origen BRD** | BRD-RB-12 |
| **UC** | UC-014 |
| **Enunciado** | Generación de reporte ejecutivo PDF restringida a [JD]. |
| **Violación** | `403 FORBIDDEN_ROLE` |

---

### FSD-BR-15 — Intentos DELETE registrados en auditoría

| Campo | Valor |
|-------|-------|
| **Tipo** | Seguridad |
| **Origen BRD** | BRD-RB-18 |
| **UC** | UC-005 |
| **Enunciado** | Todo intento de borrado denegado genera fila `audit_log` con actor, timestamp y recurso. |
| **Verificación** | TC-SAD-001 postcondición bitácora. |

---

### FSD-BR-16 — No módulos ERP en v1

| Campo | Valor |
|-------|-------|
| **Tipo** | Alcance |
| **Origen BRD** | BRD-CST-07 |
| **UC** | — |
| **Enunciado** | SIGESA v1 no integra SIIS, RRHH, nómina ni tesorería. |
| **Verificación** | NFR-016; gate release checklist. |

---

### FSD-BR-17 — Plazos normativos no editables por [CC]

| Campo | Valor |
|-------|-------|
| **Tipo** | Normativa |
| **Origen BRD** | BRD-RB-09 |
| **UC** | UC-003 |
| **Enunciado** | Fechas límite de Fase definidas en plantilla; [CC] no las modifica. |
| **Violación** | `403 FORBIDDEN_ROLE` en PATCH de plazos. |

---

### FSD-BR-19 — [EE] solo lectura documental

| Campo | Valor |
|-------|-------|
| **Tipo** | Autorización |
| **Origen BRD** | BRD-REQ-001 |
| **UC** | UC-019 |
| **Enunciado** | El evaluador externo [EE] solo consulta documentación de su carrera asignada. Prohibidas carga/subsanación de Evidencia, dictamen de Indicador, cierre de Fase, administración de usuarios y exportación de reportes. |
| **Violación** | `403 FORBIDDEN_ROLE` |
| **Verificación** | PRD-US-026; tests security EE vs POST evidencias/export. |

---

### FSD-BR-18 — Progreso obligatorio en cargas > umbral

| Campo | Valor |
|-------|-------|
| **Tipo** | UX |
| **Origen BRD** | BRD-REQ-025 |
| **UC** | UC-004 |
| **Enunciado** | Evidence > **5 MB** requiere barra de progreso determinada y bloqueo de doble envío (NFR-011). |
| **Verificación** | PRD-US-025; E2E upload. |

---

### FSD-BR-20 — Un [CC] responsable por proceso activo

| Campo | Valor |
|-------|-------|
| **Tipo** | Negocio |
| **Origen BRD** | BRD-REQ-002 |
| **UC** | UC-023 |
| **Enunciado** | Un Coordinador [CC] solo puede ser **responsable** de **un** `AccreditationProcess` en estado `ACTIVE` a la vez. Un proceso `ACTIVE` tiene como máximo un responsable [CC]. |
| **Violación** | `409 CC_ALREADY_ASSIGNED_TO_PROCESS` |
| **Verificación** | Tests de asignación doble; UI filtra candidatos no disponibles. |

---

### FSD-BR-21 — Plantilla editada no migra procesos en curso

| Campo | Valor |
|-------|-------|
| **Tipo** | Negocio |
| **Origen BRD** | BRD-REQ-004 |
| **UC** | UC-003, UC-021, UC-022 |
| **Enunciado** | Cambios en plantilla normativa o estructura editorial no alteran retroactivamente procesos ya instanciados; solo afectan **nuevos** procesos o instancias editadas explícitamente vía UC-022. |
| **Violación** | N/A (regla preventiva de diseño) |
| **Verificación** | Test: editar plantilla PUBLISHED no cambia árbol de proceso ACTIVE existente. |

---

### FSD-BR-22 — Borrado estructural condicionado

| Campo | Valor |
|-------|-------|
| **Tipo** | Estructura |
| **Origen BRD** | BRD-REQ-004 |
| **UC** | UC-022 |
| **Enunciado** | No se puede eliminar una **subfase** de proceso si tiene indicadores con evidencia en workflow iniciado (estado ≠ vacío/`PENDIENTE` sin carga). No se elimina **fase** si alguna subfase bloquea. |
| **Violación** | `409 SUBPHASE_HAS_EVIDENCE` |
| **Verificación** | Tests delete subfase con evidencia SUBIDA/OBSERVADA/APROBADA. |

---

### FSD-BR-23 — Gestión normativa solo [JD]

| Campo | Valor |
|-------|-------|
| **Tipo** | Autorización |
| **Origen BRD** | BRD-REQ-004 |
| **UC** | UC-021, UC-022, UC-023 |
| **Enunciado** | Crear/editar/eliminar plantillas, estructura de proceso instanciado y asignación de responsable [CC] es exclusivo de Jefatura DUEA [JD]. |
| **Violación** | `403 FORBIDDEN_ROLE` |
| **Verificación** | Tests security TD/CC en endpoints TPL y PROC estructura. |

---

## Matriz regla → UC

| Regla | UC aplicables |
|-------|---------------|
| BR-01 | 004 |
| BR-02, BR-15 | 005 |
| BR-03 | 004 |
| BR-04 | 008, 009 |
| BR-05 | 008 |
| BR-06 | 006 |
| BR-07 | 010 |
| BR-08 | 003 |
| BR-09 | 007, 011, 019, 023 |
| BR-10 | 016 |
| BR-12 | 001, 002 |
| BR-13 | 015 |
| BR-14 | 014 |
| BR-17 | 003 |
| BR-18 | 004 |
| BR-19 | 019, 020 |
| BR-20 | 023 |
| BR-21 | 003, 021, 022 |
| BR-22 | 022 |
| BR-23 | 021, 022, 023 |

---

## Registro de cambios

| Versión | Fecha | Cambio |
|---------|-------|--------|
| Dorada v1.0 | 2026-05-16 | Extracción y detalle de 18 reglas desde FSD.md |
| v1.1 | 2026-08-07 | BR-20…23 para plantillas, estructura de proceso y responsable [CC] (UC-021…023) |
