# Demo 2 — AcredIA / SIGESA (`release/8.2.0`)

Registro de funcionalidades por integrante (Classroom + entrega del grupo). El release **8.2.0** reúne los aportes; cada fila incluye el repositorio individual o el del grupo.

## Tabla de integrantes

| Integrante | Funcionalidades presentadas | Repositorio |
|---|---|---|
| Boris Anthony Angulo Urquieta | Cargar evidencia (FSD-UC-004), visualizar/descargar blob (API-EVD-07b), eliminar/reemplazar si no APROBADO (FSD-UC-005). Contrato OpenAPI (`openapi.json`) + CI drift y Spectral. | [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss) (`release/8.2.0`) |
| Alexander Alvarez | Por registrar en esta rama | [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss) |
| Aylen Gonzáles | Por registrar en esta rama | [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss) |
| Marlene | Por registrar en esta rama | [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss) |

Copiar la misma tabla a Classroom. Completar las filas pendientes **antes** de la demo.

## Contrato OpenAPI (Boris) — parámetro 2 Demo 2

El grupo asignó esta parte a **un integrante** (Boris). Cuenta como una de sus funcionalidades, junto con evidencias.

| Ítem | Cómo está cubierto |
|---|---|
| Contrato generado desde el código | Spring + springdoc (`/v3/api-docs`). Anotaciones `@Operation` / `@ApiResponse` en controladores. |
| Archivo versionado | [`openapi.json`](../../openapi.json) en la raíz. Regenerar: [`scripts/export-openapi.sh`](../../scripts/export-openapi.sh) (`OPENAPI_WRITE=true ./mvnw -Dtest=OpenApiExportIT test`). |
| CI en cada PR | [`.github/workflows/openapi.yml`](../../.github/workflows/openapi.yml) (`on: pull_request` y `push` a `main` / `release/**`). |
| Drift | Regenera el JSON y falla si `git diff --exit-code -- openapi.json`. |
| Spectral | `npx @stoplight/spectral-cli` con [`.spectral.yml`](../../.spectral.yml). |
| oasdiff / Schemathesis | No usados (opcionales). |

Endpoints de las funcionalidades presentadas:

- `POST /api/v1/indicators/{indicatorId}/evidences` — cargar
- `GET /api/v1/indicators/{indicatorId}/evidences` — listar (visor)
- `GET /api/v1/document-assets/{documentAssetId}/content` — previsualizar / descargar blob (Silo/S3)
- `DELETE /api/v1/evidences/{evidenceId}` — 204 si no APROBADO; 409 `EVIDENCE_IMMUTABLE` si APROBADO
- `GET /api/v1/evidences/{evidenceId}/versions` — historial (FSD-UC-005)

## Entregable de grupo

- Rama: `release/8.2.0`
- Manifiesto: [`8.2.0-aportes.md`](../../8.2.0-aportes.md)
