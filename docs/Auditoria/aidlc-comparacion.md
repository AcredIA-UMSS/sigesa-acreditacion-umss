# Comparación AI-DLC — SIGESA

**Trabajo comparado:** implementar `GET /api/health` (HTTP 200, `{"status":"UP"}`, público, test automatizado, sin tocar el resto de seguridad).  
**Versión A — AI-DLC AWS (perfil Express):** ejecutada el 2026-10-21 (simulada en `.aidlc/` porque el plugin AWS no está instalado).  
**Versión B — AI-SDLC SIGESA:** `FSD → DD → PR-IMPL → código → smoke → @save-prompt-mapping`. **No se volvió a implementar el mismo endpoint** por este camino (el orquestador exige un `FSD-UC-NNN` que no existe para liveness). Los tiempos e intervenciones de B son los del **proceso real del monorepo** aplicado a un cambio de este tamaño.

---

## Respuestas mínimas

### 1. Tiempo total de cada versión

| Versión | Tiempo total | Base |
|---------|--------------|------|
| **A. AI-DLC Express** | **~25 minutos** de reloj (19:39–19:53 UTC−4) | Medido: RE ~8 min, requisitos ~3, código ~5, tests ~7 (incluye reintento Mockito/sandbox), cierre ~2 |
| **B. AI-SDLC SIGESA** | **2–4 horas** (estimado) | No medido en este intent. Un UC chico en el repo suele exigir localizar/crear FSD + DD + PR-IMPL, código hexagonal, tests, a menudo Paso 3c Docker, DTP y PM |

Desglose Express (medido):

| Tramo | Minutos |
|-------|--------:|
| Initialization + Reverse Engineering hasta gate 1 | 8 |
| Requirements Analysis | 3 |
| Code Generation | 5 |
| Build and Test (fallo sandbox + reintento OK) | 7 |
| Cierre de workflow y ficha | 2 |
| **Total A** | **25** |

### 2. Cuántas veces hubo que intervenir o corregir

| Versión | Intervenciones humanas | Correcciones de código |
|---------|------------------------|------------------------|
| **A. Express** | **4** (un `apruebo` por gate: RE, requisitos, código, tests). Más 1 arranque (`/aidlc express`). **Cero “pedir cambios”.** | **0 del grupo.** 1 fallo técnico del agente (Mockito no arranca en sandbox); se corrigió reejecutando Maven fuera del sandbox, sin editar Java. |
| **B. AI-SDLC SIGESA** | **5–7** típicas: confirmar FSD, revisar DD, autorizar PR-IMPL, review de código, mapping PM, a veces ADR si hay divergencia | En un UC real del sprint suele haber 1–3 idas y vueltas (scope, Orval, tests). **No aplica a este health check** porque B no se ejecutó. |

En Express el humano **autorizó**, no reescribió requisitos ni parcheó el controlador.

### 3. Qué artefactos quedaron en el repositorio

**Comunes al producto (salida de A; B no produjo otros):**

- `backend/src/main/java/com/umss/sigesa/adapter/in/web/HealthController.java`
- `backend/src/main/java/com/umss/sigesa/adapter/in/web/dto/HealthResponse.java`
- `backend/src/main/java/com/umss/sigesa/adapter/in/security/SecurityConfig.java` (regla `GET /api/health` `permitAll`)
- `backend/src/test/java/com/umss/sigesa/adapter/in/web/HealthControllerTest.java`

**Solo versión A (AI-DLC):**

- `.aidlc/spaces/sigesa/intents/health-api-express/aidlc-state.md`
- `.aidlc/spaces/sigesa/intents/health-api-express/project-description.json`
- `.aidlc/spaces/sigesa/intents/health-api-express/audit/2026-10-21.md`
- `.aidlc/spaces/sigesa/intents/health-api-express/inception/reverse-engineering/reverse-engineering.md`
- `.aidlc/spaces/sigesa/intents/health-api-express/inception/requirements-analysis/requirements.md`
- `.aidlc/spaces/sigesa/intents/health-api-express/construction/code-generation/code-generation-plan.md`
- `.aidlc/spaces/sigesa/intents/health-api-express/construction/code-generation/code-summary.md`
- `.aidlc/spaces/sigesa/intents/health-api-express/construction/build-and-test/test-results.md`
- `.aidlc/spaces/sigesa/intents/health-api-express/construction/build-and-test/build-and-test-summary.md`
- `.aidlc/spaces/sigesa/intents/health-api-express/workflow-completed.md`
- `docs/Auditoria/aidlc-comparacion.md` (esta ficha)

