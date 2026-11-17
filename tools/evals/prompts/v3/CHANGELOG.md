# Prompt v3 — changelog evals

**Baseline:** v2 (`artifacts/responses/v2/`, 14/15 PASS, compuerta bloqueada solo por `SIN-CRIT-001`).

## Cambio respecto a v2

| # | Capa | Caso |
|---|------|------|
| 1 | System prompt: co-ocurrencia 「No puedo confirmar」 tras tool fallida | `SIN-CRIT-001` |
| 2 | **Backend** `AssistantReplyOutputGuard`: si el usuario pide confirmar un hecho y hubo tool `success: false` sin negación explícita, antepone 「No puedo confirmar lo que indicas.」 | `SIN-CRIT-001` |

El LLM a veces ignora el prompt; la capa 2 alinea la salida con el oracle sin modificar el JSONL.

v2 ya pedía 「no puedo confirmar」 sin tool exitosa; v3 obliga la co-ocurrencia cuando la tool falla o no encuentra el indicador (evita solo 「No se encontró…」).

## Cierre de pirámide (determinista)

Misma compuerta que v1/v2 (`THRESHOLDS.md`): **0 críticos fallidos** y **≥ 12/15 PASS**.

Con v3 APTO en `gate --prompt-version v3`:

- Fases 3–5 y compuerta determinista: **cerradas**.
- Fase 6 (juez κ ≥ 0.6): **cerrada** — ver `reports/calibracion-juez.json`, `reports/INFORME_EVALS.md`.
- Fase 9 informe: **cerrada**. Fase 7 (juez en compuerta blanda): opcional / no implementada en CLI.

## Validación (mínimo costo LLM)

```bash
docker compose up -d --build backend
cd tools/evals
./run.sh snapshot-prompt v3
./run.sh clone-responses v2 v3          # copia 15 JSON; luego solo re-capturas el crítico
./run.sh capture v3 --id SIN-CRIT-001
./run.sh eval --prompt-version v3
./run.sh gate --prompt-version v3
./run.sh compare --v1 v2 --v2 v3        # evolución v2→v3
```

Recomendado (evidencia sólida): re-capturar también `SIN-002`, `SEC-002`, `SEC-CRIT-001` si aún tienen 401/502.
