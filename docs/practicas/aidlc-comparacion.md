# Práctica: AI-DLC de AWS con y sin herramienta

> Copien este archivo en su repositorio como `docs/practicas/aidlc-comparacion.md` y complétenlo. Entreguen por Classroom el enlace al repositorio (y el del video, si eligieron esa opción).

## 1. Datos

- **Modalidad:** individual
- **Repositorio:** [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss)
- **Rama sin herramienta (A):** `feature-sinaidlc-boris` (local; aún sin push)
- **Rama con herramienta (B):** `feature-con-aidlc` — **pendiente** (no creada)
- **Video (opcional):** no

**Responsables** (obligatorio si es en grupo; cada aporte debe verse en los commits con la identidad de su autor):

| Integrante | Qué hizo | Commits o PR |
|---|---|---|
| Boris Anthony Angulo Urquieta | Versión A: `GET /api/v1/processes/stats` con flujo SIGESA (hexágono, PR-IMPL-046, tests, docs vivos). Informe de comparación (datos de A; B pendiente). | Rama `feature-sinaidlc-boris` (working tree; commit pendiente) |

## 2. El feature

**`GET /api/v1/processes/stats`** (API-PROC-03b, extensión de FSD-UC-019 / DD-UC-019).

Resumen operativo de procesos visibles al actor autenticado: no lista filas, agrega conteos. Reutiliza el mismo alcance PBAC que `GET /api/v1/processes`.

- **Entrada:** JWT. Sin query ni body.
- **Salida 200:** `{ "total": number, "byStatus": { STATUS: n }, "byOperationalMode": { MODE: n }, "frozenCount": n }` donde `frozenCount` = procesos con `operationalMode = AUDIT_READONLY`.
- **Errores:** `401` sin token; `403` rol no autorizado. Scope de carrera vacío (CC/EE/CAE) → ceros, no 403.
- **PBAC:** JD/TD ven todos; CC/EE/CAE_MEMBER solo procesos de su carrera.
- **Aceptación:** tests de servicio (agregación, scope vacío, modo nulo → ACTIVE) + WebMvc 200/401; ruta literal `/stats` declarada **antes** de `/{processId}`.
- **Contrato:** [`docs/product/api_contracts.md`](../product/api_contracts.md) (API-PROC-03b). Prompt: [`docs/prompts/impl/PR-IMPL-046.md`](../prompts/impl/PR-IMPL-046.md).

## 3. Versión A: sin la herramienta

- **Cómo trabajaron:** flujo actual del curso/SIGESA en Cursor (agente), **sin** AI-DLC de AWS. Baseline `origin/release/2.0.0`. Se reutilizó DD-UC-019 (ya existía); se escribió PR-IMPL-046; implementación hexagonal (`GetProcessStatsUseCase` → `GetProcessStatsService` → `ProcessQueryPort`); se extendió `ProcessListItem` con `operationalMode`; se actualizó DTP, FSD-UC-019, API contracts y PM-028.
- **Resultado:** rama local `feature-sinaidlc-boris`. Tests ejecutados: `GetProcessStatsServiceTest`, `ProcessControllerQueryTest`, `ProcessStatsControllerWebMvcTest`, `ListProcessesServiceTest` — **pasan** (2026-10-20 ~17:27, `./mvnw -Dtest=… test`, exit 0).
- **Tiempo:** ~15 min de reloj (17:16–17:23 diseño del contrato en chat; 17:23–17:27 implementación + Maven). Primera corrida de tests OK.
- **Intervenciones humanas:** 1 de alcance **antes** de codear (se descartó `GET /api/v1/system/info` por un feature más alineado al dominio). **0** correcciones de código tras tests (no falló Maven). El agente tomó solo decisiones de diseño en el primer pase (`operationalMode` en `ProcessListItem`, `/stats` antes del path UUID, bean en `ProcessModuleConfig`).

## 4. Versión B: con AI-DLC de AWS 2.11.0 (perfil Express)

