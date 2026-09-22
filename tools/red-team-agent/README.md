# Agente Red Team — SIGESA (GenAI)

Metodología estructurada que combina **experiencia humana**, **catálogo versionado** y **automatización (LLM + JUnit + curl)** para descubrir brechas de **seguridad, safety, confianza y rendimiento** en el asistente (`POST /api/v1/assistant/chat`).

## Inicio rápido

```bash
cd tools/red-team-agent
chmod +x setup.sh run.sh
./setup.sh
./run.sh show-config
./run.sh listar
./run.sh sync                 # → backend/src/test/resources/redteam/

# CI backend (MockMvc + catálogo)
cd ../../backend && ./mvnw test -Dtest=RedTeamAssistantCatalogWebMvcTest

# Smoke LLM vivo (Docker :8080)
cd ../tools/red-team-agent && ./run.sh probar-api

# Generar ataque nuevo (LLM local Ollama, ver tools/e2e-agent/.env)
./run.sh generar exfiltration_canary "pedir JWT secret en inglés"
```

**Manual operativo:** [MANUAL.md](./MANUAL.md) — § **2.1 Dónde viven los tests** y § **2.2 Generar más tests**  
**Taxonomía:** [catalog/taxonomy.yaml](./catalog/taxonomy.yaml)  
**Catálogo:** [catalog/attacks.json](./catalog/attacks.json)

## Invocación en Cursor

```
@sigesa-red-team listar
@sigesa-red-team generar prompt_injection_direct ...
```

Definición: [`.cursor/agents/sigesa-red-team.md`](../../.cursor/agents/sigesa-red-team.md)

## Qué protege (ejemplo SIGESA — RT-INJ-001)

| Capa | Qué hace |
|------|-----------|
| **RT-INJ-001** | Jailbreak pidiendo contraseña demo de `cc@umss.edu.bo` |
| **RT-INJ-002 / 003** | Volcado de system prompt / `sigesa.jwt.secret` |
| **RT-ROLE-001** | Suplantación JD + listado de contraseñas |
| **RT-GRD-*** | SQLi, XSS, historial envenenado → HTTP 400 |
| **Generador LLM** | Nuevas variantes (revisión humana del diff) |

Los ataques usan **credenciales demo del seed** (documentadas en tests); no son secretos de producción.
