# Catálogo Red Team — `ataques/*.json`

Fuente única para listar y ejecutar pruebas con LLM vivo (`./run.sh probar`).

Cada archivo agrupa ataques por tema. Cada entrada incluye `exito_si` (éxito del atacante), `mensaje`, opcionalmente `documento` en `documentos/`, y metadatos OWASP/ATLAS cuando aplica.

| Archivo | Tema |
|---------|------|
| `inyeccion_indirecta.json` | Contenido adjunto / instrucciones ocultas |
| `inyeccion_directa.json` | Override directo del system prompt |
| `fuga_datos.json` | Canarios demo, API keys |
| `envenenamiento_rag.json` | Fragmentos normativos envenenados (simulados) |
| `abuso_herramientas.json` | Tools fuera de rol |
| `guardrails_entrada.json` | Bypass de validación HTTP 400 |
| `politicas_y_confianza.json` | Safety, alucinación, confusión de rol |

**JUnit (CI):** `catalog/attacks.json` + `./run.sh sync` → `RedTeamAssistantCatalogWebMvcTest`.
