# FEAT-11: Exportación Asíncrona del Dossier Digital de Acreditación (ZIP Jerárquico y Foliado)

- **ID Issue:** [#73](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/73)
- **Trazabilidad Funcional:** `FSD-UC-032` · Transversal FSD §10 · `PRD-US-012` (Evolución) · [ADR-0005](../adr/ADR-0005-workflow-metodologico-evaluacion-transversal.md)
- **Módulo:** `MOD-EXP` / `MOD-EVIDENCE`
- **Release:** 8.3.0 / v2.2
- **Responsable:** Alexander Alvarez (`Alex553-bot`)

---

## 1. Contexto y Problema de Negocio (Pain Point)

En los procesos de acreditación universitaria ante el **CEUB** y el sistema **ARCU-SUR**, la entrega del **Expediente de Acreditación (Dossier Digital)** es el hito cumbre obligatorio en la transición entre la autoevaluación interna y la auditoría de los comités externos (Etapa 6: Sala de Pares / Preparación Externa).

### Dolores de los Actores:
- **Coordinador de Carrera (`[CC]`) y Comisión de Autoevaluación (`CAE`):** Al concluir la carga de evidencias (120–220 indicadores con 300–600 documentos en total), se enfrentan a un proceso manual agotador. En el sistema actual (`release/8.2.0`), las evidencias se almacenan en S3/Silo y solo pueden consultarse o descargarse **una por una** a través del navegador. Descargar manualmente cientos de archivos, crear carpetas en el sistema operativo, renombrar los PDFs con el código de folio institucional y compilar un índice maestro consume entre **1 y 2 semanas de trabajo de secretaría y docentes**, con un riesgo crítico de omitir folios o alterar la estructura exigida por la guía evaluadora.
- **Pares Evaluadores Externos (`[EE]`):** Expertos nacionales e internacionales que disponen de únicamente 3 días de visita técnica in-situ. Si la conectividad de la sala es deficiente o si no disponen de una carpeta digital estandarizada fuera de línea con un índice maestro navegable, la revisión se dilata, aumentando el riesgo de observaciones punitivas.
- **Técnico DUEA (`[TD]`) y Jefatura DUEA (`[JD]`):** Requieren validar la completitud formal del dossier y archivar una copia inmutable del expediente previo al congelamiento del proceso (`AUDIT_READONLY`).

---

## 2. Requisitos de Arquitectura y Auditoría Técnica (12-Factor & Concurrencia)

| Dimensión | Factor / Regla | Impacto y Solución Arquitectónica |
|---|---|---|
| **Concurrencia** | `CON-02` / `CON-03` | **Liberación Inmediata de Conexiones HikariCP:** Prohibido mantener conexiones transaccionales a PostgreSQL abiertas durante el streaming o compresión del archivo ZIP. La consulta de metadatos del árbol normativo y evidencias se ejecuta en un snapshot rápido ($W_{db} < 50\text{ ms}$). Los streams I/O de lectura desde MinIO/S3 y compresión `ZipOutputStream` operan con conexión JPA cerrada. |
| **Concurrencia** | `CON-04` | **I/O Asíncrono no Bloqueante con Sondeo:** El cliente web no ejecuta una llamada HTTP síncrona que mantenga el hilo Tomcat en espera. El endpoint `POST /api/v1/processes/{id}/dossier/export` encola un `ReportExportJob` y responde `202 ACCEPTED` con un `jobId`. El frontend consulta el progreso (`GET /job/{jobId}`) y descarga el binario al completarse. |
| **12-Factor** | `F12-03` | **Procesos Stateless y Multi-Instancia:** El empaquetado del ZIP genera un blob temporal en el bucket S3 (`/dossier-exports/{jobId}.zip`) con URL de expiración o entrega a través del adapter, sin guardar estado local volátil en el disco efímero del contenedor. |
| **12-Factor** | `F12-09` | **Disposabilidad y Control de Timeouts:** Los streams de lectura S3 y generación del ZIP tienen un timeout máximo de 10 minutos. Si el usuario cancela o el cliente se desconecta, el hilo de empaquetado finaliza limpiamente liberando memoria. |
| **Arquitectura** | **Hexagonal Estricta** | El núcleo de dominio no tiene dependencias de bibliotecas de compresión ni de Spring. La lógica del árbol vive en el puerto de aplicación (`application.port.in.ExportProcessDossierUseCase`) y la infraestructura en adaptadores dedicados (`ZipDossierExportStreamingAdapter`). |

---

## 3. Alcance de Implementación

### Backend (Java 21 / Spring Boot 4.x)
1. **Nuevo Caso de Uso:** `ExportProcessDossierUseCase` y `GetDossierExportJobStatusUseCase`.
2. **Estructura del Archivo ZIP:**
   ```text
   Dossier_[Carrera]_[CodigoProceso]_[Fecha].zip
   ├── 00_Indice_Maestro_Cumplimiento.html  (Índice navegable con vínculos relativos locales)
   ├── 00_Indice_Maestro_Cumplimiento.pdf   (Resumen ejecutivo formal)
   ├── Dimension_1_[NombreArea]/
   │   └── Componente_1.1_[NombreVariable]/
   │       └── Criterio_1.1.1_[NombreSubvariable]/
   │           └── Indicador_1.1.1.1_[CodigoIndicador]/
   │               ├── [FOLIO]_[NombreArchivoOriginal].pdf
   │               └── [FOLIO]_[NombreArchivoOriginal].jpg
   └── Dimension_2_...
   ```
3. **Mapeo de Evidencias y Folios:**
   - Cada evidencia vinculada (`IndicatorEvidenceLink` $\rightarrow$ `DocumentAsset`) se ubica en su carpeta respectiva.
   - Si un documento está vinculado a múltiples indicadores (relación $M:N$ de M7), se empaqueta en cada carpeta correspondiente con su prefijo de folio oficial `DocumentAsset.folioCode`.
4. **Seguridad y Control de Acceso:**
   - Accesible para roles `ROLE_JD`, `ROLE_TD`, `ROLE_CC` (solo para su carrera) y `ROLE_EE` (en modo `AUDIT_READONLY`).

### Frontend (React 19 / TypeScript estricto)
1. **Modal / Acción en Proceso:** Botón «Exportar Dossier Completo (ZIP)» en la cabecera del detalle del proceso (`/procesos/{id}`).
2. **Sondeo Reactivo:** Barra de progreso con estado `QUEUED`, `PROCESSING`, `COMPLETED` y `FAILED`.
3. **Descarga Directa:** Trigger nativo de descarga mediante blob seguro sin abrir nuevas pestañas huérfanas.

---

## 4. Casos Borde y Reglas de Negocio Identificadas

1. **Evidencias en Indicador PENDIENTE o no Aprobado:**
   - Si el proceso está en `ACTIVE`, el exportador incluye todas las evidencias subidas marcando en el índice su estado (`APROBADO`, `SUBIDO`, `OBSERVADO`).
   - Si el proceso está en `AUDIT_READONLY` (Sala de Pares), solo se incluyen evidencias activas no revocadas.
2. **Blobs Huérfanos o Inexistentes en S3:**
   - Si un `DocumentAsset` tiene un `s3Key` que no existe en el storage (archivo faltante), el empaquetador no aborta todo el ZIP de 500 archivos; escribe un archivo de texto advertencia `[FOLIO]_ERROR_DOCUMENTO_NO_DISPONIBLE.txt` y lo reporta en el índice maestro.
3. **Caracteres Especiales en Nombres de Carpetas:**
   - Sanitización de nombres de carpetas normativas (eliminar acentos, barras `/`, `\`, `:` y caracteres reservados en sistemas de archivos Windows/Linux).
4. **Procesos con 0 Evidencias:**
   - Genera el ZIP con la estructura completa de carpetas vacías y el índice maestro indicando 0% de cumplimiento.
5. **Nombres de Archivos Duplicados en un Mismo Indicador:**
   - Si dos evidencias se llaman igual (ej. `resolucion.pdf`), el prefijo de folio digital obligatorio garantiza unicidad: `FOLIO-0012_resolucion.pdf`, `FOLIO-0034_resolucion.pdf`.

---

## 5. Criterios de Aceptación

- [ ] Un usuario autorizado (`CC`, `TD`, `JD`, `EE`) solicita la exportación del proceso y recibe un `jobId` en menos de 200 ms.
- [ ] La compresión del ZIP no bloquea transacciones de base de datos ni satura el pool Hikari.
- [ ] La jerarquía de carpetas en el ZIP coincide al 100% con el árbol normativo del proceso (N1 $\rightarrow$ N2 $\rightarrow$ N3 $\rightarrow$ Indicador).
- [ ] El índice maestro HTML contiene enlaces relativos que abren los documentos localmente sin necesidad de internet.
- [ ] Las pruebas unitarias y de integración del servicio de exportación y controlador superan el 90% de cobertura de código.
