---
id: ADR-0002
titulo: Adopción de PostgreSQL como Motor de Base de Datos Principal
estado: Aprobado
fecha: "2026-07-26"
autores: "[Equipo Arquitectura SIGESA]"
---

# ADR-0002: Adopción de PostgreSQL como Motor de Base de Datos Principal

## Contexto

El baseline tecnológico original (DTI vFinal) establecía el uso de **H2 (memoria/archivo)** para la persistencia del sistema. Si bien H2 es excelente para prototipado rápido y pruebas, el módulo de Procesos de Acreditación (y SIGESA en general) requiere garantías ACID estrictas, alta concurrencia y soporte nativo para restricciones complejas a nivel de base de datos. 

Específicamente, la implementación del caso de uso `FSD-UC-003` exige la creación de índices únicos parciales (`CREATE UNIQUE INDEX ... WHERE status = 'ACTIVE'`) para garantizar que exista un solo proceso activo por carrera, una característica que requiere un motor relacional de grado de producción.

## Decisión

Se decide migrar el motor de base de datos principal de H2 a **PostgreSQL** para los entornos de desarrollo (`dev`) y producción (`prod`).

1. **Stack de Persistencia:** Spring Boot 4.x, Hibernate y el driver oficial de PostgreSQL.
2. **Entornos Locales:** Los desarrolladores deberán levantar un servidor PostgreSQL localmente (preferiblemente mediante un archivo `docker-compose.yml` provisto en la raíz del repositorio).
3. **Entorno de Pruebas (`test`):** Se retiene el uso de H2 en memoria exclusivamente para la ejecución de pruebas unitarias/integración rápidas, con el objetivo a mediano plazo de transicionar a `Testcontainers` (PostgreSQL) para asegurar paridad total con producción.

## Consecuencias

### Positivas

- **Integridad de Datos:** Soporte nativo para restricciones complejas, claves foráneas robustas y tipos de datos avanzados (UUIDs, JSONB).
- **Escalabilidad y Concurrencia:** Preparación inmediata para el despliegue en producción sin cuellos de botella en la persistencia.
- **Estandarización:** Alineación con los estándares actuales de la industria para arquitecturas empresariales Java.

### Negativas / Riesgos

- **Complejidad operativa local:** Añade una dependencia externa para los desarrolladores (requiere Docker o instalación nativa de PostgreSQL) frente a la configuración "zero-config" de H2.
- **Curva de migración de scripts:** Los scripts de migración y la generación de esquemas deben adaptarse a la sintaxis y tipos de datos específicos del dialecto de PostgreSQL.

## Trazabilidad

- **DD asociado:** `DD-SYS-001` (Configuración de Persistencia con PostgreSQL).
- **FSD impulsador:** `FSD-UC-003` (Requiere control estricto de unicidad de procesos activos)
