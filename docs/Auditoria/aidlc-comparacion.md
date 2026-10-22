# Comparación AI-DLC — SIGESA

**Intent de referencia:** `GET /api/health` (HTTP 200, `{"status":"UP"}`, público).  
**Perfil AI-DLC:** Express (7 etapas post-init; profundidad Minimal).  
**Fecha de inicio:** 2026-10-21.  
**Estado:** workflow Express **cerrado** (2026-10-21). `HealthControllerTest` 3/3 OK.

Este archivo compara el flujo **AI-DLC Express** (simulado en `.aidlc/` porque el plugin AWS no está instalado) con el flujo nativo del repo **AI-SDLC SIGESA** (`FSD → DD → PR-IMPL → código → smoke → PM`).

---

## 1. Tiempos

Reloj de pared de esta sesión Express (UTC−4).

| Tramo | AI-DLC Express (este intent) | AI-SDLC SIGESA (flujo habitual del monorepo) |
|-------|------------------------------|-----------------------------------------------|
| Arranque / resolución de trazabilidad | ~2 min: perfil Express + scan brownfield | 10–20 min: localizar `FSD-UC-NNN`, `DD-UC`, `PR-IMPL` |
| Diseño / requisitos | ~3 min (`requirements-analysis`) | Obligatorio `DD-UC-*.md` **antes** de código |
| Implementación | ~5 min (`code-generation`) | Backend + (si aplica) Orval + frontend |
| Pruebas | ~7 min (3 tests, reintento sandbox) | JUnit/JaCoCo + lint + a menudo Paso 3c Docker |
| Cierre documental | Esta ficha + `.aidlc` | `@dtp-sync` + `@save-prompt-mapping` (PM-NNN) |
| **Total medido** | **~25 min** de reloj + 4 gates humanos | **2–4 h** si se fuerza FSD/DD/PR-IMPL para un health check |

**Desglose:** RE ~8 min; requisitos ~3 min; código ~5 min; tests ~7 min; cierre ~2 min.  
**Intervenciones:** 4 de 4 aprobadas (`apruebo` en cada gate).

---

## 2. Intervenciones humanas

| # | Punto de control | Qué se pide | Estado |
|---|------------------|-------------|--------|
| 0 | Confirmación de perfil Express | El usuario ya eligió `/aidlc express` | Hecho |
| 1 | Reverse Engineering | Aprobar hallazgo: path `/api/health`, `permitAll` solo GET, sin dominio | **Aprobado** (2026-10-21) |
| 2 | Requirements Analysis | Aprobar criterios de aceptación y archivos a tocar | **Aprobado** |
| 3 | Code Generation | Aprobar Java generado (controlador, DTO, SecurityConfig, test) | **Aprobado** |
| 4 | Build and Test | Aprobar comando Maven y resultado 3/3 OK | **Aprobado** |

En AI-SDLC SIGESA las intervenciones típicas son: confirmar FSD, revisar DD, autorizar implementación, review de código, mapping PM. Para un health check eso es **sobreceremonial**.

---

## 3. Artefactos

| Artefacto | AI-DLC Express | AI-SDLC SIGESA |
|-----------|----------------|----------------|
| Estado del workflow | `.aidlc/.../aidlc-state.md` | Checklist del orquestador en el chat + `PROMPT_MAPPING.md` |
| Auditoría de eventos | `.aidlc/.../audit/` | Entrada `PM-NNN` append-only |
| Ingeniería inversa | `inception/reverse-engineering/reverse-engineering.md` | Implícita en el agente; no hay artefacto estándar |
| Requisitos | `inception/requirements-analysis/requirements.md` | `docs/product/uc/FSD-UC-*.md` |
| Diseño | **Omitido** (Express) | `docs/design/DD-UC-*.md` **obligatorio** antes de código |
| Contrato de implementación | Plan corto en Construction | `docs/prompts/impl/PR-IMPL-*.md` |
| Código | `HealthController` + `HealthResponse` + `SecurityConfig` + `HealthControllerTest` | Hexagonal completo si el UC lo exige |
| Baseline | No se toca | **Prohibido** tocar `docs/baseline/` |

