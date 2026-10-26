# Demo 2 — AcredIA / SIGESA (`release/8.2.0`)

Registro de funcionalidades por integrante (Classroom + entrega del grupo). El release **8.2.0** reúne los aportes; cada fila incluye el repositorio.

**Repositorio:** [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss)  
**Rama:** `release/8.2.0`

## Tabla de integrantes

| Integrante | Funcionalidades presentadas | Repositorio |
|---|---|---|
| Boris Anthony Angulo Urquieta | Cargar evidencia (FSD-UC-004), visualizar/descargar blob (API-EVD-07b), eliminar/reemplazar si no APROBADO (FSD-UC-005). Contrato OpenAPI (`openapi.json`) + CI drift y Spectral. | [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss) (`release/8.2.0`) |
| Alexander Alvarez | Por registrar en esta rama | [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss) |
| Aylen Gonzáles | Por registrar en esta rama | [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss) |
| Marlene | Validación de formularios de registro (FSD-UC-002). Login institucional (FSD-UC-001). Liveness `GET /api/health`. | [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss) (`release/8.2.0`) |

## Cumplimiento de requisitos (contrato + CI)

| Requisito | Qué se demuestra | Dónde |
|---|---|---|
| Contrato generado desde Spring | Comando reproducible: `scripts/export-openapi.sh` o `cd backend && OPENAPI_WRITE=true ./mvnw -Dtest=OpenApiExportIT test`. Lee springdoc `GET /v3/api-docs` y escribe `openapi.json`. | [`scripts/export-openapi.sh`](../../scripts/export-openapi.sh), [`OpenApiExportIT.java`](../../backend/src/test/java/com/umss/sigesa/adapter/in/web/OpenApiExportIT.java) |
| `openapi.json` versionado | Archivo en la **raíz**. OpenAPI 3.1.0, `info.version` 8.2.0. Incluye evidencias (demo grupo) y, en working tree, `/api/health` (Marlene). | [`openapi.json`](../../openapi.json) |
| Detección de drift | El workflow regenera el JSON (`OPENAPI_WRITE=true`) y **falla** si `git diff --exit-code -- openapi.json`. | [`.github/workflows/openapi.yml`](../../.github/workflows/openapi.yml) |
| Spectral | `npx @stoplight/spectral-cli@6.15.0 lint openapi.json --ruleset .spectral.yml --fail-severity error`. Falla el job si hay **error** según [`.spectral.yml`](../../.spectral.yml). | mismo workflow |
| Ejecución en cada PR | `on.pull_request.branches`: `main` y `release/**` (incluye destino **`release/8.2.0`**). También `push` a esas ramas. | mismo workflow |
| Documentación individual | Esta ficha: funcionalidades Marlene, FSD, criterios, repo. | esta página |
| Registro del release | Manifiesto de grupo. | [`8.2.0-aportes.md`](../../8.2.0-aportes.md) |

## Evidencias en el contrato (subida, eliminación, vista previa)

Ya estaban en **HEAD** de `release/8.2.0` (demo del grupo / Boris). No dependen de `/api/health`.

| Uso | Método y path en `openapi.json` | FSD |
|---|---|---|
| Subida | `POST /api/v1/indicators/{indicatorId}/evidences` | FSD-UC-004 |
| Listado / visor | `GET /api/v1/indicators/{indicatorId}/evidences` | FSD-UC-004 |
| Vista previa / descarga | `GET /api/v1/document-assets/{documentAssetId}/content` | FSD-UC-004 / API-EVD-07b |
| Eliminación (si no APROBADO) | `DELETE /api/v1/evidences/{evidenceId}` (204 / 409 `EVIDENCE_IMMUTABLE`) | FSD-UC-005 |
| Historial | `GET /api/v1/evidences/{evidenceId}/versions` | FSD-UC-005 |

`OpenApiExportIT` comprueba esos paths (más `GET /api/health` cuando el controlador existe).

## ¿`/api/health` está mezclado con la demo?

**Sí, en el working tree local; no en HEAD remoto.**

| Capa | HEAD `origin/release/8.2.0` (demo evidencias) | Cambios locales sin commit |
|---|---|---|
| `openapi.json` | Evidencias documentadas; **sin** `/api/health` | Añade schema `HealthResponse`, path `GET /api/health`, tag Health |
| Código | Sin `HealthController` | `HealthController`, `HealthResponse`, `permitAll` GET `/api/health`, `HealthControllerTest` |
| Docs | Tabla con Marlene «Por registrar» | Fila Marlene + esta sección |

El delta de `openapi.json` **solo** es health (no reescribe los paths de evidencias). Si se presenta solo la demo de evidencias, el contrato committed ya basta. Si Marlene presenta health, hay que **commitear** el overlay; si no, el CI de drift fallará al regenerar springdoc con el controlador nuevo.

## Funcionalidades Marlene (Classroom)

| Funcionalidad | Caso de uso FSD | Criterios de aceptación | OpenAPI |
|---|---|---|---|
| Validación de registro | [FSD-UC-002](../product/uc/FSD-UC-002.md) Gestión de usuarios [JD] | Obligatorios no vacíos; email `@umss.edu.bo`; celular 8 dígitos 6/7; contraseña ≥8 con letras y números; confirmación; [CC]/[EE] con carrera; servidor rechaza inválidos (`RegisterUserRequest`); alta válida crea cuenta INACTIVE | `POST /api/v1/admin/users` |
| Login institucional | [FSD-UC-001](../product/uc/FSD-UC-001.md) | Correo UMSS; 401 genérico si credenciales o dominio inválidos | `POST /api/v1/auth/login` |
| Liveness | NFR/ops (sin FSD de producto) | GET sin token → 200 `{"status":"UP"}`; POST no público; JWT del resto intacto | `GET /api/health` |

Código: `userFormValidation.ts` (+ test), `AddUserModalUI`, `RegisterUserRequest`, `HealthController`, `SecurityConfig`.

## Contrato OpenAPI (Boris) — parámetro 2 Demo 2

El grupo asignó la **infraestructura** del contrato a un integrante (Boris). Cuenta como una de sus funcionalidades, junto con evidencias.

| Ítem | Cómo está cubierto |
|---|---|
| Contrato generado desde el código | Spring + springdoc (`/v3/api-docs`). |
| Archivo versionado | [`openapi.json`](../../openapi.json) en la raíz. Regenerar: [`scripts/export-openapi.sh`](../../scripts/export-openapi.sh). |
| CI en cada PR | [`.github/workflows/openapi.yml`](../../.github/workflows/openapi.yml) (`pull_request` → `main` y `release/**`). |
| Drift | Regenera el JSON y falla si hay diff. |
| Spectral | `--fail-severity error` con [`.spectral.yml`](../../.spectral.yml). |
| oasdiff / Schemathesis | No usados (opcionales). |
