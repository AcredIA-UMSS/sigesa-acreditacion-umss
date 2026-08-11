---
name: sigesa-orchestrator
description: >-
  DEPRECADO — Reemplazado por el subagente sigesa-orchestrator en
  .cursor/agents/sigesa-orchestrator.md. Usar solo si el subagente no está
  disponible; en ese caso leer ese archivo y seguir su pipeline completo
  (incluye Paso 3c Docker y @save-prompt-mapping obligatorio).
disable-model-invocation: true
---

# Skill deprecado — usar subagente

Este skill fue **reemplazado** por el subagente de proyecto:

**[`.cursor/agents/sigesa-orchestrator.md`](../../agents/sigesa-orchestrator.md)** ← fuente de verdad

## Cómo invocarlo

**Siempre partir del FSD** (el orquestador resuelve DD-UC y PR-IMPL):

```text
@sigesa-orchestrator FSD-UC-022

@sigesa-orchestrator fsd=FSD-UC-022 sprint=2 solicitante="Tu Nombre" run all steps

Use the sigesa-orchestrator agent to implement FSD-UC-021 end-to-end.
```

Si `@sigesa-orchestrator` no resuelve al subagente, **lee y ejecuta directamente** `.cursor/agents/sigesa-orchestrator.md`.

---

## Pipeline resumido (sincronizado con el subagente)

| Paso | Artefacto | Skill / acción |
|------|-----------|----------------|
| 0 | FSD-UC | `docs/product/uc/FSD-UC-NNN.md` |
| 1 | DD-UC | `@feature-design-doc` → `.cursor/skills/feature-design-doc/SKILL.md` |
| 2 | PR-IMPL | `@sigesa-prompt-contract-architect` |
| 3a | Backend | `./mvnw test` · reglas persistencia dev Docker |
| 3b | Frontend + Orval | `@generate-frontend-feature` · `pnpm run generate:api` |
| **3c** | **Validación Docker** | Rebuild compose · smoke E2E `:3000` / `:8080` — ver subagente §Paso 3c |
| 4 | Code review | `@sigesa-code-reviewer-sigesa` |
| 5 | Cierre | **`@dtp-sync`** + **`@save-prompt-mapping`** (obligatorio) |

Estados del checklist: ⬜ pendiente · 🔄 en curso · ✅ completo · 🚫 bloqueado

---

## Paso 3c — Validación Docker (referencia rápida)

Detalle completo en el subagente. Obligatorio si el usuario usa `docker-compose.yml`.

```bash
docker compose up -d --build          # tras cambios back/front
curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/v3/api-docs  # → 200
```

- `application-dev.yaml`: `ddl-auto: update` + `flyway.enabled: false`
- `frontend/.env`: `VITE_API_URL=` vacío (proxy nginx `:3000`)
- Smoke: login JD → ruta del feature → `/api/v1/**` con `Authorization: Bearer …`
- BD corrupta: `docker compose down -v && docker compose up -d --build`

**Gate:** no pasar al Paso 4 sin smoke test OK.

---

## Paso 5 — Cierre obligatorio (`@save-prompt-mapping`)

**No dar por cerrada ninguna implementación** (`PR-IMPL-NNN`) sin registrar auditoría en el sprint.

1. **`@dtp-sync`** — si hubo cambios en DTOs, modelo, dependencias o contratos API  
   → `.cursor/skills/dtp-sync/SKILL.md`

2. **`@save-prompt-mapping`** — **siempre**, tras código o cierre documental  
   → `.cursor/skills/save-prompt-mapping/SKILL.md`

   ```text
   @save-prompt-mapping sprint=<N> fsd=FSD-UC-NNN solicitante="<nombre>" [estado=completado]
   ```

   (Resolver `pr=` internamente desde el FSD; ver subagente §Resolución de trazabilidad.)

   - Destino: `docs/sprints/sprint_<N>/PROMPT_MAPPING.md` (entrada `PM-NNN`, append-only)
   - Incluir: prompt exacto, archivos verificados vía `git status`, tests ejecutados
   - Si falta PM del PR-IMPL, **detener** — no marcar el feature como completo

3. Informar al usuario: ID `PM-NNN`, ruta del sprint, archivos tocados, resultado smoke/tests.

**Gate:** checklist Paso 5 = ✅ solo cuando existen entradas PM y (si aplica) DTP actualizado.

---

## Delegación (tabla completa en subagente)

| Cierre | Skill |
|--------|-------|
| DTP vivo | `dtp-sync` |
| Auditoría sprint | **`save-prompt-mapping`** |
| UI | `generate-frontend-feature` |
| Review | `sigesa-code-reviewer-sigesa` |
