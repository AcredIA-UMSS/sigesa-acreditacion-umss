# Test results — GET /api/health

**Command:** `cd backend && ./mvnw test -Dtest=HealthControllerTest`  
**Finished at:** 2026-10-21T19:51:31-04:00  
**Result:** BUILD SUCCESS

| Test | Result |
|------|--------|
| `healthAnonymousReturns200Up` | OK |
| `protectedEndpointWithoutTokenStill401` | OK |
| `healthPostIsNotPermittedAnonymously` | OK |

Tests run: 3, Failures: 0, Errors: 0, Skipped: 0. Time elapsed: 3.784 s.

Nota: el primer intento en sandbox falló por Mockito/ByteBuddy (`Could not self-attach`). Reintento fuera del sandbox: éxito.
