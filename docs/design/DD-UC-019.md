---
id: DD-UC-019
titulo: "Diseño Técnico: Diagnóstico Semántico de Fases con IA"
producto: "SIGESA"
grupo: "ACREDIA"
fsd_uc:
  - "FSD-UC-019"
prd_refs:
  - "PRD-REQ-019"
  - "PRD-US-019"
adrs:
  - "ADR_006_postgresql_16"
  - "ADR_010_event_driven_choreography"
prompts:
  - "PR-IMPL-019"
release: "v1.0"
status: borrador
fecha: "2026-07-30"
autores:
  - "Design Product Owner & Lead Solutions Architect"
---

# Design Doc `DD-UC-019` — Diagnóstico Semántico de Fases con IA (RAG)

> **Qué es**: Documento de diseño técnico para la implementación del diagnóstico de cuellos de botella semánticos a nivel de fase. Define la integración con LangChain4j, la base de datos de soporte relacional para caché y los mecanismos de control de concurrencia.
>
> **Relación con otros documentos**:
> - **Trazabilidad funcional**: [`FSD-UC-019`](../product/uc/FSD-UC-019.md).
> - **Modelo de Datos**: [`modelo_datos.md`](../product/modelo_datos.md).
> - **ADR Base**: [`ADR_006_postgresql_16`](../baseline/05_dti/adrs/ADR_006_postgresql_16.md) (para base de datos y pgvector).

---

## 1. Objetivo y contexto

Este feature permite a los usuarios con rol de Coordinador de Carrera (`[CC]`) y Jefatura (`[JD]`) obtener un análisis semántico detallado de los impedimentos que bloquean el avance de una fase del proceso de acreditación. Utiliza un modelo de lenguaje (LLM) enriquecido con el contexto operativo de la base de datos y los estándares documentales (RAG) de CEUB y ARCUSUR.

---

## 2. Diseño de Persistencia (Base de Datos)

### 2.1 Tabla de Caché Relacional (Base de Datos Real PostgreSQL)
Para evitar el engrosamiento ("bloat") de la tabla core `phase` y seguir las buenas prácticas de normalización, se implementa una tabla separada de caché relacional con relación **1:1**. 

