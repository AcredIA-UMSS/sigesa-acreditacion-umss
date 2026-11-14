---
id: PR-IMPL-048
feature_asociado: DD-UC-033
fsd_uc:
  - FSD-UC-033
fecha: "2026-11-04"
version: "1.0"
estado: Implementado
autor: "Boris Anthony Angulo Urquieta"
github_issue: "https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/76"
release: "2.1.0"
---

# Prompt Contract — Implementación `PR-IMPL-048`

> **Issue:** [#76](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/76) — Agente que lea documentos (RAG ARCU-SUR).  
> **Design:** [`DD-UC-033`](../../design/DD-UC-033.md)  
> **FSD:** [`FSD-UC-033`](../../product/uc/FSD-UC-033.md)  
> **Base RAG:** [`PR-IMPL-032`](PR-IMPL-032.md) (no reemplazar el motor; solo corpus).

---

## 1. Propósito

Conectar el *Manual de procedimientos ARCU-SUR 2023* al RAG del asistente existente y dejar trazabilidad AI-SDLC (FSD, DD, DTP, sprint 03).

## 2. In-Scope

- Chunks Markdown en `backend/src/main/resources/normative-corpus/arcu-sur-2023/`.
- Parser de front matter + `NormativeDocumentSeedLoader` upsert idempotente.
- Borrar placeholders ARCU-SUR `duea.umss.edu.bo/normativa/arcu-sur/`.
- Ampliar `AssistantNormativeRagService` (queries de pares, autoevaluación, convocatoria, visita).
- Tests parser/corpus/RAG.
- Docs vivos: FSD-UC-033, DD-UC-033, DTP, TOOL-CATALOG, issue #76, `@save-prompt-mapping sprint=03`.

## 3. Out-of-Scope

- Quinto `agent=`.
- Ingestión PDF en caliente / PDFBox.
- pgvector.
- UI de citas o biblioteca normativa.
- `docs/baseline/`.
- Versionar el PDF en git (`tmp/rag-inbox/` gitignored).

## 4. Reglas

| ID | Regla |
|----|-------|
| R1 | Reusar `POST /api/v1/assistant/chat` y `search_normative_docs`. |
| R2 | Índice = texto Markdown; PDF solo origen. |
| R3 | Dominio sin Spring/JPA nuevo; seed en config/adapter. |
| R4 | No inventar requisitos si FTS no devuelve hits. |
| R5 | Cierre: `@save-prompt-mapping sprint=03 fsd=FSD-UC-033`. |

## 5. Salida esperada

Corpus en classpath, loader al boot, tests verdes, FSD/DD/PR-IMPL y PM-037 en sprint 03.
