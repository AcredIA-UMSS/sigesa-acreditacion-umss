# Práctica: AI-DLC de AWS con y sin herramienta

**Autor:** Boris Anthony Angulo Urquieta  
**Fecha:** 2026-10-20  
**Proyecto:** SIGESA — acreditación UMSS

> Copien este archivo en su repositorio como `docs/practicas/aidlc-comparacion.md` y complétenlo. Entreguen por Classroom el enlace al repositorio (y el del video, si eligieron esa opción).

## 1. Datos

- **Modalidad:** individual
- **Estudiante:** Boris Anthony Angulo Urquieta
- **Repositorio:** [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss)
- **Rama sin herramienta (A):** `feature-sinaidlc-boris` (commit `4c6390d`)
- **Rama con herramienta (B):** `feature-aidlc-boris` (working tree; intent `261020-process-stats`)
- **Video (opcional):** no
- **Herramienta B:** AI-DLC de AWS **2.11.0** (`aidlc 2.11.0 (runtime 2.11.0)`), harness Cursor, perfil **Express** (`/aidlc express`)
- **Pin de versión:** no se generó un archivo raíz `.aidlc-version` (instalación nativa). La versión queda en `.cursor/tools/data/aidlc-manifest.json` y `aidlc-stamp.json` (`frameworkVersion: 2.11.0`) y en `aidlc version`.

**Responsables** (obligatorio si es en grupo; cada aporte debe verse en los commits con la identidad de su autor):

| Integrante | Qué hizo | Commits o PR |
|---|---|---|
| Boris Anthony Angulo Urquieta | Diseño del feature; versión A (flujo SIGESA/Cursor, sin AWS); instalación y flujo Express 2.11.0; revisión de puertas (Reverse Engineering, Requirements, Code Generation, Build and Test, Operation); informe de comparación | A: `feature-sinaidlc-boris` (`4c6390d`). B: `feature-aidlc-boris` |

## 2. El feature

**`GET /api/v1/processes/stats`** — resumen operativo de procesos visibles al actor autenticado (extensión de FSD-UC-019). No lista filas: agrega conteos.

- **Entrada:** JWT. Sin query ni body.
- **Salida 200:** `{ "total", "byStatus", "byOperationalMode", "frozenCount" }` con `frozenCount` = procesos en `operationalMode = AUDIT_READONLY`.
- **Errores:** `401` sin token (`UNAUTHORIZED`); `403` rol no autorizado (`ACCESS_DENIED`). Scope de carrera vacío (CC/EE/CAE) → **200 con ceros**, no 404.
- **PBAC:** JD/TD todos los no archivados; CC/EE/CAE_MEMBER solo su carrera (asignaciones activas en BD, igual que el listado).
- **Aceptación:** tests Maven del caso de uso y WebMvc; ruta literal `/stats` **antes** de `/{processId}`; hexágono (puerto/adaptador, sin entidad JPA en el controller); sin frontend; sin `docs/baseline/`.

Diferencia de contrato A vs B: en A los mapas solo tenían claves con conteo > 0. En B (pregunta 2 de Requirements) las claves son **siempre** `ACTIVE`/`CLOSED` y los tres modos, en 0 si faltan.

## 3. Versión A: sin la herramienta

- **Cómo:** Cursor + flujo del curso (AI-SDLC SIGESA). Baseline `origin/release/2.0.0`. DD-UC-019 ya existía. Se escribió PR-IMPL-046, se implementó hexágono reutilizando `ProcessQueryPort` (se **extendió** `ProcessListItem` con `operationalMode`), se actualizó DTP, FSD-UC-019, `api_contracts.md` (API-PROC-03b) y PM-028.
- **Resultado:** rama `feature-sinaidlc-boris`, commit `4c6390d`.
- **Tiempo:** ~**15 min** (17:16–17:23 diseño del contrato; 17:23–17:27 código + Maven).
- **Pruebas:** `GetProcessStatsServiceTest`, `ProcessControllerQueryTest`, `ProcessStatsControllerWebMvcTest`, `ListProcessesServiceTest` — **pasan** (2026-10-20 ~17:27).
- **Intervenciones:** 1 de alcance **antes** de codear (`GET /api/v1/system/info` → `processes/stats`). **0** fallos de Maven. **0** Request Changes (no hay puertas).

## 4. Versión B: con AI-DLC de AWS 2.11.0 (perfil Express)

