---
id: FSD-UC-033
nombre: Consulta normativa ARCU-SUR vía asistente (corpus)
estado: Implementado (corpus v1)
release: v2.1
actor_principal: "[JD], [TD], [CC], [EE]"
trazabilidad_prd: PRD-REQ-028, MRD-N-22
modulo: MOD-ASSISTANT
reglas: FSD-BR-04, FSD-BR-11
design_doc: DD-UC-033
pr_impl: PR-IMPL-048
github_issue: "https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/76"
ultima_actualizacion: "2026-11-04"
nota_implementacion: "Extiende el RAG de PR-IMPL-032. No crea un quinto agent=. Corpus: Manual de procedimientos ARCU-SUR 2023 en classpath."
---

# FSD-UC-033 — Consulta normativa ARCU-SUR vía asistente

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-028 · MRD-N-22 (chatbot FAQ normativo, sin dictámenes) · [#76](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/76) |
| **Precondiciones** | Usuario autenticado; `SIGESA_ASSISTANT_RAG_ENABLED=true`; backend arrancó y upsertó el corpus |
| **Design** | [`DD-UC-033`](../../design/DD-UC-033.md) |
| **Motor RAG** | [`PR-IMPL-032`](../../prompts/impl/PR-IMPL-032.md) (FTS + `search_normative_docs`) |
| **Prompt de este corte** | [`PR-IMPL-048`](../../prompts/impl/PR-IMPL-048.md) |
| **Superficie** | `/ayuda`, FAB general (`agent=general`) y copilotos que ya exponen `search_normative_docs` |

El coordinador o técnico pregunta en lenguaje natural sobre **procedimientos ARCU-SUR**. El asistente responde con fragmentos indexados del *Manual de procedimientos del Sistema ARCU-SUR 2023*, no con un PDF abierto en el cliente y **sin sustituir dictamen humano**.

## Actores y PBAC

Misma matriz que la tool `search_normative_docs`: **JD, TD, CC, EE** (lectura). No hay tool de escritura de corpus en v1.

## Flujo principal

1. Usuario autenticado envía mensaje en el chat del asistente (p. ej. «¿Cómo se constituye el comité de pares ARCU-SUR?»).
2. Backend clasifica pregunta normativa y/o el LLM elige `search_normative_docs`.
3. PostgreSQL FTS busca en `normative_document` (`template_type=ARCU-SUR`).
4. Respuesta con fragmentos, título de sección y `source_url` de cita. Path `RAG` o tool + LLM.
5. Si no hay hits: no inventar requisitos; decir que no está en el índice.

## Corpus v1

- Origen oficial: PDF *Manual de procedimientos del Sistema ARCU-SUR 2023* (no versionado; bandeja local `tmp/rag-inbox/`).
- Índice: Markdown por sección en `backend/src/main/resources/normative-corpus/arcu-sur-2023/`.
- Carga: `NormativeDocumentSeedLoader` al boot (upsert por `source_url`).

## Excepciones

| Condición | Respuesta |
|-----------|-----------|
| RAG deshabilitado | Chat sin contexto normativo de este UC |
| Pregunta no normativa | Router/LLM no fuerza RAG |
| Sin hits | No fabricar artículos del manual |

## Fuera de alcance (v1)

- Nuevo `agent=` o microservicio.
- Ingestión PDF en caliente (PDFBox) — sigue out-of-scope de PR-IMPL-032.
- Embeddings / pgvector.
- UI de biblioteca (alta/baja de documentos) y citas ricas en el chat.
- Dictamen de acreditación vía chat.

## Criterios Gherkin

```gherkin
@FSD-UC-033 @rag-arcu-sur @PRD-REQ-028
Escenario: Consulta de procedimiento ARCU-SUR con corpus indexado
  Dado un usuario autenticado y el corpus ARCU-SUR 2023 cargado
  Cuando pregunta "¿Cómo se constituye el comité de pares ARCU-SUR?"
  Entonces el asistente usa search_normative_docs o path RAG
  Y la respuesta cita un fragmento del manual 2023 (no el placeholder de demo)

@FSD-UC-033 @rag-arcu-sur
Escenario: El cliente no lee el PDF
  Dado el FAB o /ayuda
  Cuando el usuario envía un mensaje
  Entonces solo se llama POST /api/v1/assistant/chat
  Y no se transfiere el PDF al navegador
```
