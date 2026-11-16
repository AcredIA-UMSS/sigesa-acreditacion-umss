---
id: DD-UC-039
titulo: "Matriz de cobertura / informe de autoevaluación"
fsd_uc: FSD-UC-039
modulo: MOD-EVAL
---

# DD-UC-039

## Decisión

Informe **síncrono** (JSON + PDF) sobre el árbol ya persistido. No reusa el job de reportes asíncronos (UC-014) ni el ZIP del dossier.

## API

| ID | Método | Ruta | Roles |
|----|--------|------|-------|
| API-COV-01 | GET | `/api/v1/processes/{processId}/coverage-matrix` | CC, TD, JD, EE, CAE_MEMBER (alcance de carrera) |
| API-COV-02 | GET | `/api/v1/processes/{processId}/coverage-matrix/pdf` | mismos |

**200 JSON:** `{ processId, careerName, generatedAt, totals{indicators, withEvidence, approved, observed, other, coveragePercent}, dimensions[{id,name,components[{id,name,criteria[{id,name,indicators[{id,code,description,status,evidenceCount}]}]}]}] }`

**200 PDF:** `application/pdf` + `Content-Disposition: attachment; filename="matriz-cobertura-{processId}.pdf"`

**404** `PROCESS_NOT_FOUND` si no existe o el rol no tiene alcance (mismo patrón UC-030).

## Dominio

`CoverageMatrix` record puro. Ensamblado en aplicación. PDF en adaptador OpenPDF (A4, tabla código/criterio/estado/evidencias). Hexagonal: puerto `CoverageMatrixQueryPort` + `CoverageMatrixPdfPort`.

## Fuera

ZIP, MinIO, dictamen EE, semáforo.
