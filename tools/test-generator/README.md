# Test Generator AcredIA (Python + Docker)

Generador local de tests JUnit vía LLM (Ollama / Groq). **No va a producción.**

```bash
# Desde la raíz del repo
./tools/test-generator/run.sh --show-config
./tools/test-generator/run.sh --list-batches
./tools/test-generator/run.sh --batch L1 --dry-run
./tools/test-generator/run.sh --batch L1
```

Documentación: [`docs/qa/TEST-GENERATOR-PROMPT.md`](../../docs/qa/TEST-GENERATOR-PROMPT.md)

Configuración: `.env` en la raíz del repo (ver `.env.example`).