- **Comando:** `/aidlc express GET /api/v1/processes/stats` (+ contrato hexagonal y PBAC).
- **Salida de `aidlc version`:** `aidlc 2.11.0 (runtime 2.11.0)`.
- **Doctor:** 0 problemas (avisos de PATH/update/uncommitted `aidlc/` ignorados).
- **Intent:** `aidlc/spaces/default/intents/261020-process-stats/`
- **Inicio:** 2026-10-20 **17:46** (UTC-4) / `2026-10-20T21:46:40Z`
- **Cierre:** 2026-10-20 **19:10** (UTC-4) / state `Last Updated 2026-10-20T23:09:53Z`
- **Tiempo:** ~**1 h 24 min** de Express (más ~5 min de `install.sh` + `aidlc config --harness cursor` a las 17:42).
- **Etapas ejecutadas:** init (3) + Reverse Engineering, Requirements Analysis, Code Generation, Build and Test, Deployment Pipeline, Deployment Execution, Observability Setup. Ideation omitida. Plan approval / sensores / learnings / summary confirmation **off** (defaults de Express).
- **Puertas revisadas:**

| Puerta | Qué se hizo | Request Changes |
|---|---|---|
| Reverse Engineering | Inventario correcto: Java 21, `ProcessController`, PBAC, ausencia de `/stats`, `ProcessListItem` sin modo | No. Aprobado tras leer `developer-scan.md` |
| Requirements Analysis | FR1–FR3, NFR, hexágono; preguntas de alcance (BD vs JWT) y forma de mapas | No. Aprobado tras leer `requirements.md` |
| Code Generation | Plan hexagonal: `ProcessStatsRow` **nuevo** (no se toca el ítem del listado); `/stats` antes del UUID | Primera aprobación. Luego **loop-back 1** (no hubo Request Changes humano): faltaba test 403 (`FR3.2`). Se añadió `stats_withRoleOutsideAllowedSetReturns403AccessDenied`. Código de producción no cambió. Segunda aprobación. |
| Build and Test | `./mvnw test`: **560** pruebas, 0 fallos, 3 omitidas (preexistentes) | No |
| Deployment Pipeline / Execution | Compose local; humo 401 y 200 con `jd@umss.edu.bo` | No (preguntas auto-resueltas) |
| Observability | Sin SLO numérico; logs del contenedor | No. Preguntas 1 y 2 → A |

- **Artefactos versionables en B:** árbol `aidlc/` (state, auditoría, CodeKB, `requirements.md`, `code-generation-plan.md`, `test-results.md`, humo, observabilidad); runtime Cursor bajo `.cursor/` (skills/agents/hooks AIDLC); código y tests del endpoint. **No** hay `.aidlc-version` en la raíz. **No** se actualizaron DTP/FSD/PR-IMPL (Express no usa el AI-SDLC del curso).
- **Pruebas:** Maven verde (suite completa). Humo HTTP contra `localhost:8080` 401/200 documentado en `smoke-test-results.md`.

## 5. Comparación

| Aspecto | A · sin herramienta | B · con AI-DLC de AWS 2.11.0 Express |
|---|---|---|
| Tiempo total | ~15 min (17:16–17:27) | ~1 h 24 min (17:46–19:10) + ~5 min de instalación |
| Veces que tuvieron que intervenir o corregir | 1 (cambio de feature). 0 fallos de test. 0 puertas | **0 Request Changes** formales. **1 corrección** (loop-back: test 403). **4 respuestas** de pregunta (2 Requirements, 2 Observability). 8 `GATE_APPROVED` en auditoría |
| Artefactos que quedaron en el repositorio | Código + tests; `PR-IMPL-046`; API-PROC-03b; DTP; FSD-UC-019; PM-028; este informe. Sin `aidlc/` | Código + tests (incl. `@DataJpaTest` y 403); `aidlc/spaces/.../261020-process-stats/` (requisitos, plan, tests, auditoría, humo); harness `.cursor/` AIDLC; stamp `2.11.0`. Sin PR-IMPL/DTP. Sin `.aidlc-version` raíz |
| ¿Las pruebas pasan? (ejecutadas por ustedes) | Sí (Maven acotado, ~17:27) | Sí (Maven 560/0; humo 401/200, ~19:10) |
| Qué salió mejor | Velocidad; alineación inmediata con docs vivos del curso; un solo pase | Trazabilidad (`audit/`, `requirements.md`, loop-back escrito); contrato más cerrado (claves fijas); test 403 y test JPA; respetó hexágono **sin** que el humano rechazara código; humo real contra Docker |
| Qué salió peor | Decisiones en el chat, no en un log de puertas; no hubo test 403 ni `@DataJpaTest`; mapas “sparse” | ~6× más tiempo; etapas Operation (pipeline/obs) de poco valor para un GET; detector inicial dijo “TypeScript/pnpm”; no sincronizó FSD/DTP/PR-IMPL; más archivos de harness que de feature; riesgo de aprobar Operation por inercia |

