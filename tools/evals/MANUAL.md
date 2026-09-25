# Manual de seguimiento — Evals offline (pirámide SIGESA)

Guía paso a paso para el entregable de evaluación del asistente GenAI (`POST /api/v1/assistant/chat`).  
**Relacionado:** Red Team = seguridad adversarial (`tools/red-team-agent/`). **Evals** = calidad y comportamiento con dataset dorado.

**Informe de cierre:** [`reports/INFORME_EVALS.md`](reports/INFORME_EVALS.md) · **Defensa:** [`HISTORIAL_DEFENSA.md`](HISTORIAL_DEFENSA.md)

---

## Estado del workspace (2026-09-25)

| Artefacto | Ruta | Estado |
|-----------|------|--------|
| Dataset 15 oráculos | `datos/conocimiento/data_set_dorado.jsonl` | ✅ v1.0.0 |
| Tabla humana | `DATASET_DORADO.md` | ✅ |
| Umbrales (pre-resultados) | `THRESHOLDS.md` | ✅ |
| Runner / compuerta CLI | `agente_evals.py` + `./run.sh` | ✅ |
| Respuestas v1 / v2 / v3 | `artifacts/responses/` | ✅ |
| Informes deterministas | `reports/deterministic-v{1,2,3}.json` | ✅ |
| Comparativas | `reports/compare-v1-v2.md`, `compare-v2-v3.md` | ✅ |
| Rúbrica + prompt juez | `prompts/juez_rubrica.md`, `juez_system_prompt.txt` | ✅ calibrado |
| Etiquetas humanas (15) | `artifacts/labels/human.jsonl` | ✅ semilla v3 + override SIN-002 (revisar `--labeled-by`) |
| Verdictos juez (15) | `artifacts/labels/judge.jsonl` | ✅ |
| κ Cohen | `reports/calibracion-juez.json` | ✅ κ = 1.0 |
| Reporte final | `reports/INFORME_EVALS.md` | ✅ |
| Fase 7 compuerta blanda juez | CLI | ⬜ opcional / futuro |
| CI determinista offline | monorepo | ⬜ opcional |

---

## Pirámide de trabajo (orden estricto)

```text
[Fase 1] Dataset dorado congelado                    ✅
[Fase 2] Umbrales THRESHOLDS.md                     ✅
[Fase 3] Prompt v1 + captura → artifacts/.../v1/  ✅
[Fase 4] Métricas deterministas                     ✅
[Fase 5] Prompt v2 + compare v1 vs v2               ✅
[Fase 8] Prompt v3 + compuerta (0 críticos + ≥12)   ✅  (orden real: antes de juez)
[Fase 6] Calibración juez (κ ≥ 0.6)                 ✅
[Fase 9] Informe final                              ✅
[Fase 7] Juez en requiere_juez (compuerta blanda)   ⬜ pendiente
```

*Nota:* En la práctica, **v3 APTO** precedió a la calibración del juez (Fase 6), como recomienda este manual.

---

## Fase 1 — Dataset dorado ✅

- [x] 15 casos, 5 tipos, 4 críticos.
- [ ] Tag git opcional: `evals-dataset-1.0.0`.
- [ ] Revisión humana: ¿mensajes realistas para demo CC/JD?

**No modificar** JSONL durante optimización v3 salvo bug factual (nueva versión 1.0.1 documentada).

---

## Fase 2 — Umbrales ✅

Leer [`THRESHOLDS.md`](THRESHOLDS.md) antes de cualquier `./run`.

- [x] Críticos = 0 fallos → exit 1
- [x] General ≥ 12/15
- [x] Fecha de cierre documentada en `INFORME_EVALS.md` (2026-09-25)

---

## Fase 3 — Prompt v1 y captura ✅

- [x] `prompts/v1/`, `artifacts/responses/v1/` (15 + manifest)
- [x] `./run.sh snapshot-prompt`, `capture`, `rebuild-manifest` según necesidad

**Sin juez en esta fase.**

```bash
cd tools/evals
./setup.sh
./run.sh show-config
./run.sh snapshot-prompt v1
./run.sh capture --prompt-version v1
./run.sh capture --prompt-version v1 --id HECH-001   # 1×1 ante 429/502
```

---

## Fase 4 — Evaluación determinista ✅

- [x] `reports/deterministic-v1.json` (y v2, v3)
- [x] Compuerta implementada: `./run.sh gate --prompt-version vX`

```bash
./run.sh eval --prompt-version v3
./run.sh gate --prompt-version v3
./run.sh rebuild-manifest --prompt-version v3
```

---

## Fase 5 — Prompt v2 y comparación ✅

- [x] `prompts/v2/CHANGELOG.md`
- [x] `artifacts/responses/v2/`
- [x] `./run.sh compare --v1 v1 --v2 v2` → `reports/compare-v1-v2.md`

---

