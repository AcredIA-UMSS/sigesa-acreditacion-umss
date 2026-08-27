---
id: FSD-UC-024
nombre: Copiloto de control documental
estado: Implementado
release: v1.0
actor_principal: "[TD], [CC], [JD]"
trazabilidad_prd: PRD-REQ-028, PRD-US-005
modulo: MOD-ASSISTANT / MOD-EVIDENCE
reglas: FSD-BR-03, FSD-BR-09, FSD-BR-14
design_doc: DD-AGENT-003
pr_impl: PR-IMPL-026
ultima_actualizacion: "2026-08-21"
---

# FSD-UC-024 — Copiloto de control documental

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-028 (asistente) · UC-004 carga · UC-007/008/009 (extensión futura) |
| **Precondiciones** | Usuario autenticado [TD]/[CC]/[JD]; evidencias cargadas vía UC-004 |
| **Design agente** | [`DD-AGENT-003`](../../design/assistant/DD-AGENT-003.md) |
| **Prompt** | [`PR-IMPL-026`](../../prompts/impl/PR-IMPL-026.md) |

Agente conversacional (`agent=evidence`) que permite **controlar y auditar la documentación subida por el [CC]** sin reemplazar el flujo formal de aprobación/rechazo (UC-008/009, Fase 2).

## Actores y PBAC

| Rol | Modo MVP (Fase 1) |
|-----|-------------------|
| [TD] | Lectura: listar pendientes, detalle, checklist de completitud (alcance institucional) |
| [CC] | Lectura: solo evidencias de su(s) carrera(s) asignada(s) |
| [JD] | Lectura: alcance institucional (supervisión) |
| [EE] | **Sin acceso** al agente (`403`) |

## Flujo principal (Fase 1 — lectura)

1. Usuario abre superficie con copiloto (p. ej. `/evidencias/cargar`) o `/ayuda` con `agent=evidence`.
2. Pregunta en lenguaje natural (p. ej. «¿qué evidencias están pendientes de revisión?»).
3. Sistema selecciona tool (`list_pending_evidences` / `get_evidence_detail` / `check_evidence_completeness`).
4. Tool delega en casos de uso de consulta; aplica PBAC por rol/carrera.
5. Asistente responde con resumen estructurado (programa, subfase, estado, completitud).

## Tools (MVP)

| Tool | Tipo | Descripción |
|------|------|-------------|
| `list_pending_evidences` | read | Subfases/evidencias en `SUBIDO` (documentación lista para control TD) |
| `get_evidence_detail` | read | Metadatos de evidencia/versión (hash, descripción, subfase) |
| `check_evidence_completeness` | read | Checklist: archivo, descripción, subfase, estado |

## Excepciones

| Condición | Respuesta |
|-----------|-----------|
| Rol EE u otro no autorizado | `403` en chat/status `agent=evidence` |
| [CC] consulta carrera ajena | `403` / tool `ACCESS_DENIED` |
| Subfase sin evidencia | Tool `ok=false` con código `EVIDENCE_NOT_FOUND` |

## Postcondiciones

- Sin mutación de estados de subfase en Fase 1.
- Trazabilidad tool en metadata de respuesta (`toolId`, `path`, `sourceTables`).
- Modal de trazabilidad con historial de acciones (siempre visible vía enlace y al enviar mensaje); badge dev opcional con `VITE_EVIDENCE_COPILOT_DEBUG_ACTIONS`.

## Fuera de alcance (Fase 1)

- Aprobar/rechazar (UC-008/009) vía chat.
- Subsanación (UC-006) y versionado UI (UC-005).
- OCR / análisis semántico del PDF (requiere ADR).

## Criterios Gherkin

```gherkin
@FSD-UC-024 @agent-evidence
Escenario: TD lista evidencias pendientes de control
  Dado un [TD] autenticado y al menos una subfase en SUBIDO
  Cuando pregunta "lista las evidencias pendientes de revisión"
  Entonces el asistente invoca list_pending_evidences
  Y responde con programa, subfase y estado SUBIDO

@FSD-UC-024 @agent-evidence
Escenario: CC no ve evidencias de otra carrera
  Dado un [CC] autenticado de la carrera A
  Cuando solicita detalle de una subfase de la carrera B
  Entonces la tool responde ACCESS_DENIED o el ítem no aparece

@FSD-UC-024 @agent-evidence
Escenario: EE no accede al agente evidence
  Dado un [EE] autenticado
  Cuando solicita GET /api/v1/assistant/status?agent=evidence
  Entonces recibe 403
```

## Diagramas

Ver secuencia en [`DD-AGENT-003`](../../design/assistant/DD-AGENT-003.md).
