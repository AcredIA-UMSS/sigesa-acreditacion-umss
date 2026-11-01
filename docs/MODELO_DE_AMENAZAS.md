# Modelo de amenazas — Asistente IA SIGESA

Documento vivo para el entregable de laboratorio (7 filas). Producto: **SIGESA** — módulo asistente virtual con tool calling sobre procesos de acreditación UMSS.

| # | Dimensión | Descripción (SIGESA) |
|---|-----------|------------------------|
| **1. Activos** | Datos y capacidades a proteger | Credenciales demo y JWT de sesión; `programScope` (carreras); datos de procesos, indicadores y evidencias; fragmentos normativos (RAG); prompts y configuración LLM (`SIGESA_ASSISTANT_*`); integridad del flujo de acreditación (no aprobaciones falsas). |
| **2. Actores de amenaza** | Quién ataca | Usuario autenticado malicioso o comprometido (CC/JD/TD); atacante externo sin cuenta (si expusiera API); operador que abusa del copiloto; contenido indirecto en mensajes/RAG (tercero). |
| **3. Puntos de entrada** | Dónde llega el ataque | `POST /api/v1/assistant/chat` (`message`, `history`, `context.agent`); selección de tools vía LLM; documentos normativos indexados para `search_normative_docs`; login demo en entornos de prueba. |
| **4. Fronteras de confianza** | Límites de confianza | Cliente React → API Spring (JWT + RBAC); validador de entrada vs pipeline LLM; catálogo de tools tipadas vs SQL libre; PBAC por rol y `programScope`; frontera hacia proveedor LLM (Open WebUI/Ollama) como zona no confiable. |
| **5. Ataques posibles** | Vectores | Prompt injection directa/indirecta; exfiltración de contraseñas demo o claves; suplantación de JD/TD; abuso de tools (`list_users`, escritura con confirmación); bypass de guardrails (SQLi/XSS/historial envenenado); instrucciones para falsificar evidencias; fuga de system prompt. |
| **6. Impacto** | Consecuencias | Filtración de credenciales demo o configuración; listado o modificación de usuarios fuera de rol; decisiones falsas sobre acreditación; pérdida de confianza institucional; superficie para escalada si se reutilizan patrones en producción. |
| **7. Mitigaciones** | Controles | `AssistantChatInputValidator` (entrada); `AssistantToolRbacGuard` + PBAC en casos de uso; aislamiento por agente (`general`/`phases`/`users`/`evidence`); **`AssistantReplyOutputGuard`** (salida); catálogo Red Team + JUnit parametrizado; smoke `probar-lab` con 3 repeticiones; secretos solo en env, no en cliente. |

**Trazabilidad Red Team:** catálogo CI `tools/red-team-agent/catalog/attacks.json`; entregable lab `tools/red-team-agent/ataques/LAB-*.json`; hallazgo ejemplo `docs/qa/redteam/AI-SEC-001.md`.
