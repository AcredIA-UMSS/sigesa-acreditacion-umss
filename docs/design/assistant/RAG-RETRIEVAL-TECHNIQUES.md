# Técnicas de recuperación RAG sobre chunks (`normative_document`)

**FSD:** [FSD-UC-033](../../product/uc/FSD-UC-033.md) · **Design:** [DD-UC-033](../DD-UC-033.md) · **Prompt:** [PR-IMPL-054](../../prompts/impl/PR-IMPL-054.md)  
**Alcance:** el índice ya cargado (chunks Markdown + `tsvector`). No hay PDF en caliente ni embeddings.

Las técnicas se aplican **en cascada** a cada pregunta del asistente (`POST /api/v1/assistant/chat` / tool `search_normative_docs`).

---

## 1. Chunking documental (índice)

| Técnica | Dónde | Qué hace |
|---------|--------|----------|
| Partición por sección | 33 `.md` en classpath `normative-corpus/arcu-sur-2023/` | Un archivo = un chunk = una fila |
| Metadatos de cita | Front matter YAML → `title`, `template_type`, fase/subfase, `source_url` | La respuesta cita apartado, no el PDF |
| Upsert al boot | `NormativeDocumentSeedLoader` | Idempotente por `source_url` |

## 2. Índice léxico (almacenamiento)

| Técnica | Dónde | Qué hace |
|---------|--------|----------|
| Full-text search español | Columna generada `search_vector tsvector` (`to_tsvector('spanish', title \|\| body)`) | Stemming (*pares* ~ *par*), stopwords del diccionario PG |
| Índice GIN | `idx_normative_document_search_vector` | Lookup invertido lexema → filas |
| Ranking `ts_rank` | `NormativeDocumentSearchJpaAdapter` | Ordena por coincidencia léxica |

## 3. Preproceso de la pregunta

| Técnica | Clase | Qué hace |
|---------|--------|----------|
| Sanitizado | `NormativeSearchQueryNormalizer.sanitize` | Quita signos; deja letras/números |
| Query rewriting (prefijos) | `condensedForSearch` | Recorta *«qué dice la normativa sobre…»* |
| Stopwords de dominio | mismo | Tira *qué, de, la, normativa* (lista propia, además del FTS) |
| Tokenización | `significantTerms` | Términos ≥ 3 caracteres |

## 4. Selección de palabras clave (varias claves)

| Técnica | Clase | Qué hace |
|---------|--------|----------|
| Peso de dominio | `NormativeQueryPlanner.weightOf` | Tesauro +3; pista ARCU/CEUB +2; longitud ≥ 6 +1 |
| IDF tosco / genéricos | `NormativeDomainThesaurus.isGeneric` | *normativa, procedimiento, sistema, manual…* peso −2 → **descartadas** |
| Top-k | `MAX_SELECTED = 3` | Se buscan solo las 2–3 de mayor peso, no el orden de aparición |
| Pista de colección | `templateTypeHint` | ARCU-SUR vs CEUB según pistas (*pares, visita* vs *ceub, matriz*) |

Ejemplo: *«cronograma, pares, visita, normativa, procedimiento»*  
→ usadas: `pares`, `visita`, `cronograma` · descartadas: `normativa`, `procedimiento`.

## 5. Expansión por sinónimos (tesauro)

| Técnica | Clase | Qué hace |
|---------|--------|----------|
| Tesauro CEUB/ARCU-SUR | `NormativeDomainThesaurus` | *evaluadores externos / equipo evaluador / BIPE* → canónico `pares` |
| Expansión de lexemas | `expandLexemes` | Añade sinónimos a la consulta OR |

Ejemplo: *«¿Quién arma el equipo de evaluadores externos?»* → clave `pares` → chunk §6.12.

## 6. Estrategia de matching (relajación)

Orden en `NormativeDocumentSearchJpaAdapter.search`:

1. **AND** (`plainto_tsquery`) de las claves elegidas.  
2. **Candidatos condensados** (título+cuerpo).  
3. **OR** (`to_tsquery` con `\|`) de claves + sinónimos.  
4. Término a término (claves seleccionadas).  
5. Fallback histórico: `significantTerms` de la pregunta cruda + **LIKE** si no hay FTS (H2).

## 7. Presentación

| Técnica | Dónde | Qué hace |
|---------|--------|----------|
| Snippet centrado en el hit | `buildSnippet` (~420 chars) | Recorte alrededor del término |
| Transparencia | reply RAG / tool JSON | `Claves usadas: …` y `discardedTerms` |
| Path RAG | `AssistantNormativeRagService` | Si el LLM no llama tool, igual se responde con fragments |

Campos extra en el resultado de `search_normative_docs` (mismo chat, sin endpoint nuevo): `selectedTerms`, `discardedTerms`, `retrievalStrategy`, `templateTypeHint`.

## 8. Fuera de este corte

Embeddings / pgvector, PDFBox en caliente, OCR, quinto `agent=`, UI de biblioteca.

## 9. Cómo verificarlo en Demo 4

1. FAB o `/ayuda`: *«¿Quién arma el equipo de evaluadores externos?»* → cita comités de pares y muestra **Claves usadas: pares**.  
2. *«cronograma pares visita normativa procedimiento»* → no prioriza *normativa*; muestra las tres claves de dominio.  
3. Sin hits tras expansión: no inventar artículos (mensaje de índice vacío).
