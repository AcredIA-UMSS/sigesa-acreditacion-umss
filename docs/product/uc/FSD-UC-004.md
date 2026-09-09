---
id: FSD-UC-004
nombre: Cargar Evidencia en Indicador
estado: Implementado v2
release: v2.0
actor_principal: "[CC]"
trazabilidad_prd: PRD-US-005, PRD-US-025
modulo: MOD-EVIDENCE
reglas: FSD-BR-01, FSD-BR-03, FSD-BR-18
ultima_actualizacion: "2026-09-08"
nota_implementacion: "API-EVD-01 activo: POST/GET /indicators/{id}/evidences (v2 post-M5)."
---

# FSD-UC-004 — Cargar Evidencia en Indicador

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-005, 022 · PRD-US-005, 025 |
| **Precondiciones** | Indicador en proceso ACTIVE; [CC] con alcance sobre la carrera del proceso; sin observación OPEN pendiente en el indicador |

## Flujo principal

1. [CC] abre detalle del proceso (`/procesos/{id}`) y selecciona **Subir evidencia** en el **indicador** (hoja del árbol normativo).
2. Adjunta archivo y/o enlace externo y descripción; envía multipart a `POST /api/v1/indicators/{indicatorId}/evidences`.
3. Sistema valida tipo/tamaño; calcula SHA-256 (archivos).
4. Evidence Service persiste `Evidence` v1 con FK `indicator_id`.
5. **Indicador** transiciona a `SUBIDO`.
6. Notification Service notifica al [TD] (UC-015).
7. Si Evidence > 5 MB: barra de progreso y carga asíncrona (US-025).

## Excepciones y flujos alternos

| Condición | Respuesta |
|-----------|-----------|
| Sin `indicatorId` o metadatos | `400 EVIDENCE_UNCLASSIFIED` |
| Observación OPEN pendiente | `409 SUBSANATION_NOT_ALLOWED` |
| Formato inválido | `422 INVALID_EVIDENCE_FORMAT` |
| Indicador inexistente o fuera de alcance [CC] | `404 INDICATOR_NOT_FOUND` |

## Postcondiciones

`evidenceId`, `version=1`, `contentHash`, evento `EvidenceUploaded`; indicador en `SUBIDO`.

## Datos

| Entrada | Salida |
|---------|--------|
| `indicatorId`, `file` (opc.), `externalUrl` (opc.), `description` | `evidenceId`, `version`, `contentHash` |

**API v2.0:** `POST /api/v1/indicators/{indicatorId}/evidences` (multipart). Listado: `GET /api/v1/indicators/{indicatorId}/evidences`.

**Legacy v1.x:** `POST /api/v1/subphases/{subphaseId}/evidences`.

**Copiloto embebido (UC-024):** modal dev de trazabilidad con `VITE_EVIDENCE_COPILOT_DEBUG_ACTIONS`.

## Diagramas

- [Carga evidencia versionada](../diagramas/MAR-SEQ-002-carga-evidencia-versionada.mmd)
- [Estados indicador](../diagramas/FSD-UC-006_008_009_estados_indicador.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-005 @FSD-UC-004 @FSD-BR-01 @TC-04
Característica: Carga de Evidencia en indicador

  Escenario: Carga exitosa con metadatos obligatorios
    Dado un [CC] autenticado y un indicador válido en su carrera
    Cuando carga una Evidencia con descripción y archivo
    Entonces el sistema crea la Evidencia versión 1 vinculada al indicador
    Y notifica al [TD] que hay revisión pendiente

  Escenario: Carga sin metadatos rechazada
    Dado un [CC] en el modal de carga de indicador
    Cuando intenta guardar sin descripción o sin archivo/enlace
    Entonces el sistema rechaza la operación
```
