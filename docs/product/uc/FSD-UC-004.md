---
id: FSD-UC-004
nombre: Cargar Evidencia en Subfase
estado: Implementado
release: v1.0
actor_principal: "[CC]"
trazabilidad_prd: PRD-US-005, PRD-US-025
modulo: MOD-EVIDENCE
reglas: FSD-BR-01, FSD-BR-03, FSD-BR-18
ultima_actualizacion: "2026-08-27"
---

# FSD-UC-004 — Cargar Evidencia en Subfase

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-005, 022 · PRD-US-005, 025 |
| **Precondiciones** | Subfase en proceso ACTIVE; [CC] con alcance sobre la carrera del proceso; sin observación OPEN pendiente |

## Flujo principal

1. [CC] abre detalle del proceso (`/procesos/{id}`) y selecciona **Subir evidencia** en la subfase.
2. Adjunta archivo y descripción; envía multipart a `POST /api/v1/subphases/{subphaseId}/evidences`.
3. Sistema valida tipo/tamaño; calcula SHA-256.
4. Evidence Service persiste `Evidence` v1 con FK `subphase_id`.
5. Subfase transiciona a `SUBIDO`.
6. Notification Service notifica al [TD] (UC-015).
7. Si Evidence > 5 MB: barra de progreso y carga asíncrona (US-025).

## Excepciones y flujos alternos

| Condición | Respuesta |
|-----------|-----------|
| Sin `subphaseId` o metadatos | `400 EVIDENCE_UNCLASSIFIED` |
| Observación OPEN pendiente | `409 SUBSANATION_NOT_ALLOWED` |
| Formato inválido | `422 INVALID_EVIDENCE_FORMAT` |

## Postcondiciones

`evidenceId`, `version=1`, `contentHash`, evento `EvidenceUploaded`; subfase en `SUBIDO`.

## Datos

| Entrada | Salida |
|---------|--------|
| `subphaseId`, `file`, `description` | `evidenceId`, `version`, `contentHash` |

**API:** `POST /api/v1/subphases/{subphaseId}/evidences` (multipart). Listado: `GET /api/v1/subphases/{subphaseId}/evidences`.

**Copiloto embebido (UC-024):** modal dev de trazabilidad con `VITE_EVIDENCE_COPILOT_DEBUG_ACTIONS`.

## Diagramas

- [Carga evidencia versionada](../diagramas/MAR-SEQ-002-carga-evidencia-versionada.mmd)
- [Estados subfase](../diagramas/FSD-UC-006_008_009_estados_subfase.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-005 @FSD-UC-004 @FSD-BR-01 @TC-04
Característica: Carga de Evidencia en subfase

  Escenario: Carga exitosa con metadatos obligatorios
    Dado un [CC] autenticado y una subfase válida en su carrera
    Cuando carga una Evidencia con descripción y archivo
    Entonces el sistema crea la Evidencia versión 1 vinculada a la subfase
    Y notifica al [TD] que hay revisión pendiente

  Escenario: Carga sin metadatos rechazada
    Dado un [CC] en el modal de carga de subfase
    Cuando intenta guardar sin descripción o archivo
    Entonces el sistema rechaza la operación
```
