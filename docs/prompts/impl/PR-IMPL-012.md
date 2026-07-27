---
id: PR-IMPL-004
fsd_uc: FSD-SYS-001
dd: DD-SYS-001
modulo: MOD-INFRA
tarea: Backend - Configuración de Conexión a PostgreSQL
estado: Pending
---

# PR-IMPL-004: Configuración de Conexión a PostgreSQL

## Contexto

Estamos actualizando nuestro stack tecnológico backend (Spring Boot 4.x, Java 21) para utilizar PostgreSQL como base de datos principal en lugar de H2 para los entornos de desarrollo y producción.

## Tarea

Configurar el proyecto Maven y Spring Boot para conectarse a PostgreSQL garantizando las mejores prácticas de seguridad (variables de entorno) y rendimiento (HikariCP).

## Instrucciones para el Agente Backend

1. Modifica el archivo `pom.xml` para agregar la dependencia del driver oficial de PostgreSQL.
2. Actualiza/Crea el archivo `src/main/resources/application.yml` (y si es necesario `application-dev.yml`).
3. Configura el bloque `spring.datasource` utilizando variables de entorno para la URL, username y password (con valores por defecto seguros para desarrollo local, ej: `${DB_URL:jdbc:postgresql://localhost:5432/sigesa}`).
4. Configura las propiedades de JPA/Hibernate para usar el dialecto adecuado para PostgreSQL y asegura que la generación automática de DDL esté desactivada para producción (`spring.jpa.hibernate.ddl-auto=validate` o `none`), asumiendo el uso de herramientas de migración.
5. No modifiques ni rompas el perfil de pruebas (`test`) que pueda estar usando H2.

## Archivos de Contexto

- `docs/design/DD-SYS-001.md`
- `AGENTS.md` (Para respetar el stack tecnológico)

## Restricciones

- Cero credenciales hardcodeadas en texto plano en los commits.
- El código generado debe limitarse a configuraciones (`pom.xml`, `.yml`, `.properties`), no refactorizar clases Java de dominio o controladores.
