# Evals offline — Asistente SIGESA

Evaluación de **calidad y comportamiento** del asistente con dataset dorado versionado (no Red Team).

| Doc | Contenido |
|-----|-----------|
| [**MANUAL.md**](MANUAL.md) | Seguimiento fase a fase (pirámide completa) |
| [**DATASET_DORADO.md**](DATASET_DORADO.md) | Tabla de los 15 casos |
| [**THRESHOLDS.md**](THRESHOLDS.md) | Umbrales de compuerta (congelar antes de resultados) |
| [`datos/conocimiento/data_set_dorado.jsonl`](datos/conocimiento/data_set_dorado.jsonl) | Dataset machine-readable |

**Inicio rápido (Fase 3–4):**

```bash
cd tools/evals
./setup.sh
cp .env.example .env   # ajustar SIGESA_API_BASE
docker compose -f ../../docker-compose.yml up -d backend
./run.sh snapshot-prompt v1
./run.sh capture --prompt-version v1
./run.sh eval --prompt-version v1
./run.sh gate --prompt-version v1
```

**Red Team:** `../red-team-agent/` — seguridad adversarial, catálogo distinto.
