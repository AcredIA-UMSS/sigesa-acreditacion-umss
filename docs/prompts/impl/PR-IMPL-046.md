---
id: PR-IMPL-046
feature_asociado: FEAT-11
fsd_uc:
  - FSD-UC-032
fecha: "2026-10-23"
version: "1.0"
estado: Aprobado
autor: "Alexander Alvarez"
github_issue: "https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/73"
release: "8.3.0"
---

# Prompt Contract — Implementación `PR-IMPL-046`

> **Issue:** [#73 FEAT-11](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/73) — Exportación Asíncrona del Dossier Digital de Acreditación (ZIP Jerárquico y Foliado).  
> **Design / Feature Doc:** [`docs/features/FEAT-11-exportacion-dossier-digital-zip.md`](../../features/FEAT-11-exportacion-dossier-digital-zip.md)  
> **FSD:** `FSD-UC-032` / FSD §10 Transversal  
> **Rama:** `feat/73-dossier-zip-export`

---

## 1. Propósito

Generar y descargar de manera asíncrona un archivo ZIP jerárquico estructurado con el expediente documental completo (Dossier Digital) del proceso de acreditación activo o en auditoría, incluyendo el `00_Indice_Maestro_Cumplimiento.html` navegable, estructura de carpetas por Áreas/Componentes/Criterios/Indicadores y los PDFs/evidencias oficiales foliadas.

## 2. In-Scope

- **Backend:**
  - Puertos de entrada y salida: `ExportProcessDossierUseCase`, `GetProcessDossierExportJobStatusUseCase`, `DossierExportPort`.
  - Servicio de aplicación: `DossierExportService` (concurrencia liberando conexiones DB antes de I/O ZIP).
  - Adaptador de streaming ZIP: `ZipDossierExportStreamingAdapter` (árbol 4 niveles CEUB, HTML maestro de cumplimiento, integración con `EvidenceBlobStoragePort`).
  - Adaptador de seguimiento en memoria de jobs: `InMemoryDossierExportJobTrackerAdapter`.
  - Controladores REST: `POST /api/v1/processes/{id}/export/dossier` (202 ACCEPTED), `GET /api/v1/processes/{id}/export/dossier/jobs/{jobId}`.
  - Cargador de datos de demostración: `DossierDemoDataLoader` para poblar el proceso `DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE`.
  - Pruebas unitarias de arquitectura hexagonal y endpoints web con autenticación JWT.
- **Frontend:**
  - Hook reactivo `useDossierExport` con polling de estado (`QUEUED`, `PROCESSING`, `COMPLETED`, `FAILED`) y descarga de blob sin manual `fetch` ni `any`.
  - Integración en `ProcessDetailView` con botón y feedback de progreso.
  - Pruebas unitarias con Vitest.

## 3. Out-of-Scope

- Modificación de `docs/baseline/`.
- Uso de `any` en TypeScript o dependencias de Spring/JPA en el dominio puro.

## 4. Reglas

| ID | Regla |
|---|---|
| R1 | Arquitectura hexagonal estricta (Separación de Dominio, Aplicación e Infraestructura). |
| R2 | Manejo de I/O asíncrono liberando transacciones JPA antes de comprimir el streaming. |
| R3 | Tipado estricto en frontend con `customFetch` y `resolveAccessToken`. |
| R4 | Suite completa de tests pasando en backend y frontend sin regresiones. |
| R5 | Registro obligatorio en `docs/sprints/sprint_03/PROMPT_MAPPING.md`. |
