# Manual de seguimiento — Evals offline (pirámide SIGESA)

Guía paso a paso para el entregable de evaluación del asistente GenAI (`POST /api/v1/assistant/chat`).  
**Relacionado:** Red Team = seguridad adversarial (`tools/red-team-agent/`). **Evals** = calidad y comportamiento con dataset dorado.

---

## Estado del workspace

| Artefacto | Ruta | Estado |
|-----------|------|--------|
| Dataset 15 oráculos | `datos/conocimiento/data_set_dorado.jsonl` | ✅ v1.0.0 |
| Tabla humana | `DATASET_DORADO.md` | ✅ |
| Umbrales (pre-resultados) | `THRESHOLDS.md` | ✅ |
| Runner / compuerta CLI | `agente_evals.py` + `./run.sh` | ✅ Fase 3–4 |
| Respuestas v1 / v2 / v3 | `artifacts/responses/` | ⬜ pendiente |
| Etiquetas humanas (15) | `artifacts/labels/human.jsonl` | ⬜ pendiente |
| Rúbrica juez | `prompts/juez_rubrica.md` | ⬜ pendiente |
| κ Cohen | `reports/calibracion-juez.json` | ⬜ pendiente |
| Reporte final | `reports/INFORME_EVALS.md` | ⬜ pendiente |

Marca ✅ en este table cuando cierres cada ítem.

---

## Pirámide de trabajo (orden estricto)

```text
[Fase 1] Dataset dorado congelado
    ↓
[Fase 2] Umbrales THRESHOLDS.md (no cambiar tras ver scores)
    ↓
[Fase 3] Prompt v1 + captura respuestas → artifacts/.../v1/
    ↓
[Fase 4] Métricas deterministas sobre respuestas guardadas
    ↓
[Fase 5] Prompt v2 + captura → comparar v1 vs v2 (determinista)
    ↓
[Fase 6] Calibración juez (15 etiquetas humanas + κ ≥ 0.6)
    ↓
[Fase 7] Juez en casos requiere_juez (solo donde haga falta)
    ↓
[Fase 8] Prompt v3 + compuerta (0 críticos + ≥12/15)
    ↓
[Fase 9] Informe (umbrales, costos, reutilización)
```

---

## Fase 1 — Dataset dorado ✅ (hecho)

- [x] 15 casos, 5 tipos, 4 críticos.
- [ ] Tag git opcional: `evals-dataset-1.0.0`.
- [ ] Revisión humana: ¿mensajes realistas para demo CC/JD?

**No modificar** JSONL durante optimización v3 salvo bug factual (nueva versión 1.0.1 documentada).

---

## Fase 2 — Umbrales ✅ (hecho)

Leer y firmar mentalmente [`THRESHOLDS.md`](THRESHOLDS.md) antes de cualquier `./run`.

Checklist:

- [x] Críticos = 0 fallos → exit 1
- [x] General ≥ 12/15
- [ ] Registrar fecha de congelación en informe final

---

## Fase 3 — Prompt v1 y captura de respuestas

**Objetivo:** separar generación de evaluación (respuestas congeladas).

1. Snapshot del system prompt actual → `prompts/v1/system_prompt.txt` (copiar de `backend/src/main/resources/application.yaml` o env).
2. Backend + LLM ON; misma config documentada (modelo, temperatura proveedor).
3. Por cada línea del JSONL (cuando exista runner), o script temporal:
   - Login `loginAs`
   - POST chat con `agent` / `context`
   - Guardar: `artifacts/responses/v1/{id}.json` con `{ "reply", "steps", "httpStatus", "timestamp" }`

Checklist:

- [ ] Backend + LLM ON (`docker compose up -d backend`)
- [ ] `SIGESA_API_BASE=http://127.0.0.1:8080` en `.env` o entorno
- [ ] `./run.sh snapshot-prompt v1`
- [ ] `./run.sh capture --prompt-version v1`
- [ ] 15 archivos en `artifacts/responses/v1/`
- [ ] `artifacts/responses/v1/manifest.json` (commit git, fecha)

**Sin juez en esta fase.**

```bash
cd tools/evals
./setup.sh
./run.sh show-config
./run.sh snapshot-prompt v1
./run.sh capture --prompt-version v1
# un caso:
./run.sh capture --prompt-version v1 --id HECH-001
```

---

## Fase 4 — Evaluación determinista (sin costo)

1. Implementar o usar evaluador sobre respuestas **guardadas** (no re-POST).
2. Por caso: evaluar `criterios_pass` (misma semántica que red-team pero PASS = cumple).
3. Reporte: `reports/deterministic-v1.json` con `{ id, pass, matched, critical }`.

