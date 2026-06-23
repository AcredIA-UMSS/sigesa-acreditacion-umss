---
id: FSD-UC-002
nombre: Gestión de usuarios [JD]
estado: Hecho
release: v1.0
actor_principal: "[JD]"
trazabilidad_prd: PRD-US-002
modulo: MOD-AUTH
reglas: —
ultima_actualizacion: "2026-06-22"
---

# FSD-UC-002 — Gestión de usuarios [JD]

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-001 · PRD-US-002 |
| **Precondiciones** | [JD] autenticado |
| **Pantalla** | `/admin/users` |

## Flujo principal

1. [JD] accede a `/admin/users`.
2. Registra usuario con correo UMSS, rol y carrera (si [CC]).
3. Sistema crea cuenta **inactiva** hasta primer acceso.
4. Asocia `user_program_assignment` con alcance mínimo necesario.

## Excepciones y flujos alternos

| ID | Condición | Comportamiento |
|----|-----------|----------------|
| A1 | Revocación de acceso | Desactiva cuenta; usuario no puede login; historial en auditoría conservado |

## Postcondiciones

Usuario creado o desactivado; evento registrado en bitácora (UC-017).

## Diagramas

- [Estados usuario](../diagramas/UC02_estado.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-002 @FSD-UC-002 @TC-02
Característica: Gestión de usuarios [JD]

  Escenario: Alta de usuario con rol
    Dado un [JD] autenticado
    Cuando registra un usuario con correo UMSS y rol [CC]
    Entonces el sistema crea la cuenta inactiva hasta primer acceso
    Y asocia permisos solo a la carrera autorizada

  Escenario: Revocación de acceso
    Dado un usuario [CC] que deja la coordinación
    Cuando el [JD] desactiva la cuenta
    Entonces el usuario no puede iniciar sesión
    Y conserva historial de acciones previas en auditoría
```
