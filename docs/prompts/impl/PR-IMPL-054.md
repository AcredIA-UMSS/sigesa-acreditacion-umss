---
id: PR-IMPL-054
feature_asociado: DD-UC-033
fsd_uc:
  - FSD-UC-033
fecha: "2026-11-15"
version: "1.0"
estado: Implementado
autor: "Boris Anthony Angulo Urquieta"
release: "8.4.0"
---

# Prompt Contract — Implementación `PR-IMPL-054`

> **Design:** [`DD-UC-033`](../../design/DD-UC-033.md)  
> **FSD:** [`FSD-UC-033`](../../product/uc/FSD-UC-033.md)  
> **Técnicas:** [`RAG-RETRIEVAL-TECHNIQUES.md`](../../design/assistant/RAG-RETRIEVAL-TECHNIQUES.md)  
> **Base:** PR-IMPL-032 + PR-IMPL-048 (corpus ya indexado).

## 1. Propósito

Aplicar técnicas de recuperación (sinónimos, peso de claves, AND→OR) sobre los **chunks ya guardados** en `normative_document`, sin nuevo agente ni PDF en caliente.

## 2. In-Scope

- Tesauro de dominio y planificador de consulta.
- Cascada AND / OR / término en el adapter FTS.
- Mostrar claves usadas / descartadas en el reply RAG y en el payload de `search_normative_docs`.
- Documento de técnicas, FSD/DD/DTP, Demo 4 (`demo-4.md`, `8.4.0-aportes.md`).

## 3. Out-of-Scope

- pgvector / embeddings.
- Ingestión PDF en caliente.
- Nuevo `agent=` o endpoint REST aparte de chat.
- `docs/baseline/`.
- k6/Tempo (ya cubierto por otros integrantes).

## 4. Reglas

| ID | Regla |
|----|-------|
| R1 | Reusar `POST /api/v1/assistant/chat` y la tool existente. |
| R2 | Máximo 3 claves de mayor peso; genéricos no ganan a términos de tesauro. |
| R3 | Sin hits: no fabricar artículos del manual. |

