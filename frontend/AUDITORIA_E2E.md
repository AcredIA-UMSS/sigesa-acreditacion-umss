# Auditoría E2E — 5 preguntas (SIGESA)

Antes de aceptar un test generado por el agente, respondé **sí** a las cinco. Si alguna es **no**, corregí o renombrá a `*.auditado.spec.ts` tras arreglar.

1. **¿Los localizadores sobreviven un cambio de CSS?**  
   Solo `getByRole`, `getByLabel`, `getByText` (estable) o `getByTestId` (ancla explícita). Prohibido: `#id`, `.clase`, `nth-child`, XPath frágil.

2. **¿Hay esperas fijas?**  
   Prohibido `waitForTimeout`. Usar `expect(...).toBeVisible()` / `toHaveURL()` / `toHaveCount()` (Playwright espera hasta el timeout por defecto).

3. **¿El test verifica comportamiento y no copy volátil?**  
   No asserts sobre redacción exacta de respuestas del asistente LLM, toasts genéricos del servidor ni textos que cambien con i18n. Verificá: URL, rol visible, filas en tabla, `data-testid`, estados (`role="alert"`, conteos).

4. **¿Es independiente?**  
   Un solo `test()` por archivo generado; empieza con `page.goto(...)`; no depende del orden de otros tests ni de datos creados en otro spec (usá seed del backend dev o helpers locales).

5. **¿Pasó solo en Chromium?**  
   `pnpm test:e2e tests/agente/<archivo>.spec.ts` en verde, sin skips ni `test.fixme` sin ticket.

## Flujo recomendado

```bash
# 1. Generar (un caso)
# 2. Correr
pnpm test:e2e tests/agente/caso_1_1.spec.ts
# 3. Auditar (este doc)
# 4. Renombrar o comentar AUDITORIA en el archivo
```

Referencia de patrón: `tests/tradicional/login.spec.ts`.