- **Estado:** no ejecutada. Esta sección se completa al crear `feature-con-aidlc` desde el mismo baseline e implementar el **mismo** endpoint con `/aidlc express`.
- **Comando usado:** `/aidlc express …` — pendiente
- **Salida de `aidlc version`:** pendiente
- **Puertas que revisaron y qué corrigieron con Request Changes** (Reverse Engineering, Requirements Analysis, Code Generation, Build and Test): pendiente
- **Artefactos versionados:** `aidlc/` y `.aidlc-version` — aún no existen en el repo

## 5. Comparación

| Aspecto | A · sin herramienta | B · con AI-DLC de AWS |
|---|---|---|
| Tiempo total | ~15 min (diseño + código + tests) | Pendiente |
| Veces que tuvieron que intervenir o corregir | 1 (cambio de feature `system/info` → `processes/stats`); 0 fallos de test | Pendiente |
| Artefactos que quedaron en el repositorio | Código hexagonal + tests; `PR-IMPL-046`; API-PROC-03b; notas DTP/FSD-UC-019; PM-028; este informe. **Sin** `aidlc/` ni `.aidlc-version` | Pendiente: se espera `aidlc/` (estado, auditoría, `requirements.md`, `code-generation-plan.md`, `test-results.md`) y `.aidlc-version` |
| ¿Las pruebas pasan? (ejecutadas por ustedes) | Sí (Maven, 2026-10-20) | No ejecutadas (versión B no existe) |
| Qué salió mejor | Encaje con hexágono, PBAC existente y docs vivos del curso; un solo pase de tests | Pendiente (hipótesis: puertas y auditoría formales) |
| Qué salió peor | Decisiones de diseño quedaron en el chat, no en un log de puertas; overhead documental SIGESA (PR-IMPL + DTP + FSD + mapping) para un endpoint pequeño | Pendiente (hipótesis: conflicto con capas hexagonales y con el AI-SDLC ya implantado) |

## 6. Conclusiones

1. **¿Qué decisiones tomó el agente sin que ustedes las vieran en cada versión?**  
   **A:** extender `ProcessListItem` en lugar de una query SQL nueva; `frozenCount` derivado de `AUDIT_READONLY`; maps ordenados; DTO web separado del modelo de aplicación; registrar `/stats` antes de `/{processId}`. El humano no revirtió ninguna. **B:** N/A hasta ejecutar Express.

2. **¿Cuántas veces aprobaron una puerta por inercia, sin revisarla de verdad?**  
   **A:** no hay puertas AI-DLC. El equivalente (aprobar PR-IMPL-046 / mapping) se hizo en el mismo turno que el código. **B:** pendiente.

3. **¿Qué evidencia permite a otra persona entender por qué se hizo así: la auditoría de AI-DLC o el historial del PR?**  
   **A:** hoy el chat + `PR-IMPL-046` + `api_contracts.md` + tests. Falta el commit/PR (aún no hay historial Git del feature). **B:** se espera que `aidlc/` cubra el “por qué” de cada puerta; se contrastará con el PR cuando exista.

4. **¿Para qué tamaño de cambio vale la pena AI-DLC de AWS y para cuál no?**  
   **Preliminar (solo con A):** un endpoint de ~15 min con DD-UC ya escrito no justifica otro pipeline de puertas. AI-DLC tendría más sentido en un módulo **nuevo** sin FSD/DD, o en un equipo sin gobernanza propia. Se confirma o se corrige tras B.

5. **¿Lo usarían en su proyecto? ¿Por qué?**  
   **Respuesta preliminar, a confirmar con B:** **no como flujo principal de SIGESA.** El repo ya tiene AI-SDLC (FSD → DD-UC → PR-IMPL → hexágono → mapping). Meter Express duplicaría gobernanza y arriesga código que ignore puertos/adaptadores. Sí se evaluaría como **experimento acotado** (rama throwaway) si B demuestra que las puertas detectan desvíos que el flujo actual se salta. No se usaría para micro-features de un endpoint.

## 7. Antes de entregar

- [x] Versión A implementada; pruebas Maven pasan en A.
- [ ] Las dos ramas están en el repositorio y las pruebas pasan en ambas. *(falta B y push de A)*
- [ ] `aidlc/` y `.aidlc-version` están versionados en la rama B.
- [x] No hay secretos, `.env` ni archivos `*.local.json` en los cambios de A.
- [x] Modalidad individual; tabla de responsables con Boris.
