---
id: DD-UC-040
titulo: "Agenda de visita de pares"
fsd_uc: FSD-UC-040
modulo: MOD-WORKFLOW
---

# DD-UC-040

## Decisión

Una visita por proceso (`uk_peer_visit_process`). Ítems hijos (entrevistas, recintos, documentos de la visita, sesiones). **Sin** blobs: el documento de visita es un ítem de checklist (`DOCUMENT`), no un `DocumentAsset`.

## Modelo

`peer_visit`: id, process_id UK, starts_on, ends_on, venue, room, status (`DRAFT|SCHEDULED|IN_PROGRESS|COMPLETED|CANCELLED`), notes, created_by, timestamps.

`peer_visit_item`: id, visit_id, item_type (`INTERVIEW|FACILITY|DOCUMENT|SESSION`), title, scheduled_at nullable, location, notes, done, item_order.

Flyway **V33**.

## API

| ID | Método | Ruta | Roles |
|----|--------|------|-------|
| API-VISIT-01 | GET | `/api/v1/processes/{processId}/peer-visit` | CC, TD, JD, EE |
| API-VISIT-02 | PUT | `/api/v1/processes/{processId}/peer-visit` | TD, JD |

GET `200` `{ processId, careerName, visit: null \| PeerVisit }`.

PUT upsert. **422** `INVALID_DATE_RANGE` si `endsOn < startsOn`. **409** `VISIT_IMMUTABLE` si status persistido es `COMPLETED`. **404** alcance.

## Fuera

Freeze/checklist UC-030, carga MinIO, dictamen UC-041.
