# PROMPT_MAPPING — Sprint 02

| ID Mapeo | PR-IMPL | Design Doc | FSD Asociado | Descripción de la Tarea |
| :--- | :--- | :--- | :--- | :--- |
| PM-021 | PR-IMPL-019 | DD-UC-019 | FSD-UC-019 | Diseño de Diagnóstico Semántico de Fases con RAG y Caché |

---

## PM-021

| Campo | Valor |
| --- | --- |
| **ID** | PM-021 |
| **Fecha** | 2026-07-30 |
| **Hora** | 19:09 |
| **Solicitante** | CC / JD |
| **Agente/Entorno** | Antigravity AI Agent |
| **Modelo** | Gemini 3.5 Flash (Medium) |
| **Tarea** | Diseño de Diagnóstico Semántico de Fases con RAG y Caché (FSD-UC-019, DD-UC-019, PR-IMPL-019) |
| **Objetivo** | Diseñar la especificación funcional, plan técnico y contrato de prompt para la asistencia de acreditación asistida por RAG en fase activa. |
| **Contexto** | Implementación de endpoint síncrono POST con caché (15 min cooldown) y single-flight para mitigar sobrecarga de hilos y tokens. |
| **PR-IMPL vinculado** | PR-IMPL-019 |
| **DD-UC vinculado** | DD-UC-019 |
| **FSD-UC vinculado** | FSD-UC-019 |
| **Estado** | completado |

### Prompt usado exacto

```text
analyze the following project, and tell me if I want to include a RAG + langchain architecture to have a some calls for a model, which technologies should I include and which not?
Include a sub story or a new feature using the skills available and also include the design plan as well for all of these, for this pls do it in a new branch.
```

### Entradas auxiliares

- `docs/baseline/05_dti/adrs/ADR_006_postgresql_16.md`
- `docs/baseline/05_dti/adrs/ADR_010_event_driven_choreography.md`
- `docs/product/modelo_datos.md`
- `docs/product/FSD.md`
- `.agents/skills/sigesa-prompt-contract-architect/SKILL.md`

### Archivos generados o modificados

| Acción | Ruta |
| --- | --- |
| generado | `docs/product/uc/FSD-UC-019.md` |
| generado | `docs/design/DD-UC-019.md` |
| generado | `docs/prompts/prompt_contract_phase_diagnoser.md` |
| generado | `docs/prompts/impl/PR-IMPL-019.md` |
| modificado | `docs/product/FSD.md` |
| modificado | `docs/sprints/sprint_02/PROMPT_MAPPING.md` |

### Cambios realizados

- Creación de la especificación funcional `FSD-UC-019.md`.
- Creación del diseño técnico `DD-UC-019.md` detallando arquitectura hexagonal, modelo de base de datos relacional caché (`phase_diagnostic`), endpoint síncrono, y algoritmos de control de concurrencia (cooldown + single-flight).
- Creación de `prompt_contract_phase_diagnoser.md` siguiendo el rol de auditor de acreditación y estructura JSON estricta de entrada/salida.
- Creación de la tarea de implementación `PR-IMPL-019.md`.
- Registro del nuevo caso en el índice de FSD.md y en PROMPT_MAPPING.md de Sprint 02.

### Validación ejecutada

- [x] Verificado el aislamiento y compatibilidad con las directrices relacionales.
- [x] Verificados los enlaces directos y la trazabilidad 1:1 requerida por el AI-SDLC.
