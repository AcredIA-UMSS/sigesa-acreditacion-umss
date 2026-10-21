---
id: PR-IMPL-047
feature_asociado: DD-UC-019
fsd_uc:
  - FSD-UC-019
fecha: "2026-10-20"
version: "1.0"
estado: Aprobado
autor: "Boris Anthony Angulo Urquieta"
release: "2.0.0"
---

# Prompt Contract — Implementación `PR-IMPL-047`

## 1. Propósito

Exponer `GET /api/v1/processes/{processId}/phases/{phaseId}/progress` como consulta
de avance de una fase, agregando los estados de las unidades hijas sin agregar
tablas ni dependencias.

## 2. Alcance

- Responder `phaseId`, `totalSubphases`, `approvedSubphases`, `pendingSubphases`,
  `progressPercent` (un decimal) y `state`.
- Validar UUIDs en la ruta (400); responder 404 para proceso/fase inexistente o
  una fase ajena al proceso.
- Probar el resumen parcial, completo, vacío, UUID inválido y referencias no
  encontradas.
- Actualizar el contrato API, DTP y registro de prompt mapping.

## 3. Decisión de compatibilidad

El modelo de esta rama no contiene `Phase`, `Subphase` ni `SubphaseState`. Por
decisión confirmada por el usuario, el endpoint usa el modelo vigente: fase =
`Level1Node`; subfase contable = `NormativeIndicator` asociado al nivel 1; aprobado
= `IndicatorState.APROBADO`; `state` = `PhaseState` del nivel 1. El progreso con
cero indicadores es `0.0`. DD-UC-019 es la referencia de consulta de procesos;
DD-UC-010 aporta la referencia de estados del workflow.

## 4. Restricciones

- Arquitectura hexagonal y DTO REST separado de los modelos de persistencia.
- Reutilizar el puerto de consulta normativa existente.
- No crear tablas, dependencias ni tocar `docs/baseline/`.
- Registrar como `PM-029` en Sprint 03.
