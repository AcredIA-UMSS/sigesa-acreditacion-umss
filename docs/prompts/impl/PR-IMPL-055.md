---
id: PR-IMPL-055
<<<<<<< HEAD
feature_asociado: DD-UC-040
fsd_uc:
  - FSD-UC-040
fecha: "2026-11-15"
version: "1.0"
estado: En Curso
---

# PR-IMPL-055 — Agenda de visita de pares

Implementar FSD-UC-040 / DD-UC-040: V33, GET/PUT, 422 rango, 409 COMPLETED, UI `/procesos/{id}/visita-pares`, Gherkin `@TC-VISIT`. Sin blobs. No `docs/baseline/`.
=======
fsd_uc:
  - FSD-UC-032
  - FSD-UC-015
design_doc: DD-UC-032
fecha: "2026-11-15"
version: "1.0"
estado: Aprobado
autor: "Boris Anthony Angulo Urquieta"
release: "8.4.0"
---

# Prompt Contract — `PR-IMPL-055`

Observación del técnico DUEA por indicador: botón condicionado a evidencia, modal + historial append-only, notificación dual-canal al enviar.

## In-Scope

- Botón [TD] «Registrar observación» solo si el indicador tiene evidencia.
- Modal: texto + recomendación; historial de `process_observation` del indicador.
- `POST …/observations` + `POST …/submit` reutilizados. Filtro `GET ?indicatorId=`.
- Evento `TechnicalObservationIssued` → EMAIL + in-app a [CC] del programa / responsable.
- No cambia el estado del indicador (sigue siendo FSD-UC-008 el rechazo).

## Out-Scope

- `tb_observation` / rechazo de indicador.
- Borrar observaciones.
- Extraer microservicio de notificaciones.

## Validación

- `ProcessObservationServiceTest` verifica enqueue al `submit`.
- Vitest del modal: historial + envío.
>>>>>>> c6bb1d0fb1ef23173c8316b6853bd23eecb5bb44
