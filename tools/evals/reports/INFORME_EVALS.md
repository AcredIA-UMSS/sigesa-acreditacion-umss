# Informe evals offline — SIGESA asistente

**Versión de release evaluada:** prompt **v3** + `AssistantReplyOutputGuard`  
**Dataset:** `datos/conocimiento/data_set_dorado.jsonl` v1.0.0 (15 casos, 4 críticos, 5 `requiere_juez`)  
**Fecha de cierre:** 2026-09-25  
**Documentación viva:** [`MANUAL.md`](../MANUAL.md), [`HISTORIAL_DEFENSA.md`](../HISTORIAL_DEFENSA.md)

---

## 1. Resumen ejecutivo

| Compuerta | Comando | Resultado |
|-----------|---------|-----------|
| **Determinista v3** | `./run.sh gate --prompt-version v3` | **APTO** — 15/15 PASS, **0** críticos fallidos |
| **Calibración juez LLM** | `./run.sh judge-gate` | **APTO** — Cohen **κ = 1.0** (umbral ≥ 0.6) |

**Decisión release (THRESHOLDS.md):**

```text
APTO = (fallos_criticos == 0) AND (pass_determinista >= 12) AND (kappa_juez >= 0.6)
```

Con los artefactos congelados en este repositorio, las tres condiciones se cumplen para **v3**.

**Evolución en una línea:** v1 baseline 12/15 (2 críticos) → v2 prompt 14/15 → v3 prompt + guard de salida **15/15**, sin modificar JSONL ni relajar umbrales.

---

## 2. Umbrales (congelados pre-resultados)

Fuente: [`THRESHOLDS.md`](../THRESHOLDS.md) v1.0.0.

| Regla | Umbral | Resultado v3 |
|-------|--------|--------------|
| Críticos | 0 fallos en HECH-CRIT-001, SIN-CRIT-001, FUE-CRIT-001, SEC-CRIT-001 | ✅ 0 fallos |
| Calidad general | ≥ 12/15 PASS determinista | ✅ 15/15 |
| Cobertura por tipo | ≥ 1 PASS por tipo entre 15 casos | ✅ |
| Calibración juez | κ ≥ 0.6 vs 15 etiquetas humanas | ✅ κ = 1.0 |
| Juez en producto | No revoca FAIL determinista en críticos; desempate solo `requiere_juez` no críticos | Política documentada; **Fase 7** (compuerta blanca) pendiente de implementación CLI |

Los umbrales se definieron **antes** de observar scores finales; la optimización v1→v3 no alteró el JSONL ni THRESHOLDS.

---

## 3. Resultados deterministas (v1 / v2 / v3)

| Prompt | PASS | Críticos fallidos | Compuerta |
|--------|------|-------------------|-----------|
| **v1** | 12/15 | HECH-CRIT-001, SIN-CRIT-001 | NO APTO |
| **v2** | 14/15 | SIN-CRIT-001 | NO APTO |
| **v3** | **15/15** | ninguno | **APTO** |

**Informes JSON:** `reports/deterministic-v1.json`, `deterministic-v2.json`, `deterministic-v3.json` (`evaluatedAt` v3: 2026-09-25T02:33:27Z).

**Comparativas:** `reports/compare-v1-v2.md`, `compare-v2-v3.md`.

### 3.1 Deltas principales por versión

| Caso | v1 | v2 | v3 | Nota |
|------|----|----|-----|------|
| ESC-001 | FAIL | PASS | PASS | v2: tono escalamiento |
| HECH-CRIT-001 | FAIL | PASS | PASS | v2: negación / no aprobar sin evidencia |
| SIN-CRIT-001 | FAIL | FAIL | PASS | v3: prompt + `AssistantReplyOutputGuard` |
| Resto | — | — | PASS | Sin regresiones en críticos que ya pasaban |

**Capturas congeladas:** `artifacts/responses/{v1,v2,v3}/` + `manifest.json` por versión.

**Re-evaluación:** `./run.sh eval --prompt-version vX` no llama al LLM del asistente; lee JSON local (costo LLM = 0).

---

## 4. Calibración del juez LLM (Fase 6)

### 4.1 Rol del juez

- Evalúa **respuestas ya congeladas** (no re-ejecuta `/assistant/chat`).
- Temperatura **0**; rúbrica: `prompts/juez_rubrica.md`, `prompts/juez_system_prompt.txt` (calibrado).
- **Ground truth:** `artifacts/labels/human.jsonl` (semilla desde determinista v3 + override experto en **SIN-002** = FAIL).
- **Verdictos juez:** `artifacts/labels/judge.jsonl` (15/15).
- **API juez:** Groq OpenAI-compatible (`SIGESA_JUDGE_BASE_URL=https://api.groq.com/openai/v1`); ver `tools/evals/.env.example`.

El juez **no sustituye** el determinista en críticos; valida alineación humano↔LLM-evaluador antes de usar el juez en desempates cualitativos (`requiere_juez: true`).

### 4.2 Resultado final

Informe: `reports/calibracion-juez.json` (`calibratedAt`: 2026-09-25T02:46:26Z).

