# Requirements Analysis — GET /api/health (Express, depth Minimal)

**Aprobado en Reverse Engineering:** path literal `/api/health` (no `/api/v1/health`).  
**Fecha:** 2026-10-21.

## Historia

Como operador o orquestador, quiero consultar `GET /api/health` sin token para saber si el proceso Spring está vivo.

## Criterios de aceptación

1. `GET /api/health` sin `Authorization` responde **HTTP 200**.
2. Cuerpo JSON exacto: `{"status":"UP"}` (`Content-Type` JSON).
3. `POST`/`PUT`/`DELETE` `/api/health` **no** quedan públicos (solo GET).
4. El resto de reglas de `SecurityConfig` no cambian: login y Swagger siguen públicos; `/api/v1/**` (salvo login) sigue exigiendo autenticación.
5. Prueba automatizada JUnit/MockMvc verifica (2) y, como no-regresión, un GET protegido sin token sigue **401**.
6. Sin caso de uso de dominio, sin frontend, sin Actuator, sin Compose.

## Fuera de alcance

- Dependencias de BD, disco o LLM en el JSON (no es readiness).
- Documentar en FSD/DD/PR-IMPL/DTP (Express; no es UC de producto).
- Orval / cliente TypeScript.

## Archivos previstos (Construction)

| Archivo | Acción |
|---------|--------|
| `backend/src/main/java/com/umss/sigesa/adapter/in/web/HealthController.java` | Crear |
| `backend/src/main/java/com/umss/sigesa/adapter/in/web/dto/HealthResponse.java` | Crear (record) |
| `backend/src/main/java/com/umss/sigesa/adapter/in/security/SecurityConfig.java` | `GET /api/health` → `permitAll()` |
| `backend/src/test/java/com/umss/sigesa/adapter/in/web/HealthControllerTest.java` | Crear |

## Testing contract (Minimal)

- Clase: `HealthControllerTest` con `@WebMvcTest(HealthController.class)` + `@Import` de `SecurityConfig`, filtro JWT, entry point (mismo patrón que `UserAdminControllerTest`).
- Casos: GET anónimo 200 + `status=UP`; GET `/api/v1/dashboards/me/summary` o similar sin token → 401 (cadena de seguridad intacta).
