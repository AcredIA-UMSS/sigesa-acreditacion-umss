---
id: DD-UC-004
titulo: "Diseño: Cargar Evidencia (MOD-EVIDENCE)"
producto: "SIGESA"
grupo: "ACREDIA"
fsd_uc:
  - "FSD-UC-004"
prd_refs:
  - "PRD-REQ-005"
  - "PRD-US-005"
  - "PRD-US-025"
adrs:
  - "ADR_002_monolito_modular"
prompts:
  - "PR-IMPL-004"
release: "v1.0"
status: borrador
fecha: "2026-07-10"
autores:
  - "Antigravity AI (Lead Solutions Architect)"
---

# Design Doc `DD-UC-004` — Cargar Evidencia (MOD-EVIDENCE)

> **Qué es**: Documento de diseño técnico exhaustivo para la implementación del Caso de Uso **FSD-UC-004 (Cargar Evidencia)**. Define el flujo de arquitectura hexagonal para la carga de evidencias por parte del Coordinador de Carrera `[CC]`, incluyendo validación, persistencia física append-only de metadatos, almacenamiento de archivos, versionamiento y publicación del evento de carga.
>
> **Relación con otros documentos**:
> - **Trazabilidad obligatoria al FSD**: [`FSD-UC-004`](../product/uc/FSD-UC-004.md).
> - **Gobernanza de Datos**: [`modelo_datos.md`](../product/modelo_datos.md) §3.4.
> - **Reglas de Negocio**: [`reglas_negocio.md`](../product/reglas_negocio.md) (FSD-BR-01, FSD-BR-03, FSD-BR-09, FSD-BR-18).
> - **Contratos de API del Producto**: [`api_contracts.md`](../product/api_contracts.md) (API-EVD-01).

---

## 1. Objetivo y contexto

- **Qué resuelve este feature**: Permite a los Coordinadores de Carrera `[CC]` adjuntar documentos de evidencia para un indicador y criterio específicos del proceso de acreditación de su programa académico. La carga debe ser versionada, inmutable (append-only), y actualizar automáticamente el estado del indicador a `SUBIDO`.
- **Alcance**:
  | Incluido | Excluido (v1.0) |
  |---|---|
  | `POST /api/v1/indicators/{indicatorId}/evidences` para carga de evidencias en formato `multipart/form-data`. | Interfaz de almacenamiento en la nube real (e.g. S3). Usará un adaptador local localizable/stub en el filesystem para v1.0. |
  | Aislamiento estricto por carrera: el `[CC]` solo puede cargar evidencias para indicadores de su propia carrera (`FSD-BR-09`). | Notificaciones de correo activas inmediatas (se delega al outbox asíncrono y audit log stub). |
  | Verificación de tipo y tamaño de archivo (máx 5MB recomendado para evitar uploads síncronos pesados sin progreso en el cliente). | Subsanación de evidencias (`FSD-UC-006` -> API-EVD-05). |
  | Creación automática de la versión 1 de la evidencia y cambio de estado del Indicador a `SUBIDO`. | Portal público u otros flujos de visualización diferida. |

---

## 2. Capa de Dominio (Core)

El dominio no tiene dependencias de Spring Boot ni de JPA.

### Entidades y Agregados
*   `Evidence`: Representa la cabecera lógica de una evidencia.
*   `EvidenceVersion`: Representa el archivo físico versionado con su hash SHA-256.
*   `Indicator`: Consumido desde el dominio de procesos. Debe soportar el cambio de estado a `SUBIDO`.

### Excepciones de Dominio
*   `EvidenceUnclassifiedException`: Si la evidencia no tiene un indicador o criterio asociado (`400 EVIDENCE_UNCLASSIFIED`).
*   `ForbiddenProgramScopeException`: Si el usuario no tiene alcance sobre el programa académico del indicador (`403 FORBIDDEN_SCOPE`).
*   `InvalidFileFormatException`: Si el tipo MIME del archivo no está permitido (`422 INVALID_FILE_FORMAT`).
*   `MaxFileSizeExceededException`: Si el archivo supera el límite permitido (`413 PAYLOAD_TOO_LARGE`).

---

## 3. Puertos (Interfaces de Dominio)

### Inbound Ports (Casos de Uso)
*   `UploadEvidenceUseCase`: Orquesta el proceso de validación, almacenamiento del archivo binario y persistencia de metadatos.

```java
public interface UploadEvidenceUseCase {
    EvidenceResponse upload(UUID indicatorId, UUID criterionId, String description, String filename, byte[] fileBytes, String contentType, AuthenticatedIdentity identity);
}
```

