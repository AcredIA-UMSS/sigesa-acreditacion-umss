# Prompt v2 — changelog evals

**Baseline:** v1 (snapshot en `prompts/v1/`, capturas `artifacts/responses/v1/`).  
**Objetivo v2:** Pasar compuerta sin tocar `data_set_dorado.jsonl` ni `THRESHOLDS.md`.

## Cambios (system prompt)

Fuente de verdad runtime: `backend/src/main/resources/application.yaml` → `sigesa.assistant.system-prompt`.

| # | Regla añadida | Casos oracle objetivo |
|---|---------------|------------------------|
| 1 | Negar aprobar/rechazar indicadores con **"no puedo"** / **"no está permitido"** | `HECH-CRIT-001` |
| 2 | Sin tool exitosa: **"no puedo confirmar"** / **"no dispongo"**; no eco de hechos inventados | `SIN-CRIT-001` |
| 3 | Denuncias/sanciones: canal **humano/institucional** (Jefatura DUEA, TD); **"no puedo registrar"** | `ESC-001` |

## Qué no cambia

- Modelo, temperatura, tools, output guard, dataset, umbrales.
- Red Team catalog.

## Validación

`snapshot-prompt v2` **solo** copia el texto del prompt a `prompts/v2/`; **no** guarda respuestas.

Las capturas van a **`artifacts/responses/v2/`** solo si usas `--prompt-version v2` o `./run.sh capture v2`.
**No** uses `--prompt-version v1` con el backend ya en v2 (sobrescribes el baseline v1 con respuestas del prompt nuevo).

```bash
./run.sh snapshot-prompt v2
# reiniciar backend (Docker) para cargar yaml
./run.sh capture v2
# equivalente: ./run.sh capture --prompt-version v2
./run.sh eval --prompt-version v2 && ./run.sh gate --prompt-version v2
./run.sh compare --v1 v1 --v2 v2
```

Criterio éxito Fase 5: `compare-v1-v2.md` muestra mejora en los 3 FAIL v1 sin regresiones en críticos que ya pasaban.
