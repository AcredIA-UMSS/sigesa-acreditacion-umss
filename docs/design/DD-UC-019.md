---
id: DD-UC-019
titulo: "Revisión documental evaluador externo [EE] (MOD-REVIEW)"
producto: "SIGESA"
grupo: "ACREDIA"
fsd_uc:
  - "FSD-UC-019"
prd_refs:
  - "PRD-REQ-029"
  - "PRD-US-026"
adrs: []
prompts:
  - "PR-IMPL-014"
release: "v1.1"
status: aprobado
fecha: "2026-08-03"
autores:
  - "Cursor Agent"
---

# Design Doc `DD-UC-019` — Evaluador externo [EE] (MOD-REVIEW)

> **Qué es**: habilitación del rol **[EE]** para revisión documental **solo lectura** de la carrera asignada, reutilizando proyecciones del dashboard coordinador (MOD-DASH) sin capacidades de mutación.

## 1. Objetivo y contexto

- **Problema**: evaluadores externos (ARCU-SUR / CEUB) necesitan revisar evidencias e indicadores de una carrera sin permisos operativos de [CC] ni dictamen de [TD].
- **Caso de uso**: [`FSD-UC-019`](../product/uc/FSD-UC-019.md)
- **Alcance v1.1**:

| Incluido | Excluido |
|----------|----------|
| Enum `Role.EE` | Correos fuera de `@umss.edu.bo` |
| Alta [EE] por [JD] con `programId` obligatorio | Multi-carrera por usuario EE |
| JWT con `programScope[]` | Aprobación/rechazo de indicadores |
| Dashboard KPI solo lectura (reutiliza `CoordinatorKpiSection`) | Carga/subsanación de evidencias |
| `GET /dashboards/me/summary` y `/coordinator/details` | Exportación de reportes |
| RBAC en `SecurityConfig` | Portal público [P] (UC-016) |

## 2. Diseño

### 2.1 Modelo de dominio

- Extender `Role`: `CC, TD, JD, EE`.
- [EE] comparte patrón de alcance con [CC]: `user_program_assignment` obligatorio al alta.

### 2.2 Servicios

| Servicio | Cambio |
|----------|--------|
| `RegisterUserService` | `EE` requiere `programId`; crea assignment |
| `DashboardSummaryAggregationService` | Si `ROLE_EE` + scope → expone `coordinatorSection` (read-only) |
| `UploadEvidenceService` | Sin cambio; bloqueado por `SecurityConfig` (`hasRole("CC")`) |

### 2.3 Seguridad (`SecurityConfig`)

| Endpoint | Roles permitidos |
|----------|------------------|
| `POST /indicators/*/evidences` | CC |
| `POST /admin/users/**` | JD |
| `POST /reports/**` | JD |
| `POST /processes` | JD |
| `POST /dashboards/**/export-jobs` | CC, TD, JD (**excluye EE**) |
| `GET /dashboards/**` | autenticado (scope en servicio) |

### 2.4 Frontend

- `roleLabels.ts`: agregar `EE` con `ROLE_REQUIRES_PROGRAM.EE = true`.
- Rutas de escritura protegidas: `JdOnlyRoute`, nuevo `CcOnlyRoute` para `/evidencias/cargar`.
- Sidebar: [EE] ve Panel + Ayuda únicamente.
- `CoordinatorDashboardSection`: prop `readOnly` oculta exportación.

## 3. Reglas de negocio

- **FSD-BR-09**: scope por carrera (igual que CC).
- **FSD-BR-19** (nueva): [EE] solo lectura; prohibidas mutaciones de evidencia, indicador, fase, usuarios y reportes.

## 4. Plan de pruebas

- Unit: `RegisterUserServiceTest` alta EE + scope.
- Unit: `DashboardSummaryAggregationService` sección coordinador para EE.
- Integration: login EE → summary con `coordinatorSection` poblado.
- Security: EE → POST evidencias → 403.

## 5. Trazabilidad

`PRD-US-026` → `FSD-UC-019` → `DD-UC-019` → `PR-IMPL-014` → código → DTP §A.3