### Outbound Ports (Adaptadores Externos)
*   `EvidenceRepositoryPort`: Persistencia de metadatos de evidencias y versiones.
*   `FileStoragePort`: Guarda el archivo binario y retorna la clave de almacenamiento (`storageKey`) y el hash SHA-256 (`contentHash`).
*   `IndicatorStateHistoryPort`: Permite actualizar el estado del indicador a `SUBIDO` registrando la transición de estados en el historial.

---

## 4. Capa de Aplicación (Servicios de Aplicación)

### Servicio: `UploadEvidenceService` (Implementa `UploadEvidenceUseCase`)

**Flujo Lógico:**
1.  **Validar Permisos**: Recupera la carrera asociada al indicador. Verifica si el `programScope` del usuario autenticado coincide con la carrera (`FSD-BR-09`). Si no, lanza `ForbiddenProgramScopeException`.
2.  **Validar Estado del Indicador**: Verifica que el indicador se encuentre en estado `PENDIENTE` u `OBSERVADO`.
3.  **Almacenar Binario**: Llama a `FileStoragePort.store(filename, fileBytes)`. Este calcula el SHA-256 y guarda el archivo, devolviendo el `storageKey` y `contentHash`.
4.  **Persistir Evidencia**: 
    *   Crea una instancia de `Evidence` y su primera `EvidenceVersion` (versión 1).
    *   Persiste ambas a través de `EvidenceRepositoryPort.save(evidence, version)`.
5.  **Actualizar Estado del Indicador**: Transiciona el indicador a `SUBIDO` e inserta la fila en `indicator_state_history` vía `IndicatorStateHistoryPort`.
6.  **Retornar Respuesta**: Genera un `EvidenceResponse` que incluye el `evidenceId`, `version: 1`, `contentHash` y el nombre del evento `"EvidenceUploaded"`.

---

## 5. Capa de Adaptadores (Infraestructura)

### Adaptador de Entrada (Controlador REST)
*   `EvidenceUploadController`:
    *   `POST /api/v1/indicators/{indicatorId}/evidences`
    *   Consume `multipart/form-data`.
    *   Parámetros: `file` (`MultipartFile`), `criterionId` (`UUID`), `description` (`String`).
    *   Extrae el rol del token de seguridad (exige `[CC]` o `READ_CC_DASHBOARD` / `WRITE_EVIDENCE`).

### Adaptadores de Salida (Persistencia & Almacenamiento)
*   `EvidenceJpaAdapter`: Implementa `EvidenceRepositoryPort`. Mapea a entidades de base de datos `EvidenceEntity` and `EvidenceVersionEntity`.
*   `LocalFileStorageAdapter`: Implementa `FileStoragePort` guardando temporalmente los archivos en el disco local bajo un directorio seguro configurado en el workspace.
*   `IndicatorStateHistoryJpaAdapter`: Registra la transición a `SUBIDO` en la tabla `indicator_state_history`.

---

## 6. Contratos de Datos y DDL

### DTO de Respuesta
```java
public record EvidenceResponse(
    UUID evidenceId,
    int version,
    String contentHash,
    String event
) {}
```

### Esquema DDL Relacionado (PostgreSQL / H2)
```sql
CREATE TABLE evidence (
    id UUID PRIMARY KEY,
    indicator_id UUID NOT NULL,
    latest_version_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE evidence_version (
    id UUID PRIMARY KEY,
    evidence_id UUID NOT NULL REFERENCES evidence(id),
    version_number INT NOT NULL,
    description TEXT,
    storage_key VARCHAR(255) NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    observation_id UUID,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_evidence_version UNIQUE (evidence_id, version_number)
);

CREATE TABLE indicator_state_history (
    id UUID PRIMARY KEY,
    indicator_id UUID NOT NULL,
    previous_state VARCHAR(20),
    new_state VARCHAR(20) NOT NULL,
    actor_id UUID NOT NULL,
    role VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

## 7. Plan de Pruebas

- **Pruebas Unitarias**:
  - `UploadEvidenceServiceTest`: Carga exitosa con metadatos completos, verificación de hash SHA-256, y rechazo de cargas fuera del alcance de la carrera (`FSD-BR-09`).
- **Pruebas de Integración**:
  - `EvidenceUploadControllerIT`: Simulación de petición Multipart POST, retorno de HTTP 201 con payload correcto, e inserción correcta en las tablas de base de datos.
- **Flujos de Excepción**:
  - Carga sin `criterionId` lanza HTTP 400 (`EVIDENCE_UNCLASSIFIED`).
  - Intento de carga por parte de un Técnico `[TD]` o usuario de otra carrera lanza HTTP 403.
