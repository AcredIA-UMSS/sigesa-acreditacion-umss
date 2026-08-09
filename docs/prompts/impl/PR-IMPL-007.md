---
id: PR-IMPL-007
feature_asociado: DD-UC-007
fsd_uc:
  - "FSD-UC-007"
fecha: "2026-08-08"
version: "1.0"
estado: Draft
autor: "AI Prompt Architect (@sigesa-orchestrator)"
skill_origen: sigesa-prompt-contract-architect
---

# Prompt Contract — Implementación `PR-IMPL-007`

## 1. Propósito y Objetivo
Implementar la búsqueda inteligente de evidencias en SIGESA (MOD-EVIDENCE) siguiendo estrictamente el diseño de enrutamiento híbrido definido en `DD-UC-007.md` y el aislamiento por carrera de `FSD-BR-09`.

## 2. Rol y Persona
- **Identidad:** Tech Lead / Full Stack Developer Senior en SIGESA.
- **Expertise:** Java 21, Spring Boot 3.x/4.x, Spring Security (JWT), Arquitectura Hexagonal, React 19, Tailwind CSS, TypeScript, Orval.

## 3. Límites de Alcance

### In-Scope
- **Backend:**
  - Controlador REST: `SearchEvidenceController` con endpoint `GET /api/v1/evidences/search?query=...` que recibe el header `X-AI-Enabled`.
  - DTOs: `SearchQueryResponseDto`, `EvidenceSearchDetailDto`.
  - Puerto de entrada: `SearchEvidenceUseCase`.
  - Servicio: `SearchEvidenceService` resolviendo los 4 escenarios de enrutamiento híbrido (Keyword catalog local, LLM mapping, Refusal out-of-scope, IA desactivada).
  - Puerto de salida persistencia: `SearchEvidenceQueryPort`.
  - Adaptador de persistencia: `SearchEvidenceJpaAdapter` consultando la base de datos uniendo `evidence`, `evidence_version`, `indicator` y `programs`.
  - Mapeo de taxonomía estática para `dimensionName` y `criterionCode` en base al `criterionId` de la base de datos (ya que no existe tabla física de criterios).
  - Puerto de salida IA: `AssistantQueryPort` y adaptador `SearchAssistantAdapter` que realice la llamada al LLM (con un fallback mockeado si falla o si `IA_HABILITADA == false`).
  - Lógica de aislamiento por carrera (`FSD-BR-09`): si el usuario tiene rol `CC`, restringir la consulta a las carreras de su `programScope` obtenido del SecurityContext.
- **Frontend:**
  - Regeneración de la API de Orval.
  - Creación del componente de búsqueda y su correspondiente página visualizando la trazabilidad (`routingPath`, `toolUsed`, `message`) y resultados estructurados.

### Out-of-Scope
- Creación de base de datos vectorial o RAG avanzado para el contenido interno de los PDFs (solo búsqueda de metadatos/descripción/título).
- Creación de tablas adicionales DDL para criterios o dimensiones en la base de datos (se usa mapeo estático).

## 4. Restricciones y Reglas
- No usar imports de Spring/JPA en la capa de dominio o interfaz de casos de uso.
- Controlar el acceso estrictamente según el JWT (Coordinador CC limitado a su alcance de carrera; Tribunal Docente TD y Jefe JD acceso global).
- El enrutamiento híbrido debe comportarse exactamente como lo define el FSD (Escenarios 1, 2, 3 y 4).

## 5. Especificaciones de Interfaz y Datos
### API Response:
```json
{
  "query": "string",
  "routingPath": "KEYWORD | LLM | REFUSAL",
  "toolUsed": "string (nullable)",
  "dataSource": "string",
  "message": "string (nullable)",
  "results": [
    {
      "evidenceId": "UUID",
      "title": "string",
      "description": "string",
      "dimensionName": "string",
      "criterionCode": "string",
      "carreraName": "string",
      "uploadedAt": "LocalDateTime"
    }
  ]
}
```
