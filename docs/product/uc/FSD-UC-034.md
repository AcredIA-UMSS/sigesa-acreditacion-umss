---
id: FSD-UC-034
nombre: Manejo uniforme de errores de la API REST
estado: Implementado
release: v2.1
actor_principal: Sistema / clientes API
trazabilidad_prd: NFR-API
modulo: MOD-API
design_doc: DD-UC-034
pr_impl: PR-IMPL-049
github_issue: "https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/67"
ultima_actualizacion: "2026-11-04"
---

# FSD-UC-034 — Manejo uniforme de errores de la API REST

## Contexto

Los clientes (frontend Orval, integraciones) deben recibir **el mismo envelope** ante validación, autenticación, autorización, 404 e error interno, **sin trazas ni secretos**.

Contrato vivo: [`api_contracts.md`](../api_contracts.md) §1.

## Envelope

```json
{
  "error": "ERROR_CODE",
  "message": "Texto comprensible",
  "details": {}
}
```

`details` es opcional (validación de campos, parámetros). Códigos HTTP: 400, 401, 403, 404, 405, 409, 415, 422, 500, 502, 503, 504.

## Flujos cubiertos

| Situación | HTTP | `error` |
|-----------|------|---------|
| Bean validation / JSON inválido | 400 | `VALIDATION_ERROR` / `MALFORMED_JSON` |
| Sin token / token inválido | 401 | `UNAUTHORIZED` |
| Credenciales login | 401 | `AUTH_INVALID_CREDENTIALS` |
| Rol/alcance | 403 | `ACCESS_DENIED` |
| Recurso inexistente | 404 | código de dominio o `NOT_FOUND` |
| Conflicto de estado | 409 | código de dominio (`EVIDENCE_IMMUTABLE`, …) |
| Error no controlado | 500 | `INTERNAL_ERROR` (mensaje genérico; stack solo en log) |

## Fuera de alcance

- Cambiar códigos de negocio ya documentados.
- RFC 7807 Problem Details (el contrato del producto ya usa `error`+`message`).
- Exponer SQL, tokens o stack al cliente.

## Criterios Gherkin

```gherkin
@FSD-UC-034 @api-errors
Escenario: Error interno no filtra secretos
  Dado un fallo no controlado en un endpoint
  Cuando el cliente recibe la respuesta
  Entonces el status es 500
  Y error es INTERNAL_ERROR
  Y el cuerpo no contiene SQL ni stack trace

@FSD-UC-034 @api-errors
Escenario: Validación con detalles de campo
  Dado un POST JSON que omite un campo obligatorio
  Entonces el status es 400
  Y error es VALIDATION_ERROR
  Y details.fields lista el campo
```
