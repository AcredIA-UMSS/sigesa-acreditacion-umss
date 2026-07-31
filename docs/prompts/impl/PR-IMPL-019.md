---
id: PR-IMPL-019
fsd_uc: FSD-UC-019
dd: DD-UC-019
modulo: MOD-AI
tarea: Backend & Frontend - Diagnóstico Semántico de Fases con IA
estado: Pending
---

# PR-IMPL-019: Contrato de Implementación de Diagnóstico Semántico de Fases (RAG + Caché)

## 1. Propósito y Objetivo

Este documento define las directrices y restricciones estrictas para la codificación del caso de uso **Diagnóstico Semántico de Fases con IA (`FSD-UC-019`)**. Debe ser ejecutado con precisión matemática para garantizar que cualquier agente de codificación backend/frontend genere exactamente la misma arquitectura desacoplada, segura y tolerante a alta concurrencia.

---

## 2. Lo que está REQUERIDO (Especificaciones Técnicas Duras)

### 2.1 Persistencia Directa (Base de Datos Real PostgreSQL)
- **Ejecución DDL Externa**: Todas las tablas y extensiones deben ser creadas ejecutando directamente los scripts SQL en el cliente PostgreSQL real. **Prohibido** usar DDL automático de Hibernate (`spring.jpa.hibernate.ddl-auto=update/create`) o el cargador automático de Flyway en Spring Boot (el cual está desactivado).
- **Esquema de Caché 1:1**:
  ```sql
  CREATE TABLE phase_diagnostic (
      phase_id UUID PRIMARY KEY REFERENCES phase(id) ON DELETE CASCADE,
      result TEXT NOT NULL,
      updated_at TIMESTAMP WITH TIME ZONE NOT NULL
  );
  ```
- **Esquema de Almacén Vectorial RAG (Compartido y Adimensional)**:
  - *Nota*: La carga e ingesta de PDFs está fuera de alcance de esta historia. Se asume base de datos pre-cargada.
  - Para evitar acoplamiento de dimensiones antes de definir el LLM, crear la tabla con columna `VECTOR` adimensional:
  ```sql
  CREATE EXTENSION IF NOT EXISTS vector;
  CREATE TABLE document_embeddings (
      id UUID PRIMARY KEY,
      content TEXT NOT NULL,
      embedding VECTOR, -- Adimensional: acepta 768 (Gemini/Ollama) o 1536 (OpenAI)
      metadata JSONB NOT NULL
  );
  ```

### 2.2 Capa de Dominio y Puertos (Hexagonal)
- **Modelo de Dominio Puro**: `com.umss.sigesa.domain.model.PhaseDiagnostic` debe ser una clase de Java pura. **Sin** anotaciones `@Entity`, `@Table`, `@Id`, ni dependencias de `jakarta.persistence` o Spring. Atributos: `UUID phaseId`, `String result`, `Instant updatedAt`.
- **Puertos de Salida (Output Ports)**:
  - `com.umss.sigesa.application.port.out.PhaseDiagnosticRepositoryPort`:
    ```java
    Optional<PhaseDiagnostic> findByPhaseId(UUID phaseId);
    PhaseDiagnostic save(PhaseDiagnostic diagnostic);
    ```
  - `com.umss.sigesa.application.port.out.ChatModelPort`:
    ```java
    String generateDiagnosis(String systemPrompt, String userPrompt);
    ```
  - `com.umss.sigesa.application.port.out.DocumentRetrievalPort`:
    ```java
    List<String> findRelevantNorms(String criterionCode, int limit);
    ```

### 2.3 Capa de Aplicación (Caso de Uso & Concurrencia)
- **Input Port**: `com.umss.sigesa.application.port.in.DiagnosePhaseUseCase`:
  ```java
  PhaseDiagnostic diagnosePhase(UUID phaseId, String userEmail, List<String> roles);
  ```
