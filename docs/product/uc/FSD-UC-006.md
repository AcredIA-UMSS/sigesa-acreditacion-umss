---
id: FSD-UC-006
nombre: Subsanar Evidencia
estado: Reespecificado
release: v2.0
actor_principal: "[CC]"
trazabilidad_prd: PRD-US-006
modulo: MOD-EVIDENCE
reglas: FSD-BR-06
ultima_actualizacion: "2026-09-08"
nota_implementacion: "Código v1.x subsana por subfase; migración v2.0 pendiente"
---

# FSD-UC-006 — Subsanar Evidencia

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-008 · PRD-US-006 · BRD-RB-16 |
| **Precondiciones** | Indicador con evidencia cargada; observación TD/JD en estado `OPEN` |
| **Alcance v2.0** | Subsanación **por indicador**; historial liviano (metadatos sin blob en versiones anteriores) |

## Flujo principal

1. [TD]/[JD] registra observación sobre el **indicador** → `indicator_observation.status = OPEN`.
2. Sistema bloquea nuevas cargas y observaciones mientras exista observación OPEN.
3. [CC] consulta elegibilidad (`GET .../subsanation-eligibility`).
4. [CC] subsana **una vez** por observación: `POST .../evidences/{evidenceId}/subsanate` con `file`, `description`, `observationId`.
5. Sistema crea versión N+1 enlazada a la observación; marca observación `RESOLVED`.
6. Versión anterior conserva metadatos en `evidence_version` pero elimina blob en disco (`blob_purged=true`).
7. Audit / evento `EvidenceSubsanated`; notificación a [TD] (UC-015 — pendiente worker).

## Excepciones y flujos alternos

| Condición | Respuesta |
|-----------|-----------|
| Sin observación OPEN | `409 SUBSANATION_NOT_ALLOWED` |
| Segunda subsanación misma observación | `409 SUBSANATION_NOT_ALLOWED` |
| Nueva carga con observación OPEN | `409 SUBSANATION_NOT_ALLOWED` |
| Segunda observación con OPEN existente | `409 INVALID_STATE` |
| Rol distinto de CC en subsanación | `403` |

## Postcondiciones

- Cadena de versiones trazable a `observationId`.
- Solo la versión vigente mantiene archivo descargable.
- Observación queda `RESOLVED` con `resolved_version_id`.

## Diagramas

- [Secuencia subsanación](../diagramas/FSD-UC-006_subsanar_evidencia_secuencia.mmd)
- [Journey CC subsanación](../diagramas/PRD_journey_CC_subsanacion_secuencia.mmd)
- [Estados indicador](../diagramas/FSD-UC-006_008_009_estados_indicador.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-006 @FSD-UC-006 @FSD-BR-06 @TC-06
Característica: Subsanación de Evidencia en indicador

  Escenario: Subsanación enlazada a observación OPEN
    Dado un indicador con evidencia v1 y observación O-123 en estado OPEN
    Cuando el [CC] subsana la evidencia con un nuevo archivo
    Entonces el sistema registra la versión 2 enlazada a O-123
    Y marca O-123 como RESOLVED
    Y la versión 1 queda en historial solo con metadatos

  Escenario: Una subsanación por observación
    Dado una observación OPEN ya subsanada (RESOLVED)
    Cuando el [CC] intenta subsanar nuevamente con la misma observación
    Entonces el sistema responde 409 SUBSANATION_NOT_ALLOWED
```
