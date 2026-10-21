GET /api/v1/processes/{processId}/phases/{phaseId}/progress

- Devuelve el progreso de una fase según el estado de sus subfases, usando las entidades existentes (Phase, Subphase, PhaseState, SubphaseState). Sin tablas ni dependencias nuevas.
- Entrada: processId y phaseId (UUID) en la ruta.
- Salida 200 OK: { "phaseId": "...", "totalSubphases": 5, "approvedSubphases": 3, "pendingSubphases": 2, "progressPercent": 60.0, "state": "ABIERTA" }.
- Error 400: processId o phaseId no son UUID válidos.
- Error 404: processId o phaseId no existen, o la fase no pertenece al proceso.
- Reglas: approvedSubphases cuenta las subfases en SubphaseState.APROBADO; pendingSubphases = total - approved; progressPercent = approved/total*100 con 1 decimal; si no hay subfases, total = 0 y progressPercent = 0.0.
- Pruebas: 200 con progreso parcial, 200 con todas aprobadas, 200 sin subfases, 400 UUID inválido, 404 proceso/fase inexistente o fase de otro proceso.