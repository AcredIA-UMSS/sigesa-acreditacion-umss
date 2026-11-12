---
id: DD-UC-032
titulo: "Observaciones del técnico sobre evidencias del proceso"
producto: "SIGESA"
grupo: "ACREDIA"
fsd_uc:
  - "FSD-UC-032"
adrs: []
prompts:
  - "PR-IMPL-046"
release: "v1.0"
status: implementado
ultima_actualizacion: "2026-11-04"
---

# Design Doc `DD-UC-032` — Observaciones del técnico sobre evidencias

> El [TD] evalúa una evidencia ya cargada y el [CC] de esa carrera responde. No mueve el estado del indicador y no escribe en `tb_observation`.

## 1. Objetivo y contexto

| Incluido | Excluido |
| --- | --- |
| Borrador, envío, revisión, respuesta, atención y cierre | Rechazo de indicador (FSD-UC-008) |
| Historial append-only de mensajes | Notificación por correo |
| Lista de evidencias de la carrera del proceso | [JD] en este flujo |
| Panel en el detalle de proceso | Cambiar `tb_observation` del dashboard |

La evidencia pertenece al proceso cuando `indicator.program_id` es la `career_id` del proceso. Ese es el vínculo que ya existe: `evidence.indicator_id` → `indicator.program_id`.

## 2. Diseño

### 2.1 Máquina de estados

El agregado `ProcessObservation` es el único que cambia de estado.

| Desde | Hacia | Quién |
| --- | --- | --- |
| — | `BORRADOR` | [TD] |
| `BORRADOR` | `ENVIADA` | [TD] autor |
| `ENVIADA` | `EN_REVISION` | [CC] |
| `EN_REVISION` | `RESPONDIDA` | [CC], con mensaje |
| `RESPONDIDA` | `ATENDIDA` | [CC], con mensaje |
| `ATENDIDA` | `CERRADA` | [TD], con mensaje |

Un [CC] fuera de la carrera recibe el mismo `ProcessNotFoundException` que la consulta de procesos.

### 2.2 Capas

| Capa | Pieza |
| --- | --- |
| Dominio | `ProcessObservation`, `ObservationMessage`, enums de estado, tipo, prioridad y cumplimiento |
| Aplicación | `ProcessObservationUseCase`, `ProcessObservationService`, `ObservationActor` |
| Puertos out | `ProcessObservationRepositoryPort`, `EvidenceLookupPort`. Reutiliza `ProcessQueryPort` y `ProcessAccessPolicy` |
| Adaptadores | `ProcessObservationController`, `ProcessObservationJpaAdapter`, `EvidenceLookupJpaAdapter` |
| Persistencia | `process_observation`, `process_observation_message` (Flyway `V9`) |
| Frontend | `ObservationsPanel` dentro de `ProcessDetailView` |

### 2.3 API

Contrato vivo: `docs/product/api_contracts.md` §9.1, API-OBS-01 a API-OBS-10.

### 2.4 Pruebas

- `ProcessObservationTest`: flujo completo, envío incompleto, respuesta fuera de estado.
- `ProcessObservationServiceTest`: alta de [TD], 404 de [CC] ajeno, hilo de respuesta.

## 3. Consecuencias

El panel del detalle de proceso usa `customFetch`, el mutador de Orval. La regeneración de `src/api/` queda para cuando el backend publique `/v3/api-docs`.
