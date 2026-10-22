# Code summary — GET /api/health

| Ruta | Acción |
|------|--------|
| `backend/src/main/java/com/umss/sigesa/adapter/in/web/HealthController.java` | GET `/api/health` → 200 `{status:UP}` |
| `backend/src/main/java/com/umss/sigesa/adapter/in/web/dto/HealthResponse.java` | Record JSON |
| `backend/src/main/java/com/umss/sigesa/adapter/in/security/SecurityConfig.java` | `GET /api/health` permitAll |
| `backend/src/test/java/com/umss/sigesa/adapter/in/web/HealthControllerTest.java` | 200 público, 401 en path protegido, POST no público |

Sin cambios de dominio, frontend ni otros endpoints.
