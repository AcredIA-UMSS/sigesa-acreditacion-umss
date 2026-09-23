# Red Team — SIGESA

| Artefacto | Ubicación |
|-----------|-----------|
| Modelo de amenazas | [`docs/MODELO_DE_AMENAZAS.md`](../../MODELO_DE_AMENAZAS.md) |
| Plantilla hallazgo | [`docs/PLANTILLA_HALLAZGO.md`](../../PLANTILLA_HALLAZGO.md) |
| Hallazgo ejemplo | [`AI-SEC-001.md`](AI-SEC-001.md) |
| Catálogo completo | [`tools/red-team-agent/ataques/`](../../../tools/red-team-agent/ataques/) |

## Ejecutar la suite

```bash
cd tools/red-team-agent
docker compose -f ../../docker-compose.yml up -d backend
./run.sh probar --trials 3 --report reports/last-run.json
```

Informe: `attackSuccessCount` por ataque (veces que ganó el atacante en N trials).
