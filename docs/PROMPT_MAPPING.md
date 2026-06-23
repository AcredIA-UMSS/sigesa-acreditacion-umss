| PR-IMPL-003 | DD-UC-003 | FSD-UC-003 | Implementación de Plantillas y Creación de Procesos |
| — | DD-UC-001 | FSD-UC-001, FSD-UC-002 | Autenticación y Gestión de Usuarios (MOD-AUTH) → PM-001 |
| PR-IMPL-004 | DD-UC-001 | FSD-UC-001, FSD-UC-002 | Implementación MOD-AUTH (pendiente ejecución) |

---

## PM-001

| Campo | Valor |
|---|---|
| **ID** | PM-001 |
| **Fecha** | 2026-06-22 |
| **Hora** | 22:30 |
| **Solicitante** | Usuario (Tech Lead / docente) |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Composer |
| **Tarea** | `@feature-design-doc` — MOD-AUTH (FSD-UC-001, FSD-UC-002) |
| **Objetivo** | Crear `DD-UC-001` y registrar el prompt en `PROMPT_MAPPING.md` |
| **Contexto** | Plantilla `FEATURE_DESIGN_DOC_TEMPLATE.md`; release v1.0; hexagonal estricta; JWT; `user_program_assignment`; ADR-0003 |
| **PR-IMPL vinculado** | pendiente |
| **DD-UC vinculado** | DD-UC-001 |
| **FSD-UC vinculado** | FSD-UC-001, FSD-UC-002 |
| **Estado** | completado |

### Prompt usado exacto

```
@feature-design-doc FSD-UC-001,FSD-UC-002 titulo="Autenticación y Gestión de Usuarios (MOD-AUTH)" release=v1.0

Usa docs/plantillas/FEATURE_DESIGN_DOC_TEMPLATE.md como base exacta.
Completa el frontmatter: id=DD-UC-001, fsd_uc=[FSD-UC-001, FSD-UC-002],
prd_refs=[PRD-REQ-001, PRD-US-001, PRD-US-002, PRD-US-003], adrs=[ADR-0003],
prompts=[] (se llena después), status=borrador, autores=[<tu nombre>].

Contexto de diseño a respetar en las secciones 1-7:
- Sesión basada en JWT (FSD-UC-001 paso 3), con claims de rol y programScope
- Login no debe revelar si el usuario existe o no (FSD-UC-001, excepción A1)
- Usuario se crea INACTIVO hasta primer acceso (FSD-UC-002, paso 3)
- Relación usuario-alcance como entidad separada (user_program_assignment),
  no como campo plano en User (soporta FSD-BR-09)
- Revocación de acceso conserva historial de auditoría (FSD-UC-002, A1)
- En §3 (Alternativas), evalúa AuthPort/LocalAuthAdapter vs acoplar
  directamente a Spring Security, y concluye si requiere ADR-0003
- En §2, arquitectura hexagonal estricta: dominio sin dependencias de
  Spring/JPA
- En §6, deriva el plan de pruebas de los Gherkin de FSD-UC-001 y FSD-UC-002 y guardalo en prompt_mapping el prompt
```

### Entradas auxiliares

- `docs/plantillas/FEATURE_DESIGN_DOC_TEMPLATE.md`
- `docs/product/uc/FSD-UC-001.md`
- `docs/product/uc/FSD-UC-002.md`
- `docs/product/api_contracts.md`
- `docs/baseline/05_dti/adrs/ADR_003_adapter_autenticacion.md`
- `docs/product/reglas_negocio.md`
- `.cursor/skills/feature-design-doc/SKILL.md`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `docs/design/DD-UC-001.md` |
| modificado | `docs/PROMPT_MAPPING.md` |

### Cambios realizados

- Creado `DD-UC-001` (secciones 1–7): hexagonal MOD-AUTH, DDL, API, alternativas AuthPort vs Spring Security, plan de pruebas Gherkin, DoD.
- Append `PM-001` en `PROMPT_MAPPING.md` (fila resumen + entrada completa).

### Validación ejecutada

- [x] `git status --short` → `docs/design/DD-UC-001.md`, `docs/PROMPT_MAPPING.md`
- [ ] `mvn test` — no aplica (solo diseño)

### Resultado obtenido

`DD-UC-001` en **borrador**; prompt registrado como **PM-001**.

### Riesgos / observaciones

- `PR-IMPL-NNN` pendiente tras aprobar DD.

### Lecciones / reuso

- `@feature-design-doc` + plantilla estándar + `@save-prompt-mapping`.

### Próximos pasos

- [ ] Aprobar `DD-UC-001`
- [ ] Crear `PR-IMPL-NNN` e implementar MOD-AUTH
- [ ] `@dtp-sync` tras merge
