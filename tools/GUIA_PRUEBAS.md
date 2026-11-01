# Guía de comandos — pruebas SIGESA

Cheat sheet para ejecutar tests **en bloque** o **por parte**. Caminos relativos a la **raíz del repo**.

| Capa | Runner | Dónde viven |
|------|--------|-------------|
| Backend unitario / WebMvc / Spring | Maven (`./mvnw`) | `backend/src/test/java/` |
| Frontend unitario / componente | Vitest (`pnpm test:run`) | `frontend/src/**/*.test.ts(x)` — **no** incluye Playwright |
| E2E UI | Playwright | `frontend/tests/` |
| Contratos JSON | Maven (`com.sigesa.app.contracts`) | `backend/src/test/java/com/sigesa/app/contracts/` |
| Red Team (CI) | Maven parametrizado | `RedTeamAssistantCatalogWebMvcTest` |
| Red Team (LLM vivo) | `tools/red-team-agent/run.sh` | catálogo `attacks.json` |
| Evals asistente | `tools/evals/run.sh` | dataset dorado |
| Generadores (no son la suite) | `tools/e2e-agent`, `tools/contract-test-agent`, `tools/test-generator` | producen specs / JUnit |

**JDK:** 21 (`java -version`). **Frontend:** `pnpm` en `frontend/`. **Backend:** wrapper `backend/mvnw` (no hace falta Maven global).

No uses `pnpm test` en la **raíz** del monorepo: ese `package.json` apunta a otro `playwright.config.ts` (puerto 3000). El E2E de SIGESA es el de `frontend/`.

---

## 1. Backend (Maven)

```bash
cd backend
```

### Global

```bash
./mvnw test                          # Surefire: *Test / *Tests
./mvnw -q test                       # mismo, menos log
./mvnw verify                        # test + JaCoCo report + check
./mvnw test jacoco:report            # tests + HTML en target/site/jacoco/
```

Un test que **no** entra en `./mvnw test` (sufijo `IT`, no `Test`):

```bash
./mvnw test -Dtest=ReportExportAsyncE2EIT
```

### Por paquete / patrón (`-Dtest`)

Surefire acepta glob de **nombre de clase** (no ruta). Varios separados por coma.

```bash
# WebMvc (slices HTTP + SecurityConfig)
./mvnw test -Dtest='*WebMvcTest,*ControllerTest,*ControllerQueryTest'

# Dominio de aplicación por carpeta lógica
./mvnw test -Dtest='com.umss.sigesa.application.service.auth.*'
./mvnw test -Dtest='com.umss.sigesa.application.service.assistant.*'
./mvnw test -Dtest='com.umss.sigesa.application.service.process.*'
./mvnw test -Dtest='com.umss.sigesa.application.service.template.*'
./mvnw test -Dtest='com.umss.sigesa.application.service.evidence.*'
./mvnw test -Dtest='com.umss.sigesa.application.service.workflow.*'
./mvnw test -Dtest='com.umss.sigesa.application.service.report.*'
./mvnw test -Dtest='com.umss.sigesa.application.service.dashboard.*'
./mvnw test -Dtest='com.umss.sigesa.application.service.catalog.*'

# Adaptadores
./mvnw test -Dtest='com.umss.sigesa.adapter.in.web.*'
./mvnw test -Dtest='com.umss.sigesa.adapter.in.security.*'
./mvnw test -Dtest='com.umss.sigesa.adapter.out.*'

# Integración Spring (H2)
./mvnw test -Dtest='com.umss.sigesa.integration_tests.*'

# Contratos JSON (Lic. Marlene)
./mvnw test -Dtest='com.sigesa.app.contracts.**'

# Contexto de arranque
./mvnw test -Dtest=SigesaApplicationTests

# Performance (streaming reportes; no es carga de API)
./mvnw test -Dtest=Dashboard1MPerformanceTest

# Red Team catálogo (MockMvc, LLM mock)
./mvnw test -Dtest=RedTeamAssistantCatalogWebMvcTest
```

### Por clase o método

```bash
./mvnw test -Dtest=AssistantControllerWebMvcTest
./mvnw test -Dtest=AssistantControllerWebMvcTest#shouldRejectUnauthenticated   # 1 método (nombre exacto)
./mvnw test -Dtest=ProcessWorkflowIntegrationTest,UserManagementIntegrationTest
```

WebMvc que cargan `SecurityConfig` (necesitan `RestAccessDeniedHandler` en `@Import`):

```bash
./mvnw test -Dtest=ProgramCatalogControllerTest,UserAdminControllerTest,ProcessResponsibleControllerWebMvcTest,TemplateControllerWebMvcTest,NormativeStructureControllerWebMvcTest,AssistantControllerWebMvcTest,TemplateNormativeStructureControllerWebMvcTest,RedTeamAssistantCatalogWebMvcTest
```

### IntelliJ

Run del archivo o de `backend/src/test/java`. Equivale a `-Dtest=<Clase>`. El WARN de `jsonb` / H2 en integraciones es de dialecto, no de WebMvc.

---

## 2. Frontend unitario (Vitest)

```bash
cd frontend
pnpm install          # una vez
```

### Global

```bash
pnpm test:run         # Vitest, solo `src/**/*.test.*` (no `frontend/tests/`)
pnpm test             # watch
pnpm test:coverage    # coverage HTML (v8)
```

### Por parte

