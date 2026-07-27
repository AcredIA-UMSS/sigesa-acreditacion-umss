---
id: DD-SYS-001
title: Configuración de Persistencia con PostgreSQL
fsd_uc: FSD-SYS-001 (Infraestructura Core)
prd_refs: PRD-REQ-TECH-001
adrs: [ADR-0002]
prompts: [PR-IMPL-004]
release: v1.0
status: Draft
ultima_actualizacion: "2026-07-26"
---

# DD-SYS-001: Configuración de Persistencia con PostgreSQL

## 1. Contexto y Alcance

Este documento detalla la implementación técnica para conectar el backend (Spring Boot 4.x / Java 21) a una base de datos PostgreSQL, reemplazando/complementando el motor H2 especificado inicialmente.

**Alcance:**

- Agregar dependencias del driver de PostgreSQL.
- Configurar orígenes de datos (Datasource) mediante `application.yml` diferenciando perfiles (ej. `dev`, `prod`).
- Asegurar la compatibilidad con Spring Data JPA y la estrategia de migraciones (Flyway/Liquibase).

**Fuera de alcance:**

- Configuración de réplicas de lectura/escritura (clustering avanzado).
- Instalación del servidor PostgreSQL (se asume que será provisto vía Docker u otro proveedor).

## 2. Modelo de Datos y Arquitectura

- **Driver:** `org.postgresql:postgresql` (vía Maven).
- **Pool de Conexiones:** HikariCP (por defecto en Spring Boot).
- **ORM:** Hibernate (Dialecto PostgreSQL).

## 3. Interfaces / API

N/A - Este feature no expone endpoints REST, pero habilita la capa de infraestructura (`PersistenceAdapter`) para interactuar con la base de datos real.

## 4. Impacto en Specs Vivas

- **DTP.md:** 🔴 **Delta vs DTI vFinal detectado.** Se debe actualizar la sección de "Stack Tecnológico - Backend" para reflejar PostgreSQL como base de datos principal, manteniendo H2 opcionalmente para el perfil `test`.
- **ADR:** Se requiere la aprobación del `ADR-0002: Adopción de PostgreSQL como Motor de Base de Datos Principal`.

## 5. Seguridad y Permisos

- **Gestión de Credenciales:** Las credenciales de la base de datos (`spring.datasource.username`, `spring.datasource.password`) **NUNCA** deben estar hardcodeadas en el código fuente. Se inyectarán obligatoriamente mediante Variables de Entorno (`${DB_USERNAME}`, `${DB_PASSWORD}`).

## 6. Estrategia de Pruebas

- **Unitarias:** Continúan ejecutándose sin impacto directo de la base de datos (usando Mocks/Mockito).
- **Integración:** Se recomienda mantener H2 para el perfil `test` (`application-test.yml`) para asegurar la velocidad del CI/CD, o transicionar al uso de `Testcontainers` (PostgreSQL container) para garantizar paridad exacta con producción.

## 7. Despliegue y Migraciones

- Actualizar el archivo `pom.xml` con la dependencia de PostgreSQL.
- Configurar las propiedades de Spring (`application.yml`) para habilitar el motor.
- Confirmar que los scripts SQL (`V1__Create_...`) sean compatibles con el dialecto de PostgreSQL (ej. uso de `uuid_generate_v4()` o el soporte nativo de UUID).
