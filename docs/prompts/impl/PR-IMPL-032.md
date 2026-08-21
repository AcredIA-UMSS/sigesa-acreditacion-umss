---
id: PR-IMPL-032
feature_asociado: DD-SYS-002
modulo: MOD-ASSISTANT
fecha: "2026-08-21"
estado: Implementado
---

# PR-IMPL-032 — RAG normativo en asistente virtual

## Objetivo

Indexar documentación normativa de acreditación (CEUB, ARCU-SUR, FAQ SIGESA) y exponerla vía RAG en todos los copilotos del asistente.

## In-Scope

- Tabla `normative_document` + migración Flyway `V8__normative_documents.sql` (FTS PostgreSQL).
- Puerto `NormativeDocumentSearchPort` + `SearchNormativeDocumentsUseCase`.
- Tool `search_normative_docs` (roles JD, TD, CC, EE) en los 4 agentes.
- `AssistantNormativeRagService` — enriquecimiento de prompt + fallback RAG directo.
- `NormativeDocumentSeedLoader` — corpus demo (10 documentos).
- Config: `sigesa.assistant.rag-enabled`, `rag-max-chunks`.
- Tests unitarios registry/RAG/sendChat.

## Out-of-Scope

- Ingestión automática de PDFs desde `referenceUrl`.
- Embeddings pgvector (fase 2).

## Post-implementación (PR-IMPL-033)

- Trazabilidad multi-tool en UI (`steps[]` → `CopilotAssistantMetadata` en todos los copilotos).

## Archivos clave

| Capa | Ruta |
|------|------|
| Migración | `backend/src/main/resources/db/migration/V8__normative_documents.sql` |
| Entidad JPA | `adapter/out/persistance/entity/NormativeDocumentJpaEntity.java` |
| Adapter FTS | `adapter/out/persistance/NormativeDocumentSearchJpaAdapter.java` |
| Servicio RAG | `application/service/assistant/AssistantNormativeRagService.java` |
| Orquestación | `application/service/assistant/SendChatMessageService.java` |
| Tool | `AssistantToolRegistry.SEARCH_NORMATIVE_DOCS_ID` |
| Seed | `config/NormativeDocumentSeedLoader.java` |
