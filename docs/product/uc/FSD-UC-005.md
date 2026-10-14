---
id: FSD-UC-005
nombre: Versionado y bloqueo de borrado
estado: Hecho (M7 DocumentAsset)
release: v2.1
actor_principal: "[CC], [TD] (consulta); cualquier rol (intento DELETE)"
trazabilidad_prd: PRD-US-007, PRD-US-008
modulo: MOD-EVIDENCE
reglas: FSD-BR-02, FSD-BR-15
design_doc: DD-UC-004-M7
pr_impl: PR-IMPL-M7-004
ultima_actualizacion: "2026-10-13"
---

# FSD-UC-005 — Versionado y bloqueo de borrado

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-006, 007 · PRD-US-007, 008 · BRD-CST-01 |
| **Invariante** | Append-only: sin `DELETE` físico en Evidencia aprobada ni en `DocumentAsset` compartido |

## Flujo principal (consulta)

1. Usuario abre historial de versiones de una Evidencia.
2. Sistema lista versiones ordenadas; deriva la vigente por `version DESC` y `supersedesId`.
3. Cada versión expone `documentAssetId` / `folioCode` (M7); si varias evidencias comparten asset, el folio es el mismo.
4. Versiones anteriores en **solo lectura**.
5. Un `DocumentAsset` con más de un `IndicatorEvidenceLink` **no** se borra al inmutabilizar una evidencia; solo se niega DELETE de cada `Evidence`.

## Excepciones y flujos alternos

| Paso | Condición | Respuesta |
|------|-----------|-----------|
| 1 | Cualquier rol invoca `DELETE /evidences/{id}` sobre Evidencia aprobada | `409 EVIDENCE_IMMUTABLE` |
| 2 | — | Registra `AUDIT_DELETE_DENIED` en bitácora (UC-017) |

## Postcondiciones

Historial intacto; intentos de borrado auditados.

## Diagramas

- [Estados evidencia](../diagramas/FSD-UC-004_005_estados_evidencia.mmd)
- [Versionado evidencias](../diagramas/seq-001-versionado-evidencias.mmd)
- [Ciclo vida evidencia](../diagramas/state-flujo-001-ciclo-vida-evidencia.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-007 @FSD-UC-005 @TC-05
Característica: Historial de versiones

  Escenario: Versión vigente visible
    Dado un indicador con Evidencia en versiones 1 y 2
    Cuando el [TD] abre el historial
    Entonces la versión 2 aparece como vigente
    Y la versión 1 permanece consultable en solo lectura

  Escenario: Trazabilidad de subsanación
    Dado la versión 2 creada por subsanación
    Cuando se consulta su detalle
    Entonces muestra el identificador de la observación origen

@PRD-US-008 @FSD-UC-005 @NFR-017 @TC-SAD-001
  Escenario: Intento de eliminar Evidencia aprobada
    Dado una Evidencia en estado Aprobado
    Cuando un usuario intenta eliminarla físicamente
    Entonces el sistema rechaza la operación
    Y registra el intento en la bitácora de auditoría
    Y mantiene todas las versiones existentes
```
