---
id: PR-IMPL-007-MCP
feature_asociado: DD-UC-007-MCP
fsd_uc:
  - "FSD-UC-007"
fecha: "2026-08-13"
version: "1.0"
estado: Draft
autor: "AI Prompt Architect (@sigesa-orchestrator)"
skill_origen: sigesa-prompt-contract-architect
---

# Prompt Contract — Implementación `PR-IMPL-007-MCP`

## 1. Propósito y Objetivo
Implementar la búsqueda inteligente de evidencias multi-token y contextualizada con base de datos en SIGESA (MOD-EVIDENCE) a través de un servidor MCP embebido en Java, siguiendo estrictamente el diseño de enrutamiento híbrido definido en `DD-UC-007-MCP.md` y garantizando el aislamiento de carreras de `FSD-BR-09` y `FSD-BR-26.2`.

---

## 2. Rol y Persona
- **Identidad:** Ingeniero Backend y Full Stack Developer Senior experto en SIGESA.
- **Expertise:** Java 21, Spring Boot 4.x, Spring AI (módulos MCP), PostgreSQL 16 (Búsqueda por Trigramas / pg_trgm), React 19, TypeScript estricto, Orval y OxLint.

---

## 3. Límites de Alcance

### In-Scope
- **Base de Datos (Flyway):**
  - Script de migración SQL para habilitar `pg_trgm` y crear índices GIN/GIST en `academic_program(name)` y `evaluation_dimension(name)`.
- **Backend (Spring Boot & Spring AI):**
  - Creación del Servidor MCP Embebido: `com.umss.sigesa.adapter.out.assistant.mcp.AcademicContextMcpServer`.
  - Definición del método anotado con `@Tool`: `resolveSearchTokensAndSubsets(String phrase, UserContextDto userContext)`.
  - Lógica de descomposición de tokens con consultas nativas `pg_trgm` y filtrado estricto de seguridad: si el usuario es `CC`, se descarta cualquier coincidencia que no pertenezca a su `programScope`.
  - Configuración del cliente MCP: `com.umss.sigesa.config.McpClientConfig` para registrar de forma local las herramientas del servidor embebido.
  - Adaptación de `SearchEvidenceService` para llamar al cliente MCP, procesar los subconjuntos estructurados, ejecutar consultas agrupadas y mapear a `SearchQueryResponseDto`.
- **Frontend (React 19):**
  - Regeneración de código cliente vía Orval.
  - Actualización del componente de búsqueda para renderizar las evidencias agrupadas por subconjunto (`subsets`).

### Out-of-Scope
- Integración con APIs de sistemas académicos externos (ERP) o servicios en Python.
- Búsqueda semántica vectorial de archivos adjuntos (se limita a la metadata y títulos en base de datos PostgreSQL).

---

## 4. Restricciones y Reglas
- **Spring AI:** Usar exclusivamente las librerías oficiales de `spring-ai-mcp-store` para la definición de herramientas MCP.
- **Hexagonal:** Las clases del MCP no deben filtrar entidades directas de JPA en la API REST; mapear siempre a DTOs explícitos utilizando Lombok.
- **TypeScript:** Tipado estricto obligatorio en los componentes de visualización; prohibido el uso de `any`.
- **Advertencias Linter:** Todo código generado debe compilar y pasar la verificación de `oxlint` sin warnings.

---

## 5. Especificaciones de Interfaz y Datos

### Estructura DTO de Entrada (`UserContextDto`):
```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContextDto {
    private String role;
    private String programScope; // UUID de la carrera autorizada
}
```

### Estructura DTO de Salida del Backend (`SearchQueryResponseDto`):
```json
{
  "query": "string",
  "routingPath": "LLM_MULTIPATH",
  "subsets": [
    {
      "label": "string",
      "results": [
        {
          "evidenceId": "UUID",
          "title": "string",
          "description": "string",
          "dimensionName": "string",
          "carreraName": "string",
          "uploadedAt": "LocalDateTime"
        }
      ]
    }
  ]
}
```

---

## 6. Anti-patrones & Violaciones
- ❌ Utilizar scripts de python externos (el servidor MCP debe ser 100% Java embebido).
- ❌ Permitir bypass de seguridad: un coordinador de una carrera `A` jamás debe obtener resultados de una carrera `B` a través de los subconjuntos del MCP.
- ❌ Modificar archivos históricos en `docs/baseline/`.
- ❌ Dejar campos marcados como `// TODO` o implementaciones incompletas.

---

## 7. Checklist de Validación
- [ ] ¿La migración SQL activa la extensión `pg_trgm` de forma segura?
- [ ] ¿El servidor MCP valida correctamente los roles del `UserContextDto`?
- [ ] ¿El backend del MCP es 100% Java usando Spring AI `@Tool`?
- [ ] ¿El DTO agrupa correctamente los resultados por subconjuntos?
- [ ] ¿Se corre Orval para generar los contratos en TypeScript?
- [ ] ¿El linter `oxlint` aprueba todos los cambios del frontend?
