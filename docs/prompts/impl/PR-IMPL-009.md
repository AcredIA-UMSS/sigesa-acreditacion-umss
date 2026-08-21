# Prompt de Implementación PR-IMPL-009 - Aprobar Indicador
Implementar el caso de uso FSD-UC-009 en el backend y frontend.
- API: POST `/api/v1/indicators/{id}/approve`.
- Validar rol `TD` y estados `SUBIDO` o `SUBSANADO`.
- Marcar observaciones del indicador como `RESOLVED`.