## Fase 8 — Prompt v3 y compuerta ✅

- [x] `prompts/v3/CHANGELOG.md` (prompt + `AssistantReplyOutputGuard`)
- [x] `artifacts/responses/v3/`
- [x] `./run.sh gate --prompt-version v3` → **APTO** (15/15, 0 críticos)
- [x] `./run.sh compare --v1 v2 --v2 v3` → `compare-v2-v3.md`
- [x] Tabla v1/v2/v3 en `INFORME_EVALS.md`

```bash
docker compose -f ../../docker-compose.yml up -d --build backend
./run.sh snapshot-prompt v3
./run.sh capture --prompt-version v3
./run.sh eval --prompt-version v3
./run.sh gate --prompt-version v3
```

---

## Fase 6 — Calibración del modelo juez ✅

**Requisito previo:** respuestas v3 congeladas y gate determinista APTO.

1. Rúbrica → `prompts/juez_rubrica.md` + `prompts/juez_system_prompt.txt` ✅
2. Etiquetas humanas:

```bash
./run.sh seed-human-labels --prompt-version v3 --labeled-by "Tu Nombre"
# editar human.jsonl si discrepas (ground truth)
```

3. Juez LLM (temp **0**, Groq recomendado — `tools/evals/.env.example`):

```bash
./run.sh judge-run --prompt-version v3
./run.sh judge-run --prompt-version v3 --id SEC-CRIT-001 --merge   # reintento 429
```

4. Calibración:

```bash
./run.sh judge-calibrate
./run.sh judge-gate    # exit 0 si κ >= 0.6  → κ = 1.0 (2026-09-25)
```

Checklist:

- [x] κ ≥ 0.6 (iteración de rúbrica juez, sin tocar JSONL)
- [x] Matriz en `calibracion-juez.json`
- [x] `INFORME_EVALS.md` actualizado

Casos `requiere_juez: true`: ESC-001, ESC-002, FUE-002, HECH-002, SIN-002.

---

## Fase 7 — Juez en compuerta blanda ⬜

- Aplicar juez únicamente a casos `requiere_juez` **no críticos**.
- Críticos: **solo** determinista (+ guard de salida backend).
- Registrar tokens/llamadas → costo parcial en informe.
- **No implementado** en `agente_evals.py`; hoy: `gate` + `judge-gate` por separado.

---

## Fase 9 — Informe final ✅

Entregable: [`reports/INFORME_EVALS.md`](reports/INFORME_EVALS.md) (resumen APTO, umbrales, v1/v2/v3, κ, costos, Red Team, verificación).

---

## Integración con el monorepo (futuro)

| Componente | Acción |
|------------|--------|
| `agente_evals.py` + `run.sh` | ✅ capture, eval, gate, juez |
| Compartir evaluador | Extraer tipos comunes con `redteam/evaluator.py` |
| CI | Job opcional: `./run.sh gate --prompt-version v3` sin LLM |
| DTP | Sección “Evals LLM offline” en `docs/product/DTP.md` |
| Red Team | Mantener separado; evals no reemplaza `probar` |

---

## Comandos CLI (referencia)

```bash
cd tools/evals
./run.sh listar
./run.sh snapshot-prompt v3
./run.sh capture --prompt-version v3 [--id CASE] [--merge]
./run.sh eval --prompt-version v3
./run.sh gate --prompt-version v3
./run.sh compare --v1 v1 --v2 v2
./run.sh clone-responses v2 v3
./run.sh rebuild-manifest --prompt-version v3

./run.sh seed-human-labels --prompt-version v3 --labeled-by "Nombre"
./run.sh judge-run --prompt-version v3 [--id CASE] [--merge]
./run.sh judge-calibrate
./run.sh judge-gate
```

---

## Referencias SIGESA

- Capacidades por rol/agente: `AssistantCapabilitiesCatalog.java`, `docs/design/assistant/DD-AGENT-001.md`
- Estados evidencia/indicador: `docs/product/uc/FSD-UC-009.md`, diagramas MAR-STA-001
- Seguridad: `docs/MODELO_DE_AMENAZAS.md`, `AssistantReplyOutputGuard`

---

## Bitácora

| Fecha | Fase | Notas |
|-------|------|-------|
| — | 1 | Dataset v1.0.0 generado |
| — | 2 | Umbrales congelados (`THRESHOLDS.md`) |
| 2026-09-25 | 3–4 | Capturas v1/v2/v3; determinista v1 12/15 |
| 2026-09-25 | 5 | v2 14/15; `compare-v1-v2.md` |
| 2026-09-25 | 8 | v3 15/15; output guard; `gate v3` APTO |
| 2026-09-25 | 6 | Juez Groq; κ −0.11 → calibración rúbrica → κ 1.0; `judge-gate` APTO |
| 2026-09-25 | 9 | `INFORME_EVALS.md` + sync MANUAL / README |
