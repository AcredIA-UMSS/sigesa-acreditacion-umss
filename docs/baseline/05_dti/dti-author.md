---
name: dti-author
description: >
  Puebla secciones del Documento Técnico Inicial (`docs/05_dti/DTI.md`) siguiendo
  `templates/dti.md`. Mantiene sincronía atómica ADR ↔ DTI ↔ `AGENTS.md`
  (un cambio significativo viaja en un único commit).
  Activar cuando el usuario edita `docs/05_dti/DTI.md` o pide explícitamente
  "poblar §N del DTI" / "actualizar AGENTS.md desde el DTI".
allowed-tools:
  - read
  - edit
model-tier: sonnet
fsd-version-min: v2.0
status: stable
owner: AcredIA / UMSS
ubicacion: docs/05_dti/dti-author.md
---

# Skill: dti-author (poblar DTI y sincronizar con AGENTS.md)

> **Convención SIGESA**: este skill vive en **`docs/05_dti/dti-author.md`** junto al DTI canónico.
> Plantilla de origen: [`templates/dti-author_template.md`](../../templates/dti-author_template.md).
> Para Cursor: referenciar desde el control de versión de [`DTI.md`](DTI.md) o invocar `@dti-author`.
> Copia opcional en agentes: `.cursor/skills/sigesa-dti-author/SKILL.md` (sincronizar con este archivo).

## 1. Cuándo activarlo (triggers)

- DURANTE: edición de [`docs/05_dti/DTI.md`](DTI.md) o de un ADR en [`docs/05_dti/adrs/`](adrs/README.md) / [`docs/adr/`](../adr/README.md).
- ARRANCA cuando: el usuario invoca `"@dti-author §N <tema>"` o abre `docs/05_dti/DTI.md`.
- NO ACTIVAR cuando: el usuario define capacidades de producto (PRD) o requerimientos (FSD); este skill asume FSD canónico vigente en `docs/04_fsd/`.

## 2. Entradas obligatorias (Inputs)

El usuario MUST proporcionar al menos una de:

- Sección del DTI a poblar (`§0`–`§21` según [`templates/dti.md`](../../templates/dti.md)).
- Ruta a un ADR recién creado en `docs/adr/ADR-NNNN-*.md` o copia DTI en `docs/05_dti/adrs/ADR_NNN_*.md`.
- Decisión arquitectónica que se quiere reflejar en el DTI + [`AGENTS.md`](../../AGENTS.md).

Si falta cualquiera, responder: `"Necesito la sección del DTI o el ADR fuente antes de redactar."`

## 3. Fuentes de verdad (orden de precedencia)

1. [`templates/dti.md`](../../templates/dti.md) — estructura §0–§21, frontmatter YAML, tags de audiencia.
2. MRD, PRD y FSD canónicos: [`docs/02_mrd/`](../02_mrd/), [`docs/03_prd/`](../03_prd/), [`docs/04_fsd/`](../04_fsd/).
3. ADRs vigentes: [`docs/adr/`](../adr/) (numeración `ADR-0001` …) y vista DTI [`docs/05_dti/adrs/`](adrs/README.md).
4. [`AGENTS.md`](../../AGENTS.md) y [`docs/08_agents/AGENTS.md`](../08_agents/AGENTS.md) (estado declarado para agentes).
5. NFR: [`docs/05_nfr/NFR_ISO25010.md`](../05_nfr/NFR_ISO25010.md).
6. Glosario y estados: [`context/03_domain_glossary.md`](../../context/03_domain_glossary.md), máquina de estados en `team/**/docs/context/04_state_machine.md`.
7. Release objetivo del DTI: rama `release/1.0.0` (DTI Dorada v1.0).

## 4. Procedimiento

