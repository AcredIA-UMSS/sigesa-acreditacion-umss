# Red Team — entregables de laboratorio (SIGESA)

| Entregable | Ubicación |
|------------|-----------|
| Modelo de amenazas (7 filas) | [`docs/MODELO_DE_AMENAZAS.md`](../../MODELO_DE_AMENAZAS.md) |
| Plantilla hallazgo | [`docs/PLANTILLA_HALLAZGO.md`](../../PLANTILLA_HALLAZGO.md) |
| Hallazgo ejemplo | [`AI-SEC-001.md`](AI-SEC-001.md) |
| 5 ataques JSON (lab) | [`tools/red-team-agent/ataques/LAB-*.json`](../../../tools/red-team-agent/ataques/) |
| Catálogo CI ampliado | [`tools/red-team-agent/catalog/attacks.json`](../../../tools/red-team-agent/catalog/attacks.json) |

## Ejecutar los 5 ataques × 3 repeticiones

```bash
cd tools/red-team-agent
docker compose -f ../../docker-compose.yml up -d backend   # LLM ON
./run.sh probar-lab --trials 3 --report reports/lab-run.json
```

Interpretación: **`attackSuccessCount`** = veces que el atacante ganó (defensa falló). **`defensePassCount`** = veces que SIGESA resistió según `successCriteria`.
