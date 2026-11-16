---
id: PR-IMPL-057
feature_asociado: DD-UC-040
fsd_uc:
  - FSD-UC-040
fecha: "2026-11-16"
version: "1.0"
estado: Implementado
---

# PR-IMPL-057 — Agenda de visita de pares

Implementar FSD-UC-040 / DD-UC-040: agenda de visita por proceso con ítems, persistencia (V33), alcance por carrera y UI `/procesos/{id}/visita-pares`. Sin blobs MinIO. No `docs/baseline/`.

## Alcance implementado
- `GET/PUT /api/v1/processes/{processId}/peer-visit` (lectura CC/TD/JD/EE, escritura TD/JD).
- Validaciones: `422 INVALID_DATE_RANGE`, `409 VISIT_IMMUTABLE`, `404 PROCESS_NOT_FOUND` fuera de alcance/inexistente.
- Dominio, puertos, servicios (`workflow`), adapters JPA/web y vista frontend editable para TD/JD con bloqueo en `COMPLETED`.

## Pruebas
- Servicios: `GetPeerVisitServiceTest`, `UpsertPeerVisitServiceTest`.
- Integración Gherkin `@TC-VISIT`: programación + lectura EE, rango inválido, visita inmutable, CC/EE sin escritura, proceso inexistente 404.
- Frontend: `PeerVisitAgendaView.test.tsx`.