# Pruebas de humo — resumen de procesos

Fecha: 2026-10-20. Contra `http://localhost:8080`, después de recrear `sigesa-backend`.

## Sin token

`GET /api/v1/processes/stats` respondió 401.

```json
{"message":"No autenticado","error":"UNAUTHORIZED"}
```

Es la comprobación de `cd-config.md`.

## Con sesión de JD

`POST /api/v1/auth/login` con el usuario de demostración `jd@umss.edu.bo` respondió 200 y un rol `JD`. Con ese token, `GET /api/v1/processes/stats` respondió 200:

```json
{"total":3,"byStatus":{"ACTIVE":3,"CLOSED":0},"byOperationalMode":{"ACTIVE":3,"AUDIT_READONLY":0,"IMPROVEMENT_MONITORING":0},"frozenCount":0}
```

El cuerpo trae las cuatro claves pedidas. `CLOSED`, `AUDIT_READONLY` e `IMPROVEMENT_MONITORING` están en 0. `frozenCount` coincide con `AUDIT_READONLY`.

## Resultado

Las dos llamadas pasaron.
