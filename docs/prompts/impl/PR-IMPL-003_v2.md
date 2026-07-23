---
id: PR-IMPL-003V3
fsd_uc:
  - "FSD-UC-003"
fecha: "2026-07-23"
version: "3.0"
estado: Aprobado
autor: "AI Prompt Architect (@sigesa-prompt-contract-architect)"
skill_origen: sigesa-prompt-contract-architect
---

# Prompt Contract — Implementación `PR-IMPL-003V3`

> **Generado vía** `@sigesa-prompt-contract-architect`. Derivado del contrato unificado histórico, actualizado para forzar **Arquitectura Hexagonal (Ports & Adapters)**.  
> **Design doc fuente:** [`DD-UC-002`](../../design/DD-UC-003.md) · **FSD:** FSD-UC-003

---

## 1. Propósito y Objetivo

Generar el código backend completo para la inicialización de Procesos de Acreditación basados en plantillas normativas (CEUB/ARCU-SUR), utilizando estrictamente **Arquitectura Hexagonal (Puertos y Adaptadores)**. Se debe garantizar la clonación de la taxonomía (Fase → Subfase), asegurar un único proceso activo por carrera a la vez, y mantener el Modelo de Dominio completamente aislado de frameworks e infraestructura (Spring/JPA).

## 2. Rol y Persona

- **Identidad:** Senior Java Architect Agent especializado en SIGESA y Arquitectura Hexagonal.
- **Tono:** Técnico, directo, enfocado en código limpio, Domain-Driven Design (DDD) y desacoplamiento.
- **Expertise requerida:** Java 21, Spring Boot 4.x, Puertos y Adaptadores, Spring Data JPA, Mappers (MapStruct o manuales), Lombok y manejo de transacciones ACID.

## 3. Límites de Alcance

### In-Scope

- **Capa de Dominio (`domain`):** Modelos puros de Java (Entidades de Dominio) sin anotaciones de JPA ni dependencias de Spring. Excepciones de dominio.
- **Capa de Aplicación (`application`):**
  - *Inbound Ports:* Interfaces de Casos de Uso.
  - *Outbound Ports:* Interfaces para persistencia (repositorios de dominio).
  - *Use Cases:* Implementación de la lógica de clonación del proceso (`@UseCase` o `@Service` si no hay anotación custom).
- **Capa de Infraestructura (`infrastructure`):**
  - *Web Adapter (In):* Controladores REST protegidos (`@RestController`), DTOs de Request/Response.
  - *Persistence Adapter (Out):* Entidades JPA (`@Entity`), Repositorios Spring Data, implementaciones de los Outbound Ports y Mapeadores (Dominio ↔ JPA).
- Pruebas unitarias (>90% cobertura para JaCoCo) centradas en el Caso de Uso.
- Script de inicialización de datos semilla (ej. `data.sql`).

### Out-of-Scope

- ❌ Prohibido generar código de interfaces de usuario (Frontend/UI/React).
- ❌ Prohibido crear endpoints CRUD dinámicos para Plantillas.
- ❌ Prohibido usar `any`, placeholders (`// TODO`) o código incompleto.

## 4. Restricciones y Reglas

### Restricciones Duras

- **Pureza del Dominio:** Las clases en el paquete `domain` **NO DEBEN** contener anotaciones de Spring, Jackson, ni JPA (`@Entity`, `@Table`, `@Autowired`, etc.). Solo se permite Java puro y Lombok.
- **Regla de Oro (Fronteras):** NUNCA exponer Entidades JPA o Entidades de Dominio en los Controladores. Usar DTOs en el Web Adapter y mapear hacia/desde el Dominio.
- **Productividad:** Uso estricto de Lombok (`@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`).
- **Regla de Unicidad:** Validar activamente en el Caso de Uso (lanzando `ProcessAlreadyActiveException`) utilizando el Outbound Port, y respaldarlo con un índice único parcial en la BD.
- **Seguridad:** El Web Adapter debe estar protegido (`@PreAuthorize("hasRole('ROLE_JD')")`).
- **Transaccionalidad:** La implementación del Caso de Uso debe estar anotada con `@Transactional(rollbackFor = Exception.class)` (importado de Spring en la capa de aplicación o gestionado en un proxy de infraestructura).

### Límites Funcionales

- Todo el código generado debe presentarse en bloques Markdown.
- La primera línea de cada bloque debe indicar la ruta del archivo reflejando la estructura hexagonal (ej: `// src/main/java/bo/edu/umss/sigesa/process/domain/model/AccreditationProcess.java`).

## 5. Especificaciones de Entrada

**Formato:** DTO de Entrada (Request HTTP).

**Contexto Inyectado:**

- **Stack Base:** Java 21, Spring Boot 4.x, Hibernate.
- **Arquitectura:** Hexagonal / Ports and Adapters.

**Esquema de Entrada JSON Esperado en el Web Adapter:**

```json
{
  "career_id": "uuid-v4",
  "template_id": "uuid-v4"
}
```

## 6. Especificaciones de Salida

Formato: Múltiples bloques de código en Markdown (Lenguaje: Java).

Estructura obligatoria de la respuesta (Paquetes):

.../domain/model/*.java (Modelos puros con Lombok).

.../domain/exception/*.java (Errores de negocio).

.../application/port/in/*UseCase.java (Inbound Port).

.../application/port/out/*Port.java (Outbound Port).

.../application/usecase/*UseCaseImpl.java (Lógica central transaccional).

.../infrastructure/adapter/in/web/*Controller.java & *Dto.java (REST).

.../infrastructure/adapter/out/persistence/entity/*JpaEntity.java (JPA).

.../infrastructure/adapter/out/persistence/*PersistenceAdapter.java (Implementa el Outbound Port).

.../infrastructure/adapter/out/persistence/mapper/*Mapper.java (Mapeos Dominio ↔ JPA).

Ejemplo de Salida Válida (Extracto del Dominio):

```java
// src/main/java/bo/edu/umss/sigesa/process/domain/model/AccreditationProcess.java
package bo.edu.umss.sigesa.process.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class AccreditationProcess {
    private UUID id;
    private UUID careerId;
    private UUID templateId;
    private String status;
    private LocalDateTime startDate;
    private List<Phase> phases;
    
    // Comportamiento de dominio puro
    public void activate() {
        this.status = "ACTIVE";
        this.startDate = LocalDateTime.now();
    }
}
```

## 7. Anti-patrones & Violaciones

❌ Anotar modelos del dominio con @Entity, @Table o usar repositorios JPA dentro del dominio.
❌ Inyectar repositorios de Spring Data directamente en el Caso de Uso (deben inyectarse los Outbound Ports).
❌ Pasar Entidades JPA como parámetros o retornos en el controlador REST.
❌ Emitir el código de clonación iterativa sin una transacción ACID que lo proteja.
❌ Usar dependencias web (ej. HttpServletRequest, clases HTTP de Spring) dentro de la capa de Aplicación o Dominio.

## 8. Checklist de Validación

[ ] ¿Los modelos de domain/model están libres de dependencias de frameworks (sin JPA/Spring)?
[ ] ¿El Caso de Uso implementa un Inbound Port y llama a Outbound Ports?
[ ] ¿El Persistence Adapter convierte entre Entidades JPA y Modelos de Dominio?
[ ] ¿El Web Adapter (Controller) usa DTOs estandarizados?
[ ] ¿Scope In/Out está explícito y prohíbe tocar Frontend?
[ ] ¿Se requiere expresamente el uso de Lombok?
[ ] ¿Viola algún invariante de SIGESA? (Validado: Respeta rol JD, inmutabilidad de plantillas).
