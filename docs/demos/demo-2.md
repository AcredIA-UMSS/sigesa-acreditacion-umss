# Demo 2 — AcredIA / SIGESA (`release/8.2.0`)

Registro de funcionalidades por integrante (Classroom + entrega del grupo). El release **8.2.0** reúne los aportes; cada fila incluye el repositorio individual o el del grupo.

## Tabla de integrantes

| Integrante | Funcionalidades presentadas | Repositorio |
|---|---|---|
| Boris Anthony Angulo Urquieta | Cargar evidencia en indicador (FSD-UC-004) y eliminar/reemplazar si el indicador no está APROBADO (FSD-UC-005). Contrato OpenAPI generado desde Spring (`openapi.json`) + CI de drift y Spectral. | [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss) (`release/8.2.0`) |
| Alexander Alvarez | Por registrar en esta rama | [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss) |
| Aylen Gonzáles | Por registrar en esta rama | [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss) |
| Marlene | Por registrar en esta rama | [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss) |

Copiar la misma tabla a Classroom. Completar las filas pendientes **antes** de la demo.

## Contrato OpenAPI (Boris)

- Fuente: anotaciones springdoc / OpenAPI 3 en controladores Spring (`/v3/api-docs`).
- Artefacto versionado: [`openapi.json`](../../openapi.json) en la raíz del repo.
- Regenerar: `scripts/export-openapi.sh` (o `OPENAPI_WRITE=true ./mvnw -Dtest=OpenApiExportIT test` en `backend/`).
- CI: [`.github/workflows/openapi.yml`](../../.github/workflows/openapi.yml) en cada PR:
  1. Genera el JSON desde el código.
  2. Falla si hay **drift** (`git diff --exit-code -- openapi.json`).
  3. Pasa **Spectral** (`.spectral.yml`).
- Endpoints de la demo de evidencias en el contrato:
  - `POST /api/v1/indicators/{indicatorId}/evidences`
  - `GET /api/v1/indicators/{indicatorId}/evidences`
  - `DELETE /api/v1/evidences/{evidenceId}` (204 si no APROBADO; 409 `EVIDENCE_IMMUTABLE` si APROBADO)
  - `GET /api/v1/evidences/{evidenceId}/versions`

## Entregable de grupo

- Rama: `release/8.2.0`
- Manifiesto: [`8.2.0-aportes.md`](../../8.2.0-aportes.md)