1. **Verificar plantilla**: leer `templates/dti.md` y confirmar que el frontmatter YAML está presente en `docs/05_dti/DTI.md`. Si falta, generarlo desde la plantilla.
2. **Identificar audiencia de la sección** (`[humano]`, `[máquina]`, `[humano+máquina]`) y respetar el estilo:
   - `[humano]`: prosa estructurada, *trade-offs*, riesgos, *roadmap*.
   - `[máquina]`: YAML, tablas semánticas, comandos copiables; cero prosa ambigua.
   - `[humano+máquina]`: narrativa breve + tabla o YAML al final.
3. **Poblar la sección** con datos derivados del MRD/PRD/FSD/ADRs vigentes; cero invención.
4. **Detectar drift**: comparar el contenido propuesto contra `AGENTS.md`. Si la decisión implica cambio para los agentes (nuevo stack, nuevo invariante, nueva ruta crítica), proponer un **diff de `AGENTS.md`** que viaje en el **mismo commit**.
5. **Aplicar checklist de legibilidad IA** antes de cerrar (tablas semánticas, IDs trazables, sin secretos).

## 5. Salida esperada

- Sección del DTI poblada según la plantilla, con tag de audiencia correcto.
- Si la sección modificó decisiones arquitectónicas significativas:
  - Diff propuesto para `AGENTS.md` (sección afectada).
  - Mensaje de commit sugerido: `docs(dti+agents): <decisión> [ADR-NNNN]`.
- Tabla de trazabilidad obligatoria cuando aplique:

| Decisión | ADR fuente | Sección del DTI | Sección de `AGENTS.md` |
|----------|------------|-----------------|------------------------|
| Append-only evidencia | ADR-0001 | §5 / modelo datos | Invariantes de evidencia |
| Monolito modular | ADR-0002 | §3.1 | Stack y capas |

## 6. Verificación (criterios de "bien hecho")

- Frontmatter YAML del DTI válido y completo (parseable sin errores).
- Tag de audiencia consistente con el tipo de contenido de la sección.
- Cada decisión nueva cita su ADR (`[ADR-NNNN]` o enlace a `docs/05_dti/adrs/`); cero decisiones huérfanas.
- `AGENTS.md` y DTI coherentes en el mismo commit cuando la decisión afecta agentes.
- Cero secretos ni PII en el documento.
- Diagramas C4: fuente única en `docs/07_diagramas/`; copias en `docs/05_dti/07_diagramas/` solo como referencia DTI.

## 7. Anti-patrones específicos

- **Decisiones sin ADR**: si va al DTI, va con un ADR; si no tiene ADR, no entra.
- **Drift silencioso**: actualizar el DTI sin tocar `AGENTS.md` cuando la decisión los afecta a ambos.
- **Prosa ambigua en `[máquina]`**: usar YAML o tablas, no párrafos narrativos.
- **Tablas decorativas**: usar tablas solo cuando aportan estructura semántica.
- **Sobre-poblar §0–§21**: si una sección no aplica (p. ej. §6 microservicios en v1.0 monolito), marcarla `N/A` con 1 línea de justificación.
- **Rutas absolutas locales** (`e:/…`): prohibidas; solo rutas relativas al repo.

## 8. Mini ejemplo de invocación

> "@dti-author Pobla §3 (Arquitectura de Alto Nivel) del DTI a partir del ADR-0002 monolito modular.
> Después propón el diff de `AGENTS.md` que corresponda."

## 9. Modos de fallo conocidos

- ADR fuente en estado `proposed`, no `accepted` → STOP; pedir confirmación humana antes de propagar al DTI.
- Campo de frontmatter sin decidir (ej. `release_objetivo`) → `<pendiente>` + `TODO(spec)` en el cuerpo.
- Conflicto entre FSD y un ADR → STOP; escalar; no resolver por cuenta propia.
- Desalineación `docs/adr/` vs `docs/05_dti/adrs/` → sincronizar según [`adrs/README.md`](adrs/README.md) antes de cerrar.

## 10. Registro de cambios del Skill

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 0.1.0 | 13/05/2026 | docente | Versión inicial (`templates/dti-author_template.md`) |
| 0.2.0 | 17/05/2026 | AcredIA | Instalación en `docs/05_dti/`; rutas SIGESA canónicas |
