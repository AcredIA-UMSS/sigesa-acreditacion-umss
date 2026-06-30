---
id: FSD-UC-001
nombre: Autenticación y sesión
estado: Hecho
release: v1.0
actor_principal: Usuario interno ([CC], [TD], [JD])
trazabilidad_prd: PRD-US-001, PRD-US-003
modulo: MOD-AUTH
reglas: FSD-BR-12
ultima_actualizacion: "2026-06-23"
design_doc: DD-UC-001
pr_impl: PR-IMPL-001
---

# FSD-UC-001 — Autenticación y sesión

## Contexto

| Campo | Valor |
|-------|-------|
| **Design Doc** | [`DD-UC-001`](../../design/DD-UC-001.md) |
| **Prompt impl** | [`PR-IMPL-001`](../../prompts/impl/PR-IMPL-001.md) |
| **Trazabilidad** | PRD-REQ-001 · PRD-US-001, 003 · BRD-REQ-001 · MRD-N-09 |
| **Disparador** | Submit en pantalla `/login` |
| **Precondiciones** | Cuenta registrada con correo `@umss.edu.bo`; rol asignado |

## Flujo principal

1. Usuario ingresa credenciales (`email`, `password`).
2. Sistema valida vía `LocalAuthAdapter` (v1.0); v1.1 `LdapAuthAdapter` (ADR-0003).
3. Sistema crea sesión JWT con rol y alcance (carrera/facultad).
4. Redirige al dashboard según rol.
5. Registra `AUDIT_LOGIN` (UC-017).

## Excepciones y flujos alternos

| ID | Condición | Respuesta |
|----|-----------|-----------|
| A1 | Credenciales inválidas | `401` genérico; no revelar si el usuario existe |
| A2 | Sin rol asignado | `403`; acceso denegado |
| E3 | Sin sesión en acción sensible | `401`; sin cambios de estado (US-003) |

## Postcondiciones

Sesión activa con permisos acotados al rol y alcance de carrera/facultad.

## Datos

| Dirección | Campos |
|-----------|--------|
| Entrada | `email`, `password` |
| Salida | `accessToken`, `expiresIn`, `role`, `programScope` |

## Diagramas

- [Secuencia autenticación JWT](../diagramas/FSD-UC-001_autenticacion_secuencia.mmd)
- [MAR-SEQ-001 autenticación](../diagramas/MAR-SEQ-001-autenticacion-jwt.mmd)
- [diag-01 secuencia](../diagramas/diag-01-seq-autenticacion.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-001 @FSD-UC-001 @NFR-008 @TC-01
Característica: Autenticación y sesión

  Escenario: Inicio de sesión exitoso con rol asignado
    Dado un usuario con correo institucional UMSS activo y rol [CC], [TD] o [JD]
    Cuando inicia sesión con credenciales válidas
    Entonces el sistema crea una sesión autenticada
    Y redirige al panel correspondiente a su rol

  Escenario: Credenciales inválidas
    Dado un usuario en la pantalla de inicio de sesión
    Cuando ingresa credenciales incorrectas
    Entonces el sistema rechaza el acceso
    Y muestra un mensaje de error sin revelar si el usuario existe

@PRD-US-003 @FSD-UC-001 @TC-SAD-005
  Escenario: Acción sensible sin autenticación
    Dado un usuario no autenticado
    Cuando intenta cargar o aprobar una Evidencia
    Entonces el sistema rechaza la operación con código de no autorizado
    Y no registra cambios de estado
```
