# Agente E2E SIGESA (Python + SDK)

Planner y Generator **sin IDE ni navegador** para tests Playwright.

**Manual completo (IA local Docker, flujo, troubleshooting):** **[MANUAL.md](./MANUAL.md)**

## Inicio rápido

```bash
# 1. IA local
docker compose up -d ollama
docker exec sigesa-ollama ollama pull qwen2.5:7b

# 2. Entorno Python (una vez)
cd tools/e2e-agent
./setup.sh          # crea .venv + pip install + .env si falta

# 3. Probar LLM
./run.sh probar

# 4. Plan → generar → Playwright
./run.sh plan ayuda
./run.sh generar ayuda 1.2
cd ../../frontend && PW_SKIP_BACKEND=1 pnpm test:e2e tests/agente/<archivo>.spec.ts
```

Alternativa manual: `source .venv/bin/activate` y luego `python agente_e2e.py ...`

Config por defecto: **Ollama en Docker** (`localhost:11434`). Ver `MANUAL.md` §5.
