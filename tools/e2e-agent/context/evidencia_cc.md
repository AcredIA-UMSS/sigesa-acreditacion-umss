# Sección: Evidencia — CC sube archivo

## Rutas

- Carga global: `/evidencias/cargar` — H1 «Cargar Evidencia», selects «Proceso» e «Indicador normativo», textarea «DESCRIPCIÓN», input file «ARCHIVO DE EVIDENCIA», botón «Subir evidencia»
- Sidebar CC: «CARGAR EVIDENCIA»
- Desde proceso: detalle `/procesos/{uuid}` → expandir árbol → enlace/botón «Subir evidencia» (modal)

## Seed dev (backend)

- CC: `cc@umss.edu.bo` / `CoordDemo2026!` — carrera **Ingeniería de Sistemas** (INF-SIS)
- Proceso ACTIVE seed: UUID `950e8400-e29b-41d4-a716-446655440020`
- Link listado: `getByRole('link', { name: /Ver detalle del proceso Ingeniería de Sistemas/i })`

## Fixture

- `frontend/tests/fixtures/evidencia-e2e.pdf` + helper `tests/agente/helpers/evidenceFixture.ts` (`mimeType: application/pdf`)
- Backend acepta `application/octet-stream` si el nombre termina en `.pdf` (desde release con fix en `UploadNormativeIndicatorEvidenceService`)

## Diagnóstico upload 401

1. Reconstruir backend: `docker compose up -d --build backend`
2. Script API: `bash tools/e2e-agent/scripts/verify-evidence-upload.sh` (esperado HTTP 201)
3. E2E bloquea `/api/v1/assistant/**` para evitar logout en cascada; upload usa `skipUnauthorizedLogout` en `normativeIndicatorEvidenceApi.ts`

## File input en Playwright

- Preferir `evidencePdfUpload()`; alternativa `getByLabel(/Seleccionar archivo/i)` en zona de drop

## Login CC

- Tras login suele ir a dashboard CC (no `/admin/users` como JD)
- Contraseña: `getByRole('textbox', { name: 'Contraseña' })`

## Datos únicos

- Descripción: `E2E evidencia-${Date.now()}` para trazabilidad

## Precondiciones

- CC debe ver al menos un proceso ACTIVE de su carrera
- Indicador normativo v2 en el proceso (seed normativo)