*Nota de Gobernanza*: Las modificaciones DDL se aplican **directamente sobre la base de datos PostgreSQL real** (ej. cliente externo / psql), dado que el ejecutor interno de Flyway en Spring Boot está explícitamente desactivado (`spring.flyway.enabled=false`) en [application.yaml](../backend/src/main/resources/application.yaml#L28) para mantener el control determinista sobre el esquema.

```sql
-- DDL a ejecutar directamente en PostgreSQL
CREATE TABLE phase_diagnostic (
    phase_id UUID PRIMARY KEY,
    result TEXT NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_phase_diagnostic_phase FOREIGN KEY (phase_id) REFERENCES phase(id) ON DELETE CASCADE
);
```

### 2.2 Almacén de Vectores para RAG (Compartido para otras Historias)
Para dar soporte de búsqueda semántica (RAG) en esta y futuras historias de IA (ej. chatbot, procesamiento de plantillas, auditoría asistida), se expone el almacén de vectores utilizando la extensión `pgvector` en la misma instancia de PostgreSQL real.

*Nota de Gobernanza e Ingesta*: La ingesta, división y carga de PDFs en esta tabla está **fuera del alcance** de la implementación de `FSD-UC-019` y será abordada en otra historia específica. Se asume que el almacén de vectores ya estará pre-cargado para las consultas síncronas de RAG.

Para evitar acoplamiento con un proveedor de IA no definido aún, la columna de embeddings se declara como **adimensional (dimensionless)** en PostgreSQL (soportado desde `pgvector` v0.5.0+). Esto permite almacenar vectores de cualquier dimensión (ej. 768 para Gemini/Ollama, 1536 para OpenAI) de manera flexible:

```sql
-- DDL para habilitar pgvector y crear el almacén de embeddings flexible
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE document_embeddings (
    id UUID PRIMARY KEY,
    content TEXT NOT NULL,
    embedding VECTOR, -- Columna adimensional: acepta cualquier dimensión de modelo de embeddings
    metadata JSONB NOT NULL
);

-- Nota: Si se requiere crear el índice HNSW de optimización de inmediato,
-- se debe especificar la dimensión o usar la dimensión detectada tras la primera inserción:
-- Ejemplo (si se define Gemini/Ollama como 768):
-- ALTER TABLE document_embeddings ALTER COLUMN embedding TYPE vector(768);
-- CREATE INDEX ON document_embeddings USING hnsw (embedding vector_cosine_ops);
```

Este almacén es compartido. Cualquier otro caso de uso de RAG puede consultar fragmentos en `document_embeddings` utilizando el puerto de almacenamiento documental.

---

## 3. Especificación de Contratos API

### Endpoint de Diagnóstico de Fase
- **HTTP Method:** `POST`
- **Path:** `/api/v1/phases/{phaseId}/diagnose`
- **Headers:** `Authorization: Bearer <token>`
- **Comportamiento:** 
  1. Verifica autorización del actor (Coordinador `[CC]` solo para su carrera asignada; `[JD]` para cualquier carrera).
  2. Si existe un diagnóstico registrado en `phase_diagnostic` con `updated_at` menor a 15 minutos, retorna el reporte guardado (Caché).
  3. Si expiró el cooldown o no existe, bloquea llamadas concurrentes sobre el mismo `phaseId` (Single-Flight), realiza la inferencia de RAG+LLM síncrona y guarda el resultado en `phase_diagnostic`.
- **Response Schema (`200 OK`):**
```json
{
  "phaseId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "result": "# Diagnóstico de Acreditación de la Fase 1\n\n## 1. Estado de Bloqueo\nLa fase se encuentra estancada...",
  "updatedAt": "2026-07-30T16:21:27Z",
  "cooldownRemainingSeconds": 900
}
```

---

## 4. Componentes de Arquitectura Hexagonal

Para mantener la capa de dominio pura y libre de frameworks (regla en [AGENTS.md](../../AGENTS.md)):

```
[Infraestructura: REST Controller] ──> [Aplicación: DiagnosePhaseUseCase]
                                                 │
                        ┌────────────────────────┼────────────────────────┐
                        ▼                        ▼                        ▼
           [Puerto: RepositoryPort]     [Puerto: ChatModelPort]  [Puerto: DocumentStorePort]
                        │                        │                        │
                        ▼                        ▼                        ▼
           [Adaptador: JPA Adapter]    [Adaptador: LangChain4j] [Adaptador: pgvector Store]
```

*   **Dominio**: `PhaseDiagnostic` (Modelado puro Java sin JPA ni anotaciones de framework).
*   **Aplicación (Input Port)**: `DiagnosePhaseUseCase` (Caso de uso que orquesta la recolección de datos y la llamada de diagnóstico).
*   **Puertos de Salida (Output Ports)**:
    *   `PhaseDiagnosticRepositoryPort`: Lectura y escritura en la tabla `phase_diagnostic`.
    *   `ChatModelPort`: Abstracción de envío de prompt al LLM.
    *   `DocumentRetrievalPort`: Consulta semántica al almacén de vectores RAG (`pgvector`).
*   **Adaptadores (Infrastructure Layer)**:
    *   `PhaseDiagnosticController`: Endpoint REST.
    *   `JpaPhaseDiagnosticAdapter`: Implementa el repositorio JPA.
    *   `LangChain4jDiagnosticAdapter`: Implementa `ChatModelPort` conectándose a Gemini/Ollama.

---

## 5. Control de Concurrencia (Single-Flight & Cooldown)

Para evitar sobrecargar los hilos de Spring Boot y agotar los límites de API del proveedor de IA:

1.  **UI Cooldown**: La interfaz web deshabilita el botón "Diagnosticar Fase" y muestra un cooldown dinámico basado en la diferencia entre el tiempo actual y el `updatedAt` devuelto en el payload.
2.  **Locks en Servidor (Single-Flight)**: El backend utiliza una estructura de exclusión mutua (`ConcurrentHashMap` de semáforos o locks basados en `phaseId`). Si llega una petición B mientras la petición A sigue llamando al LLM para la misma fase, la petición B bloquea su hilo esperando la resolución del hilo de A, y luego lee y retorna directamente el registro de base de datos recién persistido.

## 6. Plan de Validación y QA (Cobertura Completa)

### 6.1 Happy Paths (Rutas Felices)
*   **TC-HP-01: Generación de Primer Diagnóstico (Sin Caché)**
    *   *Escenario*: Se solicita diagnóstico para la Fase X que no tiene registros previos en `phase_diagnostic`.
    *   *Resultado esperado*: El backend realiza la consulta SQL, conecta con el LLM, guarda el reporte en `phase_diagnostic` y retorna `200 OK` con el JSON estructurado. Tiempo de respuesta estimado: 2–5s.
*   **TC-HP-02: Recuperación desde Caché Activa (Dentro de Cooldown)**
    *   *Escenario*: Se solicita diagnóstico de la Fase X que posee una entrada en `phase_diagnostic` actualizada hace 5 minutos.
    *   *Resultado esperado*: El backend retorna inmediatamente el texto almacenado sin invocar al LLM. Tiempo de respuesta esperado: < 15ms.
*   **TC-HP-03: Expiración de Cooldown y Recálculo**
    *   *Escenario*: Se solicita diagnóstico de la Fase X cuya entrada en `phase_diagnostic` tiene 16 minutos de antigüedad.
    *   *Resultado esperado*: El backend detecta la expiración, llama al LLM, sobrescribe el registro anterior con la nueva fecha de actualización y retorna `200 OK`.

### 6.2 Normal & Alternative Paths (Rutas Normales y Alternas)
*   **TC-AP-01: Consulta Global por Jefatura [JD]**
    *   *Escenario*: Un usuario con rol `[JD]` solicita diagnóstico de la Fase X (Carrera de Sistemas) y luego de la Fase Y (Carrera de Medicina).
    *   *Resultado esperado*: Ambos diagnósticos se procesan con éxito (lectura de caché o regeneración), ya que el `[JD]` no tiene restricciones de aislamiento de carrera.
*   **TC-AP-02: Fase Vacía (Sin Indicadores)**
    *   *Escenario*: Se solicita diagnóstico de una fase recién inicializada que no tiene indicadores asociados aún.
    *   *Resultado esperado*: El sistema responde de inmediato con un mensaje limpio: *"Fase sin indicadores configurados para evaluar"*, sin consumir tokens del LLM.

### 6.3 Sad Paths (Rutas Tristes y Fallos de Robustez)
*   **TC-SP-01: Fallo o Caída del Proveedor de IA (Gemini/Ollama Offline)**
    *   *Escenario*: El cooldown expiró, se requiere llamar al LLM, pero el API del proveedor de IA retorna un timeout o error 500.
    *   *Resultado esperado*:
        *   Si existe un diagnóstico previo en la tabla `phase_diagnostic` (aunque sea antiguo), el sistema lo retorna adjuntando un header HTTP de advertencia: `X-Cache-Stale: true` y una advertencia en el payload.
        *   Si no hay caché previa, el sistema responde de forma controlada con un error `503 Service Unavailable` y un mensaje amigable: *"El asistente de IA no está disponible en este momento. Inténtelo más tarde"*, evitando stacktraces expuestos.
*   **TC-SP-02: Identificador de Fase Inexistente**
    *   *Escenario*: Petición con un `phaseId` UUID aleatorio que no existe en la tabla `phase`.
    *   *Resultado esperado*: Retorna `404 Not Found` con código de error `PHASE_NOT_FOUND`.
*   **TC-SP-03: Fallo de Conexión a PostgreSQL (Base de Datos Real)**
    *   *Escenario*: La base de datos PostgreSQL real se desconecta durante el proceso.
    *   *Resultado esperado*: El pool de conexiones (Hikari) gestiona el fallo, la transacción se revierte y retorna `500 Internal Server Error` sin fugas de memoria.

### 6.4 Boundaries (Límites)
*   **TC-BD-01: Límite de Cooldown (14m 59s vs 15m 00s)**
    *   *Escenario*: Probar dos peticiones con diferencia temporal milimétrica respecto al umbral de 15 minutos.
    *   *Resultado esperado*: A los 14m 59s se sirve caché (sin incrementar llamadas a la IA). A los 15m 00s se dispara la recarga del LLM.
*   **TC-BD-02: Indicadores con Observaciones Vacías o Cortas**
    *   *Escenario*: Un indicador está en estado `OBSERVADO` pero el evaluador técnico ingresó un texto vacío o menor a 20 caracteres (violación de consistencia).
    *   *Resultado esperado*: El backend limpia y valida el texto. Si no hay justificación semántica útil, el prompt del LLM se adapta indicando: *"Indicador observado sin justificación registrada"*, evitando fallos de formato en la llamada del modelo.

### 6.5 Constraints & RBAC (Seguridad y Restricciones)
*   **TC-CS-01: Violación de Aislamiento de Carrera (FSD-BR-09)**
    *   *Escenario*: Un coordinador `[CC]` asignado a la carrera A intenta llamar a `/api/v1/phases/{id}/diagnose` de una fase perteneciente a la carrera B.
    *   *Resultado esperado*: El backend valida el alcance del token JWT (`programScope`) contra el `academicProgramId` de la fase y retorna `403 Forbidden` con el código `ACCESS_DENIED`.
*   **TC-CS-02: Token de Autorización Ausente o Inválido**
    *   *Escenario*: Petición al endpoint de diagnóstico sin el Header `Authorization: Bearer <token>` o con un token expirado/corrupto.
    *   *Resultado esperado*: El perímetro de seguridad intercepta la petición y retorna `401 Unauthorized`.
*   **TC-CS-03: Acceso con Rol Público [P]**
    *   *Escenario*: Un usuario con rol público o externo intenta invocar el diagnóstico.
    *   *Resultado esperado*: Retorna `403 Forbidden`, ya que solo los roles `[CC]` y `[JD]` tienen permisos de lectura del dashboard ejecutivo y diagnósticos.

### 6.6 Concurrency & Performance (Concurrencia)
*   **TC-CC-01: Bloqueo de Peticiones Simultáneas (Single-Flight)**
    *   *Escenario*: 10 usuarios de la misma carrera hacen clic en "Diagnosticar" exactamente en el mismo segundo sobre la misma fase (sin caché activa).
    *   *Resultado esperado*: 
        *   Se ejecuta exactamente **una sola llamada** al LLM.
        *   Los otros 9 hilos esperan de forma bloqueada la resolución de la primera llamada.
        *   Los 10 usuarios reciben el mismo resultado JSON exacto con idéntico `updatedAt`.
*   **TC-CC-02: Ejecución Paralela Multi-Fase**
    *   *Escenario*: El usuario A diagnostica la Fase X de Sistemas mientras el usuario B diagnostica la Fase Y de Medicina en el mismo instante.
    *   *Resultado esperado*: Ambas llamadas se ejecutan en paralelo de forma asíncrona sin bloquearse mutuamente, ya que los locks de Single-Flight son independientes y están indexados por `phaseId`.

