# Convenciones backend — SIGESA (meta-plantilla)

**Versión:** 1.1  
**Ubicación canónica:** `docs/plantillas/BASE_DESIGN_SYSTEM_BACKEND.md`  
**Stack:** Java 21 · Spring Boot 4.x · Spring Data JPA · H2/PostgreSQL

> Complementa [`FEATURE_DESIGN_DOC_TEMPLATE.md`](FEATURE_DESIGN_DOC_TEMPLATE.md) (design docs por feature).  
> **Migrado desde** `design_docs/base_design_system.md` (2026-06-23).

## Objetivo

Convenciones transversales para módulos backend. Cada feature usa además un **DD-UC-NNN** en `docs/design/`.

## Convenciones principales

- Design docs: frontmatter YAML + secciones 1–7 (`@feature-design-doc`).
- Terminología: [`docs/product/glosario.md`](../product/glosario.md) — Evidence, Fase, [CC], [TD], [JD].
- **Nunca** exponer entidades JPA en controladores; siempre DTOs.
- Evidence aprobada: append-only (sin DELETE físico).
- Auditoría: `created_at`, `updated_at`, `created_by`, `updated_by`.
- Soft delete: `deleted_at`; consultas activas filtran `deleted_at IS NULL`.

## Módulos y paquetes

| Módulo | Paquete | Estilo |
|--------|---------|--------|
| MOD-AUTH, MOD-PROCESS | `com.umss.sigesa.*` hexagonal | Puertos/adaptadores (`DD-UC-001`, `DD-UC-003`) |
| MOD-DASH, MOD-REPORT | `com.umss.sigesa.reports.*` | Capas `domain/repository/service/web` (`DD-UC-004`) |

## Testing y calidad

- JaCoCo ≥ 90 % en capa servicio (`agents.md`).
- Unit (Mockito), `@WebMvcTest` / Boot 4 `spring-boot-starter-webmvc-test`, `@DataJpaTest`.
- Sad Paths: RBAC, filtros inválidos, export failures.

## Plantillas rápidas

- Paginación: `page`, `size`, `sort`.
- Errores: `{ timestamp, status, error, message, path }`.

---

*Usar junto con `docs/design/DD-UC-NNN.md` y `docs/prompts/impl/PR-IMPL-NNN.md`.*