```bash
./run.sh eval --prompt-version v1
./run.sh gate --prompt-version v1   # exit 1 si falla compuerta
```

Checklist:

- [ ] `reports/deterministic-v1.json`
- [ ] 4 críticos en `criticalFailures` (debe estar vacío para APTO)
- [ ] Conteo ≥ 12/15 PASS

---

## Fase 5 — Prompt v2 y comparación

1. Definir cambio v2 (documentar en `prompts/v2/CHANGELOG.md`).
2. Capturar `artifacts/responses/v2/*.json` (misma suite, mismas condiciones).
3. Determinista v1 vs v2 side-by-side: `reports/compare-v1-v2.md`

Checklist:

- [ ] Tabla: caso | v1 PASS | v2 PASS | delta
- [ ] No tocar dataset ni umbrales

---

## Fase 6 — Calibración del modelo juez

**Solo después** de tener respuestas v1 (o v2) guardadas.

1. Etiquetar **manualmente** PASS/FAIL (o escala 1–3) para las **15** respuestas → `artifacts/labels/human.jsonl`.
2. Redactar rúbrica fija → `prompts/juez_rubrica.md` (criterios por `tipo`).
3. Juez LLM: temperatura **0**, mismo modelo documentado.
4. Correr juez sobre las 15 → `artifacts/labels/judge.jsonl`.
5. Calcular **Cohen κ** → `reports/calibracion-juez.json`.

Checklist:

- [ ] κ ≥ 0.6 o iterar **solo rúbrica/prompt del juez**, no el dataset
- [ ] Matriz de confusión en informe

Casos con `requiere_juez: true` en dataset: HECH-002, SIN-002, FUE-002, ESC-001, ESC-002.

---

## Fase 7 — Juez en compuerta blanda

- Aplicar juez únicamente a casos `requiere_juez` **no críticos**.
- Críticos: **solo** determinista (+ guard de salida backend).
- Registrar tokens/llamadas → costo parcial en informe.

---

## Fase 8 — Prompt v3 (optimización honesta)

1. Diseñar v3 para subir PASS sin tocar JSONL ni THRESHOLDS.
2. `artifacts/responses/v3/`
3. Compuerta:

```bash
# Futuro:
# ./run.sh gate --prompt-version v3
# exit 0 solo si: críticos 0/4 AND pass >= 12/15
```

Checklist:

- [ ] v3 documentado en `prompts/v3/CHANGELOG.md`
- [ ] Comparativa v1 / v2 / v3 en informe

---

## Fase 9 — Informe final

Plantilla mínima `reports/INFORME_EVALS.md`:

1. Resumen ejecutivo (¿apto sí/no?)
2. Justificación de umbrales (copiar/refinar THRESHOLDS.md)
3. Resultados deterministas v1/v2/v3 (tablas)
4. Calibración juez (κ, ejemplos desacuerdo)
5. **Costos:** # POST chat, # llamadas juez, tokens estimados, ahorro por re-evaluar JSON guardado
6. Lecciones y riesgos residuales
7. Relación con Red Team (`tools/red-team-agent`) — evals ≠ adversarial

---

## Integración con el monorepo (futuro)

| Componente | Acción |
|------------|--------|
| `agente_evals.py` + `run.sh` | Cargar JSONL, login, capture, gate, eval offline |
| Compartir evaluador | Extraer tipos comunes con `redteam/evaluator.py` (invertir PASS) |
| CI | Job opcional: determinista sobre `artifacts/` sin LLM |
| DTP | Sección “Evals LLM offline” cuando compuerta exista |
| Red Team | Mantener separado; evals no reemplaza `probar` |

---

## Comandos CLI

```bash
cd tools/evals
./run.sh listar
./run.sh snapshot-prompt v1
./run.sh capture --prompt-version v1
./run.sh eval --prompt-version v1
./run.sh gate --prompt-version v1
```

---

## Referencias SIGESA

- Capacidades por rol/agente: `AssistantCapabilitiesCatalog.java`, `docs/design/assistant/DD-AGENT-001.md`
- Estados evidencia/indicador: `docs/product/uc/FSD-UC-009.md`, diagramas MAR-STA-001
- Seguridad: `docs/MODELO_DE_AMENAZAS.md`, `AssistantReplyOutputGuard`

---

## Bitácora (rellenar al avanzar)

| Fecha | Fase | Notas |
|-------|------|-------|
| | 1 | Dataset v1.0.0 generado |
| | 2 | Umbrales congelados |
| | 3 | Captura v1 |
| | 4 | Determinista v1 |
| | 5 | v2 + compare |
| | 6 | κ juez |
| | 8 | v3 + gate |
| | 9 | Informe entregado |
