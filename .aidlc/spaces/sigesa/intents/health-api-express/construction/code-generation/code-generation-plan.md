# Code generation plan — GET /api/health

Unit único (Express omite Units Generation): liveness HTTP.

## Testing Contract

- Run: `./mvnw -q test -Dtest=HealthControllerTest` (en `backend/`)
- Targets: GET anónimo 200 + `status=UP`; GET `/api/v1/admin/users` sin token 401; POST `/api/health` anónimo 401.
- CI before merge: N/A (ci-pipeline SKIP).
