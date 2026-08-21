# Prompt de Implementación PR-IMPL-008 - Rechazar Indicador
Implementar el caso de uso FSD-UC-008 en el backend y frontend.
- API: POST `/api/v1/indicators/{id}/reject` con `{ "justification": "..." }`.
- Validar rol `TD`, estados `SUBIDO` o `SUBSANADO` y justificación de al menos 20 caracteres.
- Crear observación con estado `PENDIENTE_SUBSANACION`.