---

## 4. Pruebas (estrategia)

| Criterio | Express (Minimal) | AI-SDLC SIGESA |
|----------|-------------------|----------------|
| Qué se prueba | Solo lo pedido: 200 + JSON; GET público; resto de reglas intactas (401 en un path protegido) | Cobertura unitaria > 90 % (JaCoCo), tests de dominio y a menudo E2E |
| Cómo | `WebMvcTest` + `SecurityConfig` (MockMvc) | JUnit por capa + smoke Docker |
| Estado | 3/3 OK (`./mvnw test -Dtest=HealthControllerTest`) | N/A en este intent |

Comando ejecutado:

```bash
cd backend && ./mvnw test -Dtest=HealthControllerTest
# Tests run: 3, Failures: 0 — BUILD SUCCESS (2026-10-21T19:51:31-04:00)
```

---

## 5. Ventajas y desventajas

### AI-DLC Express

**Ventajas**

- Camino corto requisitos → código → test, adecuado cuando el enunciado ya está cerrado.
- Gates explícitos: no se escribe código de producto sin aprobación.
- Trazabilidad ligera (state + audit) sin inventar un FSD.
- Profundidad Minimal evita diseño hexagonal innecesario para un liveness.

**Desventajas**

- El plugin oficial no está en este repo: la ejecución es **simulada**.
- Omite diseño; un error de path (`/api` vs `/api/v1`) se detecta tarde si se salta Reverse Engineering.
- No cubre DTP, Orval ni frontend (correcto aquí, peligroso si se usa Express para un UC de negocio).
- Guard Policy *off* por defecto; aquí se forzó aprobación humana porque el usuario lo pidió.

### AI-SDLC SIGESA

**Ventajas**

- Trazabilidad FSD → DD → PR-IMPL → PM alineada a acreditación y al Tech Lead.
- Hexagonal, DTOs, tests de dominio y gobernanza de `docs/baseline/`.
- Orquestador evita implementar un UC sin contrato.

**Desventajas**

- Ceremonia alta para un endpoint de 10 líneas.
- Bloqueo si no existe `FSD-UC-NNN` (el orquestador debe detenerse).
- Tiempo y artefactos desproporcionados frente al riesgo del cambio.

---

## 6. Conclusión

Para **GET /api/health**, AI-DLC Express es el perfil correcto: requisito cerrado, cambio localizado (adaptador + security + un test), sin modelo de dominio. El AI-SDLC del monorepo sigue siendo el camino adecuado para casos de uso de acreditación (evidencias, usuarios, dashboards), donde un DD y un PR-IMPL evitan divergencia silenciosa.

No conviene mezclar los dos en el mismo intent: no se creará `FSD-UC` ni se modificará `docs/baseline/` para este health check. Sí se respetará la regla de alcance mínimo (solo archivos necesarios) y la aprobación por gate.

**Veredicto:** Express para operación/liveness; AI-SDLC SIGESA para producto. En este intent, 4 gates y ~25 min bastaron para un endpoint público verificado. No se creó FSD/DD/PR-IMPL ni se tocó `docs/baseline/`.

---

## Bitácora de actualización

| Fecha | Qué cambió |
|-------|------------|
| 2026-10-21 | Alta del archivo. Express iniciado. Gate 1 (Reverse Engineering) abierto. |
| 2026-10-21 | Gate 1 aprobado. Gate 2 (Requirements Analysis) abierto. |
| 2026-10-21 | Gate 2 aprobado. Código generado. Gate 3 abierto (tests no ejecutados). |
| 2026-10-21 | Gate 3 aprobado. Maven BUILD SUCCESS (3 tests). Gate 4 abierto. |
| 2026-10-21 | Gate 4 aprobado. Workflow Express cerrado. |
