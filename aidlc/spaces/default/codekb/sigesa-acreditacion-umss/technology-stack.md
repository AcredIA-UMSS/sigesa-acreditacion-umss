# Technology Stack

Versiones tomadas de `backend/pom.xml` y, en el frontend, de `frontend/package.json` (ojeada). Lo que el parent de Spring Boot gestiona sin versión explícita queda marcado como gestionado.

## Lenguajes y runtime

| Pieza | Versión | Dónde |
| --- | --- | --- |
| Java | 21 | `backend/pom.xml` (`java.version`) |
| TypeScript | ~6.0.2 | frontend, ojeado |

## Backend

| Pieza | Versión | Papel |
| --- | --- | --- |
| Spring Boot | 4.1.0 | Parent y arranque (`spring-boot-starter-web`, data-jpa, security, validation, flyway) |
| Spring Security | gestionado | Cadena JWT y `@PreAuthorize` |
| jjwt | 0.12.6 | Emisión y parseo HMAC del JWT |
| springdoc-openapi | 2.8.8 | Swagger UI |
| Hibernate / JPA | gestionado | Entidades en el adaptador de salida |
| Flyway | gestionado | Migraciones PostgreSQL del perfil `prod` |
| PostgreSQL JDBC | gestionado | Perfiles `dev` y `prod` |
| H2 | gestionado | Perfil por defecto, en memoria |
| Lombok | gestionado | Accesores y builders |
| JaCoCo | 0.8.13 | Cobertura en `verify`, umbral parcial |
| Bouncy Castle | 1.84 | Proveedor criptográfico |
| Apache POI | 5.2.5 | Documentos Office |
| fastcsv | 3.4.0 | CSV |
| OpenPDF | 2.0.3 | PDF |
| AWS SDK S3 | 2.29.51 | Almacén de objetos |

Virtual threads de Spring están activados en `application.yaml`.

## Frontend (ojeada)

React 19.2.7, React Router 7.18.0, Vite 8.1.0, Tailwind CSS 4.3.1, TanStack Query 5.101.1, Orval 8.18.0, Axios 1.18.1, OxLint 1.69.0, Vitest 5.0.1, Playwright 1.63.0. El cliente de procesos está generado; no se vio una llamada a `/processes/stats`.
