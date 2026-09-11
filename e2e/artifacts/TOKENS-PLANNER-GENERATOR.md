# Registro de tokens — Playwright Planner y Generator

**Proyecto:** SIGESA · gestión de evidencias (`FSD-UC-004`)  
**Fecha:** 2026-09-11  
**Suite:** `PLAYWRIGHT_HTML_OPEN=never npx playwright test` (cwd `frontend/`) → **15 passed**  
**Nota:** Cursor no expone usage LLM de la sesión (no hay API de tokens). Este registro mide **artefactos persistidos** con heurística `tokens ≈ bytes UTF-8 / 4`. Los system prompts son los de Playwright 1.63 (`playwright-test-planner` / `playwright-test-generator`, modelo declarado en el agent: `sonnet`; ejecución en Cursor Grok 4.6).

## Resumen

| Agente | Rol | Entrada (system + prompts de agente) | Salida persistida | Total estimado |
| --- | --- | ---: | ---: | ---: |
| **Planner** | Plan + auditoría P01–P15 | 3 004 B ≈ **751** | 11 912 B ≈ **2 978** | **≈ 3 729** |
| **Generator** | 5 specs aceptados/corregido + locators | 3 511 B ≈ **878** | 8 217 B ≈ **2 054** | **≈ 2 932** |
| **Ambos** | | **≈ 1 629** | **≈ 5 032** | **≈ 6 661** |

La exploración del producto (FSD, rutas, snapshots) **no está incluida**: Cursor no la factura en este archivo. El número de arriba es el **piso medible** (prompts de agente + entregables), no el usage completo de la sesión.

## Planner (`playwright-test-planner`)

**Sesión:** 2026-09-11 15:23 (UTC-4)  
**Alcance:** iniciar sesión [CC] y llegar a `/evidencias/cargar`.  
**Resultado de auditoría:** 15 propuestos → 4 aceptados, 1 corregido (P05 → fuente/estado/ticket), 10 descartados.

### Entrada (system)

| Artefacto | Bytes | Tokens est. |
| --- | ---: | ---: |
| `playwright-test-planner.agent.md` | 2 808 | 702 |
| `playwright-test-plan.prompt.md` | 196 | 49 |
| **Subtotal** | **3 004** | **751** |

### Salida persistida

| Artefacto | Bytes | Tokens est. |
| --- | ---: | ---: |
| `e2e/specs/gestion-evidencias.plan.md` | 3 821 | 955 |
| `e2e/specs/gestion-evidencias.audit.md` | 3 500 | 875 |
| `e2e/seed.cc-evidencias.spec.ts` | 317 | 79 |
| Canvas `gestion-evidencias-planner-audit.canvas.tsx` | 4 274 | 1 069 |
| **Subtotal** | **11 912** | **2 978** |

## Generator (`playwright-test-generator`)

**Sesión:** 2026-09-11 15:31 (UTC-4)  
**Alcance:** un spec por caso aceptado + P05 corregido; 5 preguntas E2E (persona locator, sin espera fija, UI no copy LLM, independiente con `page.goto`, datos propios).  
**Lenguaje de tests:** TypeScript; aserciones sobre copy **español** de la UI (`Correo Institucional`, `Cargar evidencia`, `Subir evidencia`).

### Entrada (system)

| Artefacto | Bytes | Tokens est. |
| --- | ---: | ---: |
| `playwright-test-generator.agent.md` | 3 336 | 834 |
| `playwright-test-generate.prompt.md` | 175 | 44 |
| **Subtotal** | **3 511** | **878** |

El plan (`gestion-evidencias.plan.md`, 3 821 B ≈ 955 tokens) es **contexto de entrada adicional** del Generator (no duplicado en el total del Planner). Con plan incluido: Generator entrada ≈ **1 833** tokens.

### Salida persistida

| Artefacto | Caso | Bytes | Tokens est. |
| --- | --- | ---: | ---: |
| `e2e/evidence/cc-login-sidebar-cargar-evidencia.spec.ts` | 1.1 | 1 474 | 369 |
| `e2e/evidence/cc-direct-url-evidencias-cargar.spec.ts` | 1.2 | 1 104 | 276 |
| `e2e/evidence/unauthenticated-redirect-evidencias.spec.ts` | 1.3 | 681 | 170 |
| `e2e/evidence/jd-blocked-from-evidence-module.spec.ts` | 2.1 | 1 137 | 284 |
| `e2e/evidence/uploadable-indicators-show-state-and-source.spec.ts` | 3.1 | 1 823 | 456 |
| `e2e/specs/locators-evidencias.md` | anclas | 1 998 | 500 |
| **Subtotal** | | **8 217** | **2 054** |

## Evidencia de corrida verde

| Ítem | Ruta |
| --- | --- |
| Log | `e2e/artifacts/last-run.txt` |
| HTML | `e2e/artifacts/html-report/index.html` |
| Trazas (15) | `e2e/artifacts/test-results/**/trace.zip` |

HTML y traces están en `.gitignore` (`e2e/artifacts/html-report/`, `e2e/artifacts/test-results/`) por tamaño; quedan en disco para la entrega local.