- **Clase de Servicio**: `com.umss.sigesa.application.service.DiagnosePhaseService`:
  1.  **Aislamiento y RBAC (FSD-BR-09)**: Consultar el alcance del usuario. Si el actor es `[CC]`, verificar en base de datos si la fase pertenece al `academicProgramId` de su carrera asignada en `UserProgramAssignment`. Si no coincide, lanzar `AccessDeniedException` (debe resultar en `403 Forbidden`).
  2.  **Verificación de Caché (TTL de 15 Minutos)**:
      - Consultar `PhaseDiagnosticRepositoryPort`.
      - Si existe registro y `Instant.now().isBefore(diagnostic.updatedAt().plus(15, ChronoUnit.MINUTES))`, retornar el registro inmediatamente (sin llamar al LLM).
  3.  **Control de Concurrencia (Single-Flight Pattern)**:
      - Usar un `ConcurrentHashMap<UUID, CompletableFuture<PhaseDiagnostic>> activeRequests` estático y privado para rastrear las llamadas en curso.
      - Utilizar `activeRequests.computeIfAbsent(phaseId, id -> CompletableFuture.supplyAsync(() -> executeLlmCall(id)))` para garantizar que exactamente un solo hilo ejecute la llamada pesada al LLM.
      - Al finalizar la ejecución de la llamada (éxito o fallo), remover la clave del mapa en un bloque `finally`.
  4.  **Generación de Contexto y Consulta RAG**:
      - Consultar los datos de la fase en la base de datos (Indicadores en estado `OBSERVADO`, `PENDIENTE` e historiales de observaciones).
      - Ejecutar búsqueda semántica a través de `DocumentRetrievalPort` para los códigos de criterios afectados.
      - Construir los prompts estructurados de acuerdo con `prompt_contract_phase_diagnoser.md`.
      - Invocar a `ChatModelPort` para obtener el reporte.
  5.  **Persistencia**:
      - Guardar el nuevo reporte con `Instant.now()` como `updatedAt` en el repositorio.
      - Retornar el objeto de dominio.

### 2.4 Adaptadores de Infraestructura (REST & OpenAPI)
- **Controller**: `com.umss.sigesa.infrastructure.adapter.rest.PhaseDiagnosticController` exponiendo `POST /api/v1/phases/{phaseId}/diagnose`.
- **Integración SpringDoc**: Incluir anotaciones `@Operation(summary = "...")` y `@ApiResponse(responseCode = "200")` para asegurar que el contrato OpenAPI `/v3/api-docs` contenga la estructura exacta del payload.
- **DTOs & Mappers**: Mapear la entidad JPA del adaptador de persistencia (`PhaseDiagnosticEntity`) y los objetos de API a DTOs puros. Nunca devolver entidades de dominio ni de persistencia directamente en la salida HTTP.
- **Resiliencia (Fallback)**: Si el LLM falla, verificar si hay un diagnóstico previo en base de datos (aunque tenga más de 15 minutos) y retornarlo con el header HTTP `X-Cache-Stale: true`. Si no existe, lanzar una excepción mapeada a `503 Service Unavailable`.

### 2.5 Integración Frontend (React + Orval)
- Ejecutar `pnpm generate:api` para compilar los nuevos hooks de Axios y TanStack Query.
- En la UI, usar el hook generado `useDiagnosePhaseMutation` de React Query.
- Deshabilitar el botón de diagnóstico y mostrar un texto de temporizador de cooldown si la diferencia entre la fecha del cliente y el atributo `updatedAt` del diagnóstico es inferior a 900 segundos.

---

## 3. Lo que está PROHIBIDO (Fuera de Alcance)

- ❌ **No usar base de datos H2** para almacenar ni validar los flujos de RAG/Embeddings. Todos los flujos de prueba local e integración deben conectarse a PostgreSQL real (ej. vía Docker en puerto 5432).
- ❌ **No importar frameworks** (`spring`, `jakarta.persistence`, `hibernate`) en las clases del paquete `com.umss.sigesa.domain` o `com.umss.sigesa.application.port`.
- ❌ **No hacer bypass a la validación de RBAC/Scope**: CC nunca debe poder leer diagnósticos de fases de otra carrera.
- ❌ **No llamar al LLM síncronamente en múltiples hilos paralelos para la misma fase**: la lógica de Single-Flight con `CompletableFuture` o Locks debe estar implementada obligatoriamente para proteger el servidor de sobrecarga.

---

## 4. Plan de Pruebas Unitarias (Reproducibilidad Estricta)

El desarrollo del backend requiere la creación de pruebas unitarias en `src/test` con una cobertura del 100% en la lógica de concurrencia:
- **Test de Caché**: Mockear `PhaseDiagnosticRepositoryPort` para retornar un registro de hace 10 minutos y verificar que `ChatModelPort` nunca sea invocado.
- **Test de Concurrencia (Single-Flight)**:
  - Crear un pool de hilos (`ExecutorService` con 5 hilos).
  - Usar un `CountDownLatch` para sincronizar y disparar 5 solicitudes concurrentes del caso de uso de diagnóstico sobre la misma `phaseId`.
  - Asegurar mediante `verify(chatModelPort, times(1))` que la API de la IA fue llamada **exactamente una vez** y que los 5 hilos recibieron el mismo resultado exacto.
- **Test de Fallback**: Mockear un fallo de la API de IA (lanzando excepción) y asegurar que el sistema retorna la caché antigua con el indicador de stale correspondiente.
