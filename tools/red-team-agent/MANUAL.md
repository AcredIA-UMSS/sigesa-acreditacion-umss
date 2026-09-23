# Manual Red Team — Asistente SIGESA

## 1. Alcance

- **API:** `POST /api/v1/assistant/chat` (JWT CC/JD/TD según agente)
- **Agentes:** `general`, `phases`, `users`, `evidence`
- **Fuera de alcance inicial:** ataques físicos, phishing real, DoS de producción

## 2. Arquitectura del catálogo

| Capa | Ruta | Uso |
|------|------|-----|
| **Operativo (LLM vivo)** | `ataques/*.json` | `./run.sh listar`, `./run.sh probar` — formato `exito_si`, documentos adjuntos |
| **CI Maven** | `catalog/attacks.json` | `./run.sh sync` → `RedTeamAssistantCatalogWebMvcTest` (legacy `forbiddenInReply` / guardrails 400) |

```text
ataques/*.json  ──► ./run.sh probar [--trials 3]  ──► reports/last-run.json
catalog/attacks.json ──► ./run.sh sync ──► JUnit parametrizado (LLM mockeado)
```

## 2.1 Generar más casos (LLM → CI)

```bash
cd tools/red-team-agent
./setup.sh
./run.sh generar exfiltration_canary "hint"
./run.sh sync
cd ../../backend && ./mvnw test -Dtest=RedTeamAssistantCatalogWebMvcTest
```

Nuevos casos con criterios `exito_si`: editar o añadir entradas en `ataques/<tema>.json` manualmente.

## 3. Criterios de evaluación

- **`exito_si`** (ataques/): el atacante gana si se cumple `cualquiera` o `todas` las condiciones (`texto_contiene`, `herramienta_ejecutada`, `http_status`, `regex_en_texto`). No se usa igualdad de texto completo.
- **Defensa OK** = el atacante **no** ganó en ese trial.
- **CI:** `forbiddenInReply` / `expectHttpStatus` en `catalog/attacks.json`.

## 4. Comandos

| Comando | Descripción |
|---------|-------------|
| `./run.sh listar [--categoria X] [--id AI-SEC-004]` | Todos los casos en `ataques/` |
| `./run.sh listar --ci` | Solo `catalog/attacks.json` (JUnit) |
| `./run.sh cobertura` | Casos por categoría en `ataques/` |
| `./run.sh sync` | CI: copia `catalog/attacks.json` al backend |
| **`./run.sh probar`** | **Suite completa**, 3 repeticiones + informe JSON |
| `./run.sh probar --trials 1 --id AI-SEC-004` | Un caso, smoke rápido |
| `./run.sh probar-api` | Alias: default 1 repetición |
| `./run.sh probar-lab` | Alias: default 3 repeticiones |
| `./run.sh generar <cat> [hint]` | Nuevo ataque en catalog CI |
| `./run.sh show-config` | API + LLM |

```bash
docker compose up -d backend
./run.sh probar --report reports/last-run.json
```

## 5. Documentación de seguridad (curso / SIGESA)

| Artefacto | Ruta |
|-----------|------|
| Modelo de amenazas | `docs/MODELO_DE_AMENAZAS.md` |
| Plantilla hallazgo | `docs/PLANTILLA_HALLAZGO.md` |
| Hallazgo ejemplo | `docs/qa/redteam/AI-SEC-001.md` |
| Catálogo de ataques | `ataques/*.json` (ver `ataques/README.md`) |

Mitigación en código: `AssistantChatInputValidator`, `AssistantReplyOutputGuard`, RBAC de tools.

## 6. Gobernanza

- Revisar JSON generado por LLM antes de merge.
- Canarios ficticios; no PII ni secretos reales.
- Hallazgos → `docs/qa/redteam/` / ADR, no `docs/baseline/`.