**Versión B (AI-SDLC) para este intent:** ninguno. No hay `FSD-UC` de health, ni `DD-UC`, ni `PR-IMPL`, ni fila PM. `docs/baseline/` no se tocó (correcto en ambos mundos).

### 4. Si las pruebas pasan en ambas versiones

| Versión | ¿Pasan? | Evidencia |
|---------|---------|-----------|
| **A. Express** | **Sí** | `cd backend && ./mvnw test -Dtest=HealthControllerTest` → Tests run: 3, Failures: 0, BUILD SUCCESS (2026-10-21T19:51:31-04:00). Casos: GET anónimo 200 `UP`; GET `/api/v1/admin/users` sin token 401; POST `/api/health` anónimo 401. |
| **B. AI-SDLC SIGESA** | **No hay suite de este feature en B** | B no implementó un segundo `HealthController`. La suite existente del producto (login, usuarios, etc.) no se reejecutó completa en esta actividad; no se afirma “toda SIGESA en verde”. |

Conclusión honesta: **las pruebas del health check pasan en la única versión que se construyó (A).** B no tiene un segundo binario que comparar.

### 5. Qué salió mejor y qué peor en cada versión

| | **Mejor** | **Peor** |
|--|-----------|----------|
| **A. AI-DLC Express** | Rapidez (~25 min); gates claros; no se inventó un FSD; alcance mínimo (sin dominio ni frontend); test alineado al enunciado | Plugin AWS ausente (flujo simulado); primer `mvn test` falló por sandbox/Mockito; Express omite diseño (riesgo si el path `/api` vs `/api/v1` se hubiera ignorado); no deja DTP/PM |
| **B. AI-SDLC SIGESA** | Trazabilidad FSD→DD→PR-IMPL→PM; hexagonal y baseline congelado; evita implementar un UC de negocio sin contrato | Para un liveness es **sobreceremonial** (2–4 h, documentos que el orquestador exige y aquí no existen); se **bloquearía** al no haber `FSD-UC-NNN` de health |

### 6. ¿Usarían AI-DLC de AWS en el proyecto, y por qué?

**Sí, de forma acotada; no como reemplazo del AI-SDLC de SIGESA.**

- **Sí para** tickets cerrados de infraestructura/operación (health, config, un test de regresión): Express encaja, el grupo solo aprueba gates y el código queda pequeño.
- **No para** casos de uso de acreditación (evidencias, usuarios, dashboards, plantillas): el repo ya gobierna con FSD/DD/PR-IMPL, hexagonal y `docs/baseline/` intocable. AWS Feature/Enterprise duplicaría ceremonia o, peor, Express **saltaría** el DD obligatorio.
- **Condición:** instalar el plugin oficial (hoy no está; `.aidlc` lo armó el asistente). Sin motor AWS no hay `aidlc engine`, ni guards reales, ni worktrees.
- **Política propuesta:** Express (o Bugfix) para ops; `@sigesa-orchestrator FSD-UC-NNN` para producto. No mezclar ambos en el mismo intent.

---

## Detalle de apoyo (Express ejecutado)

### Gates humanos (versión A)

| # | Etapa | Pedido | Resultado |
|---|--------|--------|-----------|
| 0 | Perfil | `/aidlc express` | Hecho |
| 1 | Reverse Engineering | Path `/api/health`, `permitAll` solo GET | `apruebo` |
| 2 | Requirements Analysis | Criterios y archivos | `apruebo` |
| 3 | Code Generation | Java generado | `apruebo` |
| 4 | Build and Test | Maven 3/3 OK | `apruebo` |

### Comando de prueba (versión A)

```bash
cd backend && ./mvnw test -Dtest=HealthControllerTest
# Tests run: 3, Failures: 0 — BUILD SUCCESS (2026-10-21T19:51:31-04:00)
```

---

## Bitácora

| Fecha | Qué cambió |
|-------|------------|
| 2026-10-21 | Express iniciado; gates 1–4; workflow cerrado; health en backend. |
| 2026-10-21 | Ficha reescrita para cubrir las seis respuestas mínimas de la actividad. |
