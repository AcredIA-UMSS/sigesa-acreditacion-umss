# Reverse Engineering — GET /api/health (Express, depth Minimal)

**Proyecto**: SIGESA (brownfield)  
**Fecha**: 2026-10-21  
**Alcance de análisis**: adaptador REST + Spring Security. Sin cambios de dominio.

## Hallazgos

| Tema | Evidencia | Implicación |
|------|-----------|-------------|
| Prefijo API | Controladores mapean `/api/v1/...` (p. ej. `AuthController` `@RequestMapping("/api/v1/auth")`) | El intent pide **`GET /api/health`**, no `/api/v1/health`. Se respeta el path literal del requerimiento. |
| Seguridad | `SecurityConfig.securityFilterChain`: `POST /api/v1/auth/login` y Swagger `permitAll`; resto `authenticated()` | Hay que añadir **solo** `GET /api/health` como público, **antes** de `anyRequest()`. |
| Filtro JWT | `JwtAuthenticationFilter` no rechaza ausencia de token; delega a la cadena | No hace falta exceptuar el filtro; `permitAll` basta. |
| Health existente | No hay controlador `/api/health`. Actuator no está expuesto como contrato del intent. Compose usa health de **Open WebUI**, no del backend SIGESA. | Endpoint nuevo, mínimo, liveness de proceso. |
| Pruebas | `WebMvcTest` + `MockMvc` (p. ej. `AuthControllerTest`); humo con `@SpringBootTest` en `AuthenticatedApiSmokeTest` | Prueba mínima: `WebMvcTest` del nuevo controlador **con** `SecurityConfig` para verificar 200 anónimo y que otro path sigue 401. |
| Hexagonal | Dominio sin Spring | Health es adaptador de infraestructura; **sin** puerto de dominio ni caso de uso. |

## Fuera de alcance (no tocar)

- Login, RBAC, `/api/v1/**` salvo la regla nueva de permitAll.
- Frontend, Orval, Docker Compose healthchecks.
- Actuator `/actuator/health` (no pedido).

## Riesgo

Si `permitAll` se aplica a `/api/health/**` o a todos los métodos, se abriría superficie de más. Matcher: **`HttpMethod.GET` + `/api/health`**.
