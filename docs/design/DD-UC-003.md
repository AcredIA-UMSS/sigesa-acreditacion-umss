---
id: DD-UC-003
fsd_ref: FSD-UC-003
titulo: "Diseño: Plantillas y Proceso CEUB/ARCU-SUR"
modulo: MOD-PROCESS
arquitectura: Hexagonal
tecnologia: Java 21, Spring Boot 3.x
estado: Aprobado
autor: AI Architect (@feature-design-doc)
fecha: "2026-06-22"
---

# DD-UC-003: Diseño de Plantillas y Proceso CEUB/ARCU-SUR

## 1. Propósito y Objetivo
Implementar el módulo `MOD-PROCESS` permitiendo a los actores `[JD]` activar plantillas normativas (CEUB/ARCU-SUR) y generar procesos de acreditación. Se debe garantizar arquitectónicamente la regla de negocio **FSD-BR-08** (un solo proceso activo por tipo/carrera/periodo) y evitar mutaciones retroactivas en procesos en curso.

---

## 2. Capa de Dominio (Core)

El dominio es el corazón de la arquitectura y no debe tener dependencias del framework (Spring) ni de la base de datos (JPA).

### Entidades (Aggregate Roots)
*   `Template`: Representa la plantilla normativa. Contiene la taxonomía jerárquica inmutable una vez activada.
*   `AccreditationProcess`: Representa la instancia del proceso para una carrera en un periodo específico.

### Value Objects
*   `Taxonomy`: Estructura anidada (`Fase` → `Dimensión` → `Criterio` → `Indicador`).
*   `ProcessType`: Enum (`CEUB`, `ARCU_SUR`).
*   `ProcessStatus`: Enum (`ACTIVE`, `CLOSED`, `ARCHIVED`).

### Excepciones de Dominio
*   `ProcessAlreadyActiveException` (Mapea al error `PROCESS_ALREADY_ACTIVE` definido en la excepción A1).
*   `TemplateNotValidException` (Lanzada si la plantilla no fue validada por el comité).

---

## 3. Puertos (Interfaces de Dominio)

### Inbound Ports (Casos de Uso)
Interfaces que los adaptadores de entrada (Controladores) invocarán.
*   `ActivateTemplateUseCase`: Maneja la lógica de validación y activación de una plantilla.
*   `CreateAccreditationProcessUseCase`: Orquesta la creación de un nuevo proceso asegurando la regla FSD-BR-08.

### Outbound Ports (Repositorios)
Interfaces que el dominio usa para comunicarse con el exterior.
*   `TemplateRepositoryPort`: Para buscar y guardar el estado de las plantillas.
*   `AccreditationProcessRepositoryPort`: 
    *   Debe contener explícitamente la firma: `boolean existsActiveProcessByCareerAndTypeAndPeriod(UUID careerId, ProcessType type, String period);`

---

## 4. Capa de Aplicación (Implementación de Casos de Uso)

### Clase: `CreateAccreditationProcessService` (Implementa `CreateAccreditationProcessUseCase`)
**Flujo Lógico:**
1.  Recibe la solicitud con `careerId`, `templateId` y `period`.
2.  Llama a `AccreditationProcessRepositoryPort.existsActiveProcessByCareerAndTypeAndPeriod(...)`.
3.  Si devuelve `true`, lanza `ProcessAlreadyActiveException`.
4.  Llama a `TemplateRepositoryPort.findById(...)` para recuperar la plantilla.
5.  Clona la taxonomía de la plantilla al nuevo contexto del proceso (garantizando el aislamiento contra futuros cambios en la plantilla).
6.  Instancia el `AccreditationProcess` con estado `ACTIVE`.
7.  Guarda la entidad vía `AccreditationProcessRepositoryPort.save(...)`.

---

## 5. Capa de Adaptadores (Infraestructura)

### Adaptadores de Entrada (Web / REST)
Clases anotadas con `@RestController` que implementan los contratos API.
*   `TemplateController`: 
    *   `POST /api/v1/templates/{id}/activate`
*   `AccreditationProcessController`:
    *   `POST /api/v1/processes`

### Adaptadores de Salida (Persistencia)
*   `TemplateJpaAdapter`: Implementa `TemplateRepositoryPort`. Traduce la entidad de dominio `Template` a la entidad `@Entity TemplateEntity`.
*   `AccreditationProcessJpaAdapter`: Implementa `AccreditationProcessRepositoryPort`. Utiliza Spring Data JPA (`AccreditationProcessJpaRepository`) para interactuar con PostgreSQL.

---

## 6. Contratos de Datos (DTOs)

Utilizaremos `records` de Java 21 para garantizar la inmutabilidad de los DTOs en los controladores.

### Request DTOs
```java
public record ActivateTemplateRequest(
    String period
) {}

public record CreateProcessRequest(
    UUID templateId,
    UUID careerId,
    String period,
    ProcessType type
) {}
```
### Response DTOs

```java
public record ProcessResponse(
    UUID processId,
    ProcessStatus status,
    String taxonomySnapshotVersion,
    LocalDateTime createdAt
) {}
```

---

## 7. Validaciones y Reglas Duras (Para el prompt de implementación)

### FSD-BR-08

Es obligatorio aislar la verificación de duplicidad de forma transaccional cuando sea posible.

Utilizar una restricción de base de datos para prevenir *race conditions*:

```sql
UNIQUE (career_id, process_type, period)
WHERE status = 'ACTIVE'
```

### FSD-BR-17

Los plazos normativos mapeados desde la plantilla hacia el proceso **no deben exponer métodos setter públicos** en el controlador de coordinadores (`CC`).

### Restricciones Arquitectónicas

* Las entidades JPA (`@Entity`) están estrictamente prohibidas en los controladores.
* Las entidades JPA (`@Entity`) están estrictamente prohibidas en el dominio.
* Todas las entidades persistentes deben mapearse a entidades de dominio puro antes de cruzar cualquier puerto de la Arquitectura Hexagonal.
* Ningún controlador puede exponer directamente entidades JPA en solicitudes o respuestas.