## 6. Conclusiones

1. **¿Qué decisiones tomó el agente sin que ustedes las vieran?**  
   **A:** extender `ProcessListItem`; `TreeMap` solo con claves presentes; DTO aparte; `/stats` primero. El humano no revirtió ninguna.  
   **B:** *no* tocar `ProcessListItem` (nuevo `ProcessStatsRow` + query); claves siempre presentes; plan approval off → construyó sin mostrar el plan como pregunta; test-after; Operation sobre Compose local; loop-back del 403 lo diagnosticó Build and Test, no el revisor humano.

2. **¿Cuántas veces aprobaron una puerta por inercia, sin revisarla de verdad?**  
   **A:** no aplica.  
   **B:** Reverse Engineering y Requirements se leyeron (scan + FR). Code Generation se aprobó y el hueco del 403 lo cazó la etapa siguiente, no la lectura del plan. Deployment/Observability se aprobaron con preguntas mínimas (A/A): ahí sí hay **inercia** — el feature ya estaba verde en Maven.

3. **¿Qué evidencia permite a otra persona entender por qué se hizo así?**  
   **A:** commit `4c6390d` + PR-IMPL-046 + contratos vivos. El “por qué” fino quedó en el chat.  
   **B:** la **auditoría AI-DLC** (`audit/boris-computer-*.md`) + `requirements.md` + `code-generation-plan.md` + `test-results.md` (loop-back 1) explican alcance, puertas y el arreglo del 403 mejor que un PR suelto. El historial Git de B aún no está commiteado.

4. **¿Para qué tamaño de cambio vale la pena AI-DLC de AWS y para cuál no?**  
   **No** para un endpoint de 15 min con DD-UC ya escrito: Express mete Operation y ~1,5 h de puertas. **Sí** cuando no hay FSD/DD, el equipo no comparte hexágono, o hace falta un rastro de aprobación (auditoría/Classroom). Un módulo **nuevo** o un cambio que toque seguridad/despliegue real aprovecha más las puertas 2.1 → 3.6 que este GET.

5. **¿Lo usarían en su proyecto? ¿Por qué?**  
   **Boris Anthony Angulo Urquieta — no como flujo principal de SIGESA.** El repo ya tiene AI-SDLC (FSD → DD-UC → PR-IMPL → hexágono → mapping). Express **duplica** gobernanza, no escribe los docs vivos del curso, y para este tamaño el coste (1 h 24 min vs 15 min) no se justifica. Lo que sí se llevaría: el hábito de **puerta + artefacto** (requirements y test-results con loop-back) y el test 403. Se usaría AWS AI-DLC en un spike o en un servicio **sin** FSD, no en cada historia de `release/2.0.0`.

## 7. Antes de entregar

- [x] Las dos ramas existen en local y las pruebas Maven pasan en ambas.
- [ ] Push de A y B al remoto / PR (pendiente de quien entregue Classroom).
- [x] `aidlc/` del intent está en la rama B (working tree).
- [ ] `.aidlc-version` en la raíz: **no lo emite** esta instalación nativa 2.11.0; queda el stamp `frameworkVersion: 2.11.0`.
- [x] No hay secretos, `.env` ni `*.local.json` en los artefactos del feature.
- [x] Tabla de responsables: Boris Anthony Angulo Urquieta.

## 8. Línea de tiempo (20/10/2026, UTC-4)

| Hora | Qué |
|---|---|
| 17:16–17:27 | **A:** diseño + implementación + Maven OK |
| 17:39–17:42 | Install `v2.11.0` + `aidlc config --harness cursor` + doctor |
| 17:46 | **B:** `/aidlc express` — Reverse Engineering |
| ~18:01 | Scan aprobado |
| ~18:11 | Requirements aprobado (preguntas 1–2) |
| ~18:37 | Loop-back Code Generation / B&T (test 403) |
| 19:10 | Workflow complete (Operation / observability) |
