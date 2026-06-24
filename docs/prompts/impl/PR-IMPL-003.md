---
id: PR-IMPL-003
feature_asociado: DD-UC-003
fecha: "2026-06-22"
autor: "AI Prompt Architect (@sigesa-prompt-contract-architect)"
---

# Prompt de Implementación `PR-IMPL-003`

## 1. Propósito y Objetivo
Generar el código fuente en Java para el módulo `MOD-PROCESS` que permita activar plantillas normativas y crear procesos de acreditación. El código debe garantizar la regla de negocio **FSD-BR-08** (un solo proceso activo por tipo, carrera y periodo) utilizando una estructura estricta de puertos y adaptadores.

## 2. Rol y Persona
Actúa como un **Desarrollador Backend Senior experto en SIGESA**. Escribes código para Java 21 usando Spring Boot 3.x. Dominas la **Arquitectura Hexagonal**, el diseño guiado por el dominio (DDD) y la creación de código limpio, transaccional y listo para producción.

## 3. Límites y Restricciones (In-Scope / Out-of-Scope)
- **Obligatorio (Hexagonal):** El dominio NO debe tener dependencias de Spring (ni `@Service`, ni `@Autowired`, ni imports de JPA).
- **Obligatorio (DTOs):** Usa `records` de Java 21 para `CreateProcessRequest`, `ActivateTemplateRequest` y `ProcessResponse` en los adaptadores de entrada (Controladores).
- **Prohibido:** Modificar código fuera del alcance de `MOD-PROCESS`. 
- **Prohibido (Acoplamiento):** Exponer entidades `@Entity` de JPA en los controladores o en el dominio. Usa mapeadores (Mappers) en la capa de infraestructura.
- **Validación FSD-BR-08:** El servicio de aplicación debe verificar transaccionalmente la preexistencia de un proceso activo antes de crear uno nuevo, lanzando `ProcessAlreadyActiveException`.

## 4. Contexto de Entrada
- **Diseño a seguir:** Debes basarte estrictamente en los contratos, puertos y entidades definidos en el archivo `docs/design/DD-UC-003.md`.
- **Casos de Uso Core:** `CreateAccreditationProcessUseCase` y `ActivateTemplateUseCase`.
- **Excepciones de Dominio:** `ProcessAlreadyActiveException` (HTTP 409) y `TemplateNotValidException` (HTTP 422).

## 5. Salida Esperada
Proporciona el código Java completo de los siguientes componentes, organizados por capas:
1. **Dominio:** Entidades base (`Template`, `AccreditationProcess`) y Excepciones.
2. **Puertos:** Inbound (`UseCase`) y Outbound (`RepositoryPort`).
3. **Aplicación:** Servicio implementando el Inbound Port (`CreateAccreditationProcessService`).
4. **Adaptadores (Infra):** Controladores REST con los DTOs en formato `record`.
(No es necesario generar los adaptadores de base de datos JPA completos en esta primera iteración, puedes hacer stubs/interfaces).