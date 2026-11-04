---
id: PR-IMPL-049
feature_asociado: DD-UC-034
fsd_uc:
  - FSD-UC-034
fecha: "2026-11-04"
version: "1.0"
estado: Implementado
autor: "Boris Anthony Angulo Urquieta"
github_issue: "https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/67"
release: "2.1.0"
---

# Prompt Contract — Implementación `PR-IMPL-049`

> **Issue:** [#67](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/67) — Manejo uniforme de errores en la API REST.  
> **Design:** [`DD-UC-034`](../../design/DD-UC-034.md)  
> **FSD:** [`FSD-UC-034`](../../product/uc/FSD-UC-034.md)

## 1. Propósito

Unificar el envelope de error REST y cubrir huecos (500, validación con `details`, sin stack al cliente) sin romper códigos de negocio ni el frontend.

## 2. In-Scope

- `ApiErrorResponse` + `GlobalExceptionHandler`.
- 401/403 security writers al mismo DTO.
- `server.error.include-*=never`.
- Tests WebMvc del envelope.
- Docs: api_contracts §1, FSD-UC-034, DTP, PM sprint 03.

## 3. Out-of-Scope

- RFC 7807.
- Reescribir todos los advice de dominio.
- Cambiar mensajes de negocio (`EVIDENCE_IMMUTABLE`, etc.).
- `docs/baseline/`.

## 4. Reglas

| ID | Regla |
|----|-------|
| R1 | JSON de error siempre tiene `error` y `message`. |
| R2 | 500: mensaje genérico; stack solo en log. |
| R3 | Frontend sigue leyendo `error`/`message` (`customFetch`). |
| R4 | Cierre: `@save-prompt-mapping sprint=03 fsd=FSD-UC-034`. |
