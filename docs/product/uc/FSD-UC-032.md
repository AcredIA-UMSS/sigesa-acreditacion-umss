---
id: FSD-UC-032
nombre: Exportación Asíncrona del Dossier Digital de Acreditación (ZIP Jerárquico y Foliado)
estado: Hecho
release: v2.2
actor_principal: "[CC], [TD], [JD], [EE]"
trazabilidad_prd: PRD-US-012
modulo: MOD-EXP
reglas: FSD-BR-32.1, FSD-BR-32.2, FSD-BR-32.3, FSD-BR-32.4, FSD-BR-32.5
ultima_actualizacion: "2026-10-28"
design_doc: DD-UC-032
pr_impl: PR-IMPL-046
---

# FSD-UC-032 — Exportación Asíncrona del Dossier Digital de Acreditación (ZIP Jerárquico y Foliado)

## 1. Contexto

| Campo | Valor |
|---|---|
| **Trazabilidad** | [ADR-0005](../../adr/ADR-0005-workflow-metodologico-evaluacion-transversal.md) · [FSD §10](../FSD.md) · `FEAT-11` (Issue #73) |
| **Design Doc** | [`DD-UC-032`](../../design/DD-UC-032.md) |
| **Relacionados** | `FSD-UC-004` (Gestión Evidencias) · `FSD-UC-019`/`020` (RBAC Procesos) · `FSD-UC-028` (Workflow) |
| **Precondiciones** | Proceso existente en `ACTIVE` o `AUDIT_READONLY`; usuario autenticado con rol `JD`, `TD`, `EE` o `CC` (asignado a la carrera del proceso). |
| **Pantalla** | `/procesos/{processId}` — Botón cabecera «Exportar Dossier Completo (ZIP)». |

En los procesos de acreditación universitaria ante el **CEUB** y el sistema **ARCU-SUR**, la entrega del **Expediente de Acreditación (Dossier Digital)** es el hito cumbre obligatorio en la transición entre la autoevaluación interna y la auditoría externa de pares evaluadores.

El caso de uso permite empaquetar de forma automatizada y asíncrona todos los documentos y evidencias organizados en la estructura jerárquica normativa del proceso, garantizando integridad criptográfica y entrega offline.

---

## 2. Reglas de Negocio (Business Rules)

- **FSD-BR-32.1 (Zero Disco Local & Streaming a Storage):** La generación del ZIP se realiza en streaming directo hacia el object storage (S3/MinIO) o sistema de almacenamiento configurado (`EvidenceBlobStoragePort`), sin escribir archivos temporales en el disco efímero del contenedor (`Files.createTempFile` prohibido) ni cargar el ZIP completo en el heap de la JVM (`Files.readAllBytes` prohibido).
- **FSD-BR-32.2 (Límites de Concurrencia y Backpressure):** 
  - El sistema cuenta con un semáforo global de exportaciones simultáneas (máximo 4–8 concurrentes). Si el semáforo está colmado, la API rechaza la solicitud inmediatamente con `429 Too Many Requests` (o `503 Service Unavailable`) indicando cabecera `Retry-After: 60`.
  - Límite por usuario: máximo 1 exportación activa (`PENDING` o `PROCESSING`) por usuario simultáneamente. Solicitudes adicionales devuelven `409 Conflict`.
- **FSD-BR-32.3 (TTL y Purga Automática):** Los archivos ZIP generados se almacenan con prefijo temporal `dossier-exports/` y cuentan con un tiempo de vida (TTL) de 24 a 48 horas. Una tarea periódica programada y limpieza al arranque eliminan del bucket los objetos expirados.
- **FSD-BR-32.4 (Control RBAC por Carrera para Coordinador):** Los roles `JD` y `TD` pueden exportar cualquier proceso. El rol `CC` **únicamente** puede solicitar y descargar exportaciones de procesos pertenecientes a las carreras asignadas a su usuario en `user_program_assignment`. Si intenta exportar otra carrera, el sistema responderá `404 PROCESS_NOT_FOUND` (ocultamiento seguro).
- **FSD-BR-32.5 (Lectura Paralela Acotada de Evidencias):** La recuperación de evidencias desde S3 hacia el empaquetador ZIP se ejecuta en paralelo con concurrencia acotada (semáforo de 8 a 16 hilos) para no saturar el pool HTTP del cliente ni el bucket, volcando cada stream/buffer directamente al `ZipOutputStream`.
- **FSD-BR-32.6 (Timeout de Ejecución):** Cada job de exportación tiene un timeout estricto de 10 minutos (FEAT-11 / F12-09). Al superarse, el job se marca como `FAILED`, liberando hilos y semáforos.
- **FSD-BR-32.7 (Tolerancia a Blobs Faltantes):** Si un archivo de evidencia no existe en el storage, no se aborta la exportación; se incorpora en su ruta un archivo de texto descriptivo `[FOLIO]_ERROR_DOCUMENTO_NO_DISPONIBLE.txt`.

---

## 3. Flujo Funcional Asíncrono

### 3.1 Solicitud de Exportación
1. El usuario autorizado pulsa «Exportar Dossier Completo (ZIP)».
2. El frontend realiza `POST /api/v1/processes/{processId}/dossier/export`.
3. El backend valida permisos de rol y pertenencia de carrera (para `CC`).
4. Se verifica el límite de exportaciones activas por usuario y disponibilidad en el semáforo global.
5. Se crea el registro `ReportExportJob` en estado `PENDING` y se encola la tarea asíncrona en el pool `reportJobExecutor`.
6. El backend responde `202 ACCEPTED` en < 200 ms con:
   ```json
   {
     "jobId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
     "status": "PENDING",
     "message": "Dossier export has started in the background.",
     "statusUrl": "/api/v1/processes/{processId}/dossier/export-jobs/{jobId}"
   }
   ```

### 3.2 Sondeo de Estado (Polling)
1. El frontend consulta periódicamente `GET /api/v1/processes/{processId}/dossier/export-jobs/{jobId}` (cada 1–2 segundos).
2. El backend responde con el progreso porcentual (`0..100%`) y estado (`PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`).
3. Al alcanzar estado `COMPLETED`, la respuesta incluye el campo `downloadUrl`:
   - Si el almacenamiento configurado es S3 (`sigesa.evidence.storage=s3`), `downloadUrl` contiene una **URL firmada de S3 (Presigned GET URL)** con expiración corta (15 minutos).
   - Si es almacenamiento local, `downloadUrl` contiene la ruta del controlador `/api/v1/processes/{processId}/dossier/export-jobs/{jobId}/download`.

### 3.3 Descarga del Archivo ZIP
1. Con URL firmada S3: el navegador o cliente descarga el binario directamente desde el bucket S3 sin pasar por los servidores ni saturar los hilos de Tomcat/Spring Boot.
2. Con fallback backend: el endpoint de descarga genera un stream HTTP con cabecera `Content-Disposition: attachment; filename="Dossier_...zip"`.

---

## 4. Estructura Jerárquica del ZIP Foliado

El archivo generado adopta el formato (sin prefijo en producción):
`[Carrera]_[CodigoProceso]_[Fecha].zip`

```text
[Carrera]_[CodigoProceso]_[Fecha].zip
├── 00_Indice_Maestro_Cumplimiento.html  (Índice responsivo con hipervínculos locales y métricas)
├── 00_Resumen_Ejecutivo.txt             (Resumen ejecutivo formal)
├── 00_Manifiest_Checksums.txt           (Hashes SHA-256 de todas las evidencias para auditoría)
├── Dimension_1_[NombreArea]/
│   └── Componente_1.1_[NombreVariable]/
│       └── Criterio_1.1.1_[NombreSubvariable]/
│           └── Indicador_1.1.1.1_[CodigoIndicador]/
│               ├── [FOLIO]_[NombreArchivoOriginal].pdf
│               └── [FOLIO]_[NombreArchivoOriginal].jpg
└── Dimension_2_...
```

---

## 5. Excepciones y Respuestas de Error

| Código | Condición | Causa / Mensaje |
|---|---|---|
| `401` | No autenticado | Token JWT ausente o inválido. |
| `403` | Rol no autorizado | Usuario sin rol `JD`, `TD`, `CC` o `EE`. |
| `404` | Proceso no encontrado / fuera de alcance | Proceso inexistente o usuario `CC` intentando acceder a otra carrera. |
| `409` | Exportación activa duplicada | Usuario ya posee una exportación en estado `PENDING` o `PROCESSING`. |
| `429` | Concurrencia máxima alcanzada | Semáforo global colmado. Devuelve cabecera `Retry-After: 60`. |
| `500` | Fallo interno de job | Registrado en `ReportExportJob.errorMessage` como estado `FAILED`. |

---

## 6. Criterios de Aceptación

- [ ] Solicitud de exportación devuelve `202 ACCEPTED` con `jobId` en < 200 ms.
- [ ] No se crean archivos temporales en el sistema de archivos del contenedor (`./data` o `/tmp`).
- [ ] El consumo de heap durante la compresión se mantiene acotado independientemente del tamaño total del ZIP (mediante buffers de streaming y partes multipart).
- [ ] Concurrencia de descarga desde S3 limitada a 8–16 peticiones simultáneas por job.
- [ ] Límite global de jobs concurrentes (devuelve 429 cuando el semáforo se agota).
- [ ] Los jobs que excedan 10 minutos se cancelan y marcan como `FAILED`.
- [ ] Cuando `sigesa.evidence.storage=s3`, el cliente recibe una URL prefirmada de descarga con expiración de 15 minutos.
- [ ] Los archivos ZIP en el bucket caducan y son eliminados automáticamente tras 24–48 horas de retención.
- [ ] El rol `CC` solo puede exportar procesos de sus carreras asignadas.
- [ ] Cobertura de pruebas unitarias y de integración > 90%.