```bash
pnpm exec vitest run src/features/auth
pnpm exec vitest run src/features/processes
pnpm exec vitest run src/features/assistant
pnpm exec vitest run src/lib/auth
pnpm exec vitest run src/components/ui

# Un archivo
pnpm exec vitest run src/features/auth/pages/LoginPage.test.tsx

# Watch de un glob
pnpm exec vitest src/features/processes
```

---

## 3. E2E UI (Playwright — `frontend/tests`)

Playwright levanta Vite `:5173` y, salvo `PW_SKIP_BACKEND=1`, el backend con `./mvnw spring-boot:run` (perfil `dev`) en `:8080`. Si ya tenés Docker/backend + `pnpm dev`, reusa servidores (`reuseExistingServer`).

```bash
cd frontend
pnpm exec playwright install chromium   # una vez (browsers)
```

### Global

```bash
pnpm exec playwright test
# o, si el script del package.json está sano:
pnpm test:e2e
```

### Por carpeta

```bash
pnpm exec playwright test tests/tradicional
pnpm exec playwright test tests/agente
pnpm exec playwright test tests/integration
pnpm exec playwright test tests/search_evidence
```

### Por archivo / título

```bash
pnpm exec playwright test tests/tradicional/login.spec.ts
pnpm exec playwright test tests/agente/ayuda-funciones-usuario-jd.spec.ts
pnpm exec playwright test tests/integration/auth-flow.integration.spec.ts

pnpm exec playwright test -g "login"          # filtra por título de test
```

### Backend ya arriba / modos

```bash
PW_SKIP_BACKEND=1 pnpm exec playwright test tests/agente/ayuda-funciones-usuario-jd.spec.ts

pnpm exec playwright test --headed
pnpm exec playwright test --ui
pnpm exec playwright show-report              # último HTML
npx playwright show-trace test-results/.../trace.zip
```

`seed.spec.ts` es utilidad de datos, no un flujo de producto.

---

## 4. Contratos JSON (agente + Maven)

Detalle: [`contract-test-agent/README.md`](contract-test-agent/README.md).

```bash
cd tools/contract-test-agent
./setup.sh
./run.sh modulos
./run.sh listar assistant          # assistant | evidence | auth
./run.sh correr                    # Surefire solo com.sigesa.app.contracts
```

Equivalente directo:

```bash
cd backend && ./mvnw test -Dtest='com.sigesa.app.contracts.**'
```

---

## 5. Red Team (asistente)

Manual: [`red-team-agent/MANUAL.md`](red-team-agent/MANUAL.md).

```bash
cd tools/red-team-agent
./setup.sh
./run.sh show-config
./run.sh listar
./run.sh sync                      # catalog → backend/src/test/resources/redteam/

# CI (sin LLM real)
cd ../../backend && ./mvnw test -Dtest=RedTeamAssistantCatalogWebMvcTest

# LLM vivo (backend Docker :8080, assistant ON)
cd ../tools/red-team-agent
./run.sh probar-api
./run.sh probar-api --id RT-INJ-001
./run.sh probar-lab --trials 3 --report reports/lab-run.json
```

---

## 6. Evals offline (calidad del asistente)

Manual: [`evals/README.md`](evals/README.md). Compuerta actual: **v3**.

```bash
cd tools/evals
./setup.sh                         # venv; copiar .env.example → .env si hace falta

# Sin recapturar LLM (artefactos ya en repo)
./run.sh gate --prompt-version v3
./run.sh judge-gate                # κ ≥ 0.6 sobre judge.jsonl existente

./run.sh listar
./run.sh eval --prompt-version v3
./run.sh compare --from v2 --to v3

# Recaptura (API + prompt de esa versión)
docker compose -f ../../docker-compose.yml up -d backend
./run.sh capture --prompt-version v3
./run.sh eval --prompt-version v3 && ./run.sh gate --prompt-version v3
./run.sh judge-run --prompt-version v3 && ./run.sh judge-calibrate && ./run.sh judge-gate
```

---

## 7. Generadores (escriben tests; no sustituyen la suite)

```bash
# Playwright specs vía LLM local
cd tools/e2e-agent
./setup.sh && ./run.sh probar
./run.sh plan ayuda
./run.sh generar ayuda 1.2
# luego: cd ../../frontend && PW_SKIP_BACKEND=1 pnpm exec playwright test tests/agente/<archivo>.spec.ts

# JUnit vía LLM
./tools/test-generator/run.sh --list-batches
./tools/test-generator/run.sh --batch L1 --dry-run
```

---

## 8. Recetas frecuentes

**Solo lo que tocaste en un PR de seguridad WebMvc**

```bash
cd backend && ./mvnw test -Dtest='*WebMvcTest,*ControllerTest'
```

**Frontend feature processes**

```bash
cd frontend && pnpm exec vitest run src/features/processes
```

**Humo UI login (backend ya en Docker)**

```bash
cd frontend && PW_SKIP_BACKEND=1 pnpm exec playwright test tests/tradicional/login.spec.ts
```

**Asistente: unitario + HTTP + catálogo RT**

```bash
cd backend && ./mvnw test -Dtest='com.umss.sigesa.application.service.assistant.*,AssistantController*,RedTeamAssistantCatalogWebMvcTest'
```

**Todo lo “rápido” sin Playwright ni evals LLM**

```bash
cd backend && ./mvnw -q test
cd ../frontend && pnpm test:run
```