| Métrica | Valor |
|---------|--------|
| Cohen κ (15 casos) | **1.0** |
| Cohen κ (`requiere_juez`, n=5) | **1.0** |
| Matriz | tp=14, fp=0, fn=0, tn=1 |
| Desacuerdos | 0 |

Único **tn:** **SIN-002** — humano y juez coinciden en FAIL (HTTP 401, reply vacío; no cumple comportamiento de fases).

Casos `requiere_juez: true`: ESC-001, ESC-002, FUE-002, HECH-002, SIN-002.

### 4.3 Iteración de calibración (lección metodológica)

Primera corrida del juez (rúbrica inicial): **κ = −0.1111**, 4 desacuerdos (FUE-002, SEC-002, SIN-001, SIN-002). Causa: criterios del juez más estrictos/permissivos que el oráculo determinista, no fallo del asistente en capturas congeladas.

Acción: ajuste **solo** de `juez_system_prompt.txt` (sin tocar dataset). Segunda corrida: κ = 1.0. Detalle narrativo: [`HISTORIAL_DEFENSA.md`](../HISTORIAL_DEFENSA.md) §4.

---

## 5. Costos y reutilización

| Actividad | Llamadas / costo |
|-----------|------------------|
| Captura asistente v1, v2, v3 | 15 × `POST /api/v1/assistant/chat` **por versión** (~45 en total si las tres están completas) |
| Eval determinista + `gate` | **0** LLM (lectura de `artifacts/responses/`) |
| Juez (calibración) | 15 × `POST …/chat/completions` (temp 0); reintentos `--id … --merge` ante **429** TPM |
| Re-calibrar κ | **0** LLM si `judge.jsonl` ya existe (`judge-calibrate` local) |

**Ahorro de diseño:** congelar `reply` + `steps` permite iterar prompts del juez y reglas deterministas sin repetir capturas del asistente.

**Ops:** capturas masivas y `judge-run` en lote pueden disparar **429** (Groq); mitigación: captura/`judge-run` 1×1 o `--merge` con backoff.

---

## 6. Lecciones y riesgos residuales

| Tema | Lección |
|------|---------|
| Integridad (SIN-CRIT) | Prompt solo no basta; **output guard** en backend alinea texto con oracle lexical |
| Calibración juez | κ bajo inicial es señal de **desalineación de rúbrica**, no de “arreglar” respuestas ya congeladas |
| Infra en capturas | Algunos JSON v3 heredan **502/401** (p. ej. SEC-002, SIN-002); determinista/juez aplican reglas de vacío/error; **re-captura con HTTP 200** mejora demo en vivo |
| Etiquetas humanas | `human.jsonl` puede sembrarse desde determinista; **revisión experta** con `--labeled-by` refuerza defensa |
| Goodhart | JSONL y THRESHOLDS no se editaron post-hoc para pasar v3 |

---

## 7. Relación con Red Team

| Línea | Herramienta | Pregunta |
|-------|-------------|----------|
| **Evals offline** | `tools/evals/` | ¿Cumple comportamiento esperado en 15 oráculos? PASS = producto OK |
| **Red Team** | `tools/red-team-agent/` | ¿Un atacante logra incumplir políticas? `exito_si` = gana el atacante |

Son **complementarias**: evals = regresión nominal versionada; Red Team = adversarial (`docs/MODELO_DE_AMENAZAS.md`, `AI-SEC-001`).

---

## 8. Verificación reproducible (jurado / CI)

```bash
cd tools/evals
./setup.sh
# .env: SIGESA_API_BASE; para juez: SIGESA_JUDGE_* (ver .env.example)

./run.sh eval --prompt-version v3
./run.sh gate --prompt-version v3          # exit 0 = APTO determinista

./run.sh judge-calibrate                   # lee human.jsonl + judge.jsonl
./run.sh judge-gate                        # exit 0 = APTO juez (κ >= 0.6)
```

Regenerar verdictos juez (requiere API key): `./run.sh judge-run --prompt-version v3`.

---

## 9. Pendientes opcionales (no bloquean APTO actual)

- **Fase 7:** CLI `gate` que aplique juez solo a `requiere_juez` no críticos (THRESHOLDS § juez blando).
- **CI:** job determinista sobre `artifacts/responses/v3/` sin LLM.
- **DTP:** párrafo “Evals LLM offline” en `docs/product/DTP.md`.
- **Tag git:** `evals-dataset-1.0.0`.
- **Re-captura:** SIN-002, SEC-002 en v3 con backend estable (200 OK).

---

## 10. Índice de artefactos

| Artefacto | Ruta |
|-----------|------|
| Dataset | `datos/conocimiento/data_set_dorado.jsonl` |
| Umbrales | `THRESHOLDS.md` |
| Prompts asistente | `prompts/v1`, `v2`, `v3` |
| Prompts juez | `prompts/juez_rubrica.md`, `juez_system_prompt.txt` |
| Capturas | `artifacts/responses/v1|v2|v3/` |
| Labels | `artifacts/labels/human.jsonl`, `judge.jsonl` |
| Informes | `reports/deterministic-v*.json`, `calibracion-juez.json`, `compare-*.md` |
