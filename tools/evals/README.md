# Evals offline — Asistente SIGESA

Evaluación de **calidad y comportamiento** del asistente con dataset dorado versionado (no Red Team).

**Estado (2026-09-25):** compuerta **v3 APTO** (15/15, 0 críticos) · juez **κ = 1.0** (≥ 0.6).

| Doc | Contenido |
|-----|-----------|
| [**INFORME_EVALS.md**](reports/INFORME_EVALS.md) | **Informe final** (APTO, umbrales, v1→v3, juez, costos) |
| [**MANUAL.md**](MANUAL.md) | Seguimiento fase a fase (pirámide completa) |
| [**HISTORIAL_DEFENSA.md**](HISTORIAL_DEFENSA.md) | Línea de tiempo y narrativa defensa |
| [**DATASET_DORADO.md**](DATASET_DORADO.md) | Tabla de los 15 casos |
| [**THRESHOLDS.md**](THRESHOLDS.md) | Umbrales de compuerta (congelados pre-resultados) |
| [`datos/conocimiento/data_set_dorado.jsonl`](datos/conocimiento/data_set_dorado.jsonl) | Dataset machine-readable |

### Verificación rápida (sin LLM salvo juez ya generado)

```bash
cd tools/evals
./run.sh gate --prompt-version v3    # determinista APTO
./run.sh judge-gate                  # κ >= 0.6 (usa judge.jsonl existente)
```

### Pipeline completo (referencia)

```bash
cd tools/evals
./setup.sh
cp .env.example .env   # SIGESA_API_BASE; juez: SIGESA_JUDGE_* (Groq)
docker compose -f ../../docker-compose.yml up -d backend

./run.sh capture --prompt-version v3
./run.sh eval --prompt-version v3 && ./run.sh gate --prompt-version v3

./run.sh seed-human-labels --prompt-version v3 --labeled-by "Nombre"
./run.sh judge-run --prompt-version v3
./run.sh judge-calibrate && ./run.sh judge-gate
```

**Red Team:** [`../red-team-agent/`](../red-team-agent/) — seguridad adversarial, catálogo distinto.
