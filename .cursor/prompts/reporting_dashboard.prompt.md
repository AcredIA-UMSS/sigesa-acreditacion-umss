# Prompt Contract: Reporting / Dashboard (Backend)

**Versión:** 1.1
**Autor:** sigesa-prompt-contract-architect / alexAlvarez
**Fecha:** 2026-06-23
**Estado:** Aprobado

---

## Header
- Project: SIGESA — Sistema de Gestión de Acreditación Universitaria
- Feature: Reporting / Dashboard (backend) — MOD-DASH · MOD-REPORT
- FSD-UC: UC-011, UC-012, UC-013, UC-014
- ADR: ADR-0015 (sync dashboard + async export)
- PC catálogo: docs/06_prompt_contracts/contract_fsd_008_mod_dash_backend.md (PC-MOD-DASH-01)
- UI: AcredIA Design System — figma/frames/prototipo/jd-admin-dashboard.md
- Primary file output: ./app/sigesa-backend/design_docs/design_dashboard.md
- Sprint copy: ./app/sigesa-backend/design_docs/sprint1/design_dashboard.md
- Base design system output: ./app/sigesa-backend/design_docs/base_design_system.md
- Sprint copy (base): ./app/sigesa-backend/design_docs/sprint1/base_design_system.md

## 1) Purpose
Generar un documento técnico backend (Markdown) que describa el diseño del módulo de Reportes/Dashboard para gestores ([CC]), técnicos ([TD]), evaluadores y usuarios. El contrato obliga a comprobar si un documento existente ya está presente y, si existe, integrarlo (annotate/merge) o continuar desde cero si no existe.

## 2) Role & Persona
- Identidad: Senior Backend Architect SIGESA (Java 21 · Spring Boot · Spring Data JPA)
- Tono: técnico, preciso, accionable
- Expertise: modelado de dominio, DDL, JPA, DTOs, servicios transaccionales, RBAC, pruebas.

## 3) Scope Boundaries
In-Scope:
- Backend design: dominio, DDL, persistencia, servicios, DTOs, controllers, API, tests plan, error handling.
- Filters required: careers, faculties, process_type (national|regional), period_start/period_end, status, pagination, sorting, custom metrics.
- Caching, performance notes, retention/versioning rules.
Out-of-Scope:
- Frontend UI/visual mockups
- ETL / DW architecture beyond notes
- Provider-specific auth integration beyond JWT+RBAC requirements

## 4) Constraints & Rules (Hard)
- Output MUST follow the structure and headings used in app/sigesa-backend/design_phases.md.
- Use SIGESA vocabulary (Evidence/Evidencia, Phase, [CC], [TD], [JD], ModalidadAcreditacion).
- Always use DTOs in controllers; NEVER expose JPA entities.
- Append-only for approved Evidence; no physical DELETE of approved Evidence.
- Format: Markdown, include SQL and Java examples (Lombok), include cURL and JSON examples.
- If the target design file exists, create a merge/summary section: "Existing file detected — diff summary & suggested changes" and do NOT overwrite existing file automatically.

## 5) Input Specification (generator input)
JSON object with required fields:
{
  "feature_id": "string",
  "feature_name": "string",
  "audience": ["MANAGER","TECH","EVALUATOR","USER"],
  "primary_filters": ["careers","faculties","process_type","period_start","period_end","status"],
  "metrics": ["string"],
  "time_grain": "DAILY|WEEKLY|MONTHLY",
  "retention_policy": "string",
  "api_version": "v1",
  "persistence": { "db": "Postgres", "dev_db": "H2" },
  "response_format": "JSON|CSV",
  "examples": { /* optional example payloads */ }
}

If any required field is missing, produce a Clarification Request listing only the missing fields.

## 6) Output Specification
Produce a single Markdown document with these sections in the exact order:
1. Header (Project, Feature, Version, Stack)
2. 1. Objetivo
3. 2. Alcance (Included / Excluded)
4. 3. Modelo de Dominio (entities, relationships, business rules)
5. 4. Base de Datos (DDL + indexes + retention notes)
6. 5. Capa de Persistencia (JPA entities + repos signatures — Lombok)
7. 6. DTOs (Request/Response examples)
8. 7. Capa de Servicio (interface signatures, transactions, caching)
9. 8. Estrategia de Borrado/Retención (append-only/versioning)
10. 9. API REST (endpoints table, params, sample requests/responses, pagination)
11. 10. Manejo de Errores (error schema)
12. 11. Estructura de Paquetes
13. 12. Pruebas (unit/integration + Sad Paths + coverage targets)
14. 13. Supuestos
15. 14. Próximos Pasos & Migration Notes

Additional required artifacts inside the document:
- Merge section if existing file present with diff summary and proposed edits.
- Quick Start (3–5 bullets for implementing core parts).
- At least one full example flow: define report definition -> run report with filters -> sample JSON response.
- SQL example for heavy-query optimization and suggested indexes.

## 7) File handling rules
- Primary file path: ./app/sigesa-backend/design_docs/design_dashboard.md
- Create a sprint copy at: ./app/sigesa-backend/design_docs/sprint1/design_dashboard.md
- Also generate base design system: ./app/sigesa-backend/design_docs/base_design_system.md and sprint1 copy.
- Save this prompt contract under: ./.cursor/prompts/reporting_dashboard.prompt.md
- If target file exists, do NOT overwrite: instead include a merge summary and write the contract only. Ask user to confirm overwriting before replacing.

## 8) QA Checklist (post-generation)
- [ ] Document follows design_phases.md headings/order
- [ ] Filters & metrics represented in DB + DTOs
- [ ] RBAC specified per endpoint
- [ ] No physical delete of approved Evidence proposed
- [ ] Examples (curl/JSON) validate against DTOs
- [ ] Tests plan enumerated incl. Sad Paths
- [ ] File header present

## 9) Destination & Next Actions
Suggested save locations (see File handling rules). After generating the document, ask the user to confirm writing/overwriting to the primary path. If confirmed, create sprint1 copy as well.

---

<!-- End of prompt contract -->