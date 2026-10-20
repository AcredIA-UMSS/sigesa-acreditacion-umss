---
id: PR-IMPL-046
feature_asociado: DD-UC-019
fsd_uc:
  - FSD-UC-019
fecha: "2026-10-20"
version: "1.0"
estado: Aprobado
autor: "Boris Anthony Angulo Urquieta"
release: "2.0.0"
---

# Prompt Contract — Implementación `PR-IMPL-046`

> **Práctica AI-DLC (versión A, sin herramienta AWS):** `GET /api/v1/processes/stats`  
> **Design:** [`DD-UC-019`](../../design/DD-UC-019.md)  
> **FSD:** [`FSD-UC-019`](../../product/uc/FSD-UC-019.md)  
> **Rama:** `feature-sinaidlc-boris` desde `origin/release/2.0.0`

## 1. Propósito

Exponer un resumen de procesos visibles al actor (conteos por `status` y `operationalMode`, más `frozenCount`) sin listar filas.

## 2. In-Scope

- `GetProcessStatsUseCase` / `GetProcessStatsService` sobre `ProcessQueryPort` (mismo filtro de carrera que el listado).
- `GET /api/v1/processes/stats` en `ProcessController`.
- Extender `ProcessListItem` con `operationalMode`.
- Tests unitarios + WebMvc 200/401.
- Contrato vivo API-PROC-03b + DTP/FSD-UC-019.

## 3. Out-of-Scope

- Frontend / Orval, Flyway, AI-DLC de AWS, `docs/baseline/`.

## 4. Cierre

`@save-prompt-mapping sprint=03 pr=PR-IMPL-046`
