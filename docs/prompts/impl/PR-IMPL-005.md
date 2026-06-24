---
id: PR-IMPL-005
feature_asociado: DD-UC-004
fsd_uc:
  - FSD-UC-011
  - FSD-UC-012
  - FSD-UC-013
  - FSD-UC-014
fecha: "2026-06-23"
version: "1.0"
estado: Aprobado
autor: "alexAlvarez / @sigesa-prompt-contract-architect"
skill_origen: sigesa-prompt-contract-architect
---

# Prompt Contract — Implementación `PR-IMPL-005`

> **Design doc fuente:** [`DD-UC-004`](../../design/DD-UC-004.md) · **FSD:** UC-011–014 · **ADR:** [ADR-0015](../../adr/ADR-0015-dashboard-sync-async-reporting.md).

## 1. Propósito y Objetivo

Implementar el módulo **MOD-DASH / MOD-REPORT** en `com.umss.sigesa.reports` según [`DD-UC-004`](../../design/DD-UC-004.md):

- Lecturas sync: KPIs y datos paginados con `SecurityInjector` + JWT (MOD-AUTH).
- Export async: `ReportRun`, worker Virtual Threads, Excel POI, upload local/S3.
- Tests con JaCoCo ≥ 90 % en `DashboardServiceImpl`.

## 2. Rol y Persona

- **Identidad:** Desarrollador Backend Senior SIGESA (Java 21, Spring Boot 4.x).
- **Expertise:** JPA, DTOs, RBAC, streaming Excel, Testcontainers E2E.

## 3. Límites de Alcance

### In-Scope

- Paquete `com.umss.sigesa.reports.*` (domain, repository, service, web, security).
- Endpoints: `/api/v1/dashboard/*`, `/api/v1/reports/*`.
- Entidades `ReportDefinition`, `ReportRun`; `FilterPayload` POJO.
- `SecurityInjector` mutando filtros según rol/alcance JWT.
- Tests unitarios, controller, integración repo, E2E opcional Docker.
- Documentación en `docs/design/DD-UC-004.md` (no `design_docs/`).

### Out-of-Scope

- Frontend AcredIA DS (repo `sigesa-front`).
- `GET /dashboard/executive` (v1.1).
- PDF ejecutivo completo (stub job async aceptable).
- Modificar `docs/baseline/`.

## 4. Restricciones y Reglas

- Salida de diseño: `docs/design/DD-UC-004.md` (plantilla FEATURE_DESIGN_DOC_TEMPLATE).
- DTOs en controllers; nunca entidades JPA expuestas.
- Evidence append-only; reportes solo lectura.
- Tras implementar: `@save-prompt-mapping PR-IMPL-005` → `@dtp-sync`.
- Cobertura servicio ≥ 90 % (`agents.md`).

## 5. Contexto de Entrada

- **Design Doc:** `docs/design/DD-UC-004.md`
- **FSD vivo:** `docs/product/uc/FSD-UC-011.md` … `014.md`
- **API:** `docs/product/api_contracts.md` §MOD-DASH
- **Convenciones:** `docs/plantillas/BASE_DESIGN_SYSTEM_BACKEND.md`

## 6. Salida Esperada

| Artefacto | Ruta |
|-----------|------|
| Código | `src/main/java/com/umss/sigesa/reports/**` |
| Tests | `src/test/java/com/umss/sigesa/reports/**` |
| Config | `application.yaml`, `application-postgres.yaml`, `data.sql` |
| Docker | `docker-compose.yml`, `scripts/run_e2e_docker.sh` |

## 7. QA Checklist

- [ ] Alineado a `DD-UC-004` §2
- [ ] RBAC SecurityInjector en endpoints dashboard
- [ ] Export async 202 + poll run
- [ ] JaCoCo ≥ 90 % DashboardServiceImpl
- [ ] PM-NNN en `docs/PROMPT_MAPPING.md`
- [ ] Sin archivos en `design_docs/` (rutas canónicas `docs/`)
