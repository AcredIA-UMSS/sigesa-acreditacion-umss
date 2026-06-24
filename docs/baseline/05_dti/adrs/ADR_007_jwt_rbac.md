# ADR_007: Autenticación JWT stateless y RBAC institucional

| Campo | Valor |
|-------|-------|
| **Canónico** | [ADR-0007](../../adr/ADR-0007-jwt-rbac-authentication.md) |
| **Origen equipo** | `team/aylenGonzales/09_dti/adr/ADR-004.md` |
| **Relacionado** | [ADR_003](ADR_003_adapter_autenticacion.md) (proveedor de credenciales) |

## Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0004` |
| Título | Autenticación stateless con JWT + RBAC por rol institucional (@umss.edu.bo) |
| Fecha | 16/05/2026 |
| Autor(es) | Equipo AcredIA |
| Estado | **Aceptada** |
| Alcance | MOD-01 — Autenticación y gestión de roles; transversal a todos los endpoints |
| Stakeholders consultados | Tech Lead AcredIA · @ArchAgent · Jefa DUEA · TI UMSS |

---

## 1. Contexto

AcredIA / SIGESA es una aplicación web con una SPA React en el frontend y una API REST en el backend. El sistema tiene cuatro roles con visibilidades radicalmente distintas: [CC] ve solo su carrera, [TD] tiene visibilidad global, [JD] tiene visibilidad total más funciones administrativas, y [P] accede sin autenticación al portal público.

La autenticación debe cumplir las siguientes restricciones de negocio y técnicas:
- Solo correos institucionales `@umss.edu.bo` son válidos (RB-06, RBN-01).
- El sistema es **web pura sin instalación de software adicional** (PRD-0.1 Principio 3; PRD-NFR-010).
- La UMSS **no dispone de un proveedor de identidad institucional (IdP) centralizado** (SSO/SAML) disponible para el proyecto en v1.0 — confirmado con TI UMSS.
- El frontend (React SPA) se comunica con el backend via API REST; el servidor no gestiona sesiones en memoria.
- El equipo tiene 4 desarrolladores; la solución de autenticación debe implementarse en el sprint inicial sin bloquear otros módulos.
- La seguridad de los datos de acreditación es crítica: 0 incidentes de acceso no autorizado (PRD-NFR-006).

**Fuerzas en tensión:**
- **Seguridad vs. simplicidad**: OAuth2/OIDC es más robusto pero requiere un Identity Provider externo o self-hosted que añade infraestructura y tiempo.
- **Stateless vs. revocación inmediata**: JWT stateless es simple y escala bien, pero revocar un token antes de su expiración requiere una blocklist (complejidad adicional).
- **Tiempo de implementación**: el sprint de autenticación no puede bloquear el desarrollo del repositorio de evidencias (T-04) que está en la ruta crítica.

---

## 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-----------------|
| **A. JWT stateless + RBAC en claims + validación de dominio @umss.edu.bo** | Sin servidor de sesiones; compatible con SPA React; RBAC en el payload del token; fácil de implementar; imagen mental clara para el equipo | Revocación antes de expiración requiere blocklist en BD; si el JWT es robado, es válido hasta `exp` (mitigado con TTL corto de 24h) | $0 |
| **B. Sesiones en servidor (cookies HTTP-only + session store en PostgreSQL o Redis)** | Revocación inmediata (destruir sesión en BD); cookies HTTP-only reducen XSS | Requiere Redis o tabla de sesiones; el backend debe ser stateful; más complejo de escalar horizontalmente; CORS más difícil con SPA | $0 + complejidad |
| **C. OAuth2/OIDC con Identity Provider propio (Keycloak self-hosted)** | Estándar industrial; SSO posible; refresh tokens gestionados por IdP; soporte para federación futura con LDAP UMSS | Agrega Keycloak al `docker-compose` (400 MB RAM mínimo); curva de aprendizaje del equipo; tiempo de configuración ≥ 2 semanas; bloquea el sprint inicial | $0 self-hosted pero alto costo de tiempo |
| **D. Auth-as-a-Service (Firebase Auth, Auth0, Supabase Auth)** | Gestión de usuarios externalizada; MFA incluido; rápida integración | Datos de usuarios salen del servidor institucional (posible restricción TI UMSS); costo OPEX para > 1.000 usuarios; dependencia de tercero | USD 0–50/mes según plan |

---

## 3. Decisión

> **Elegimos la alternativa A: autenticación stateless con JWT (access token TTL 24h + refresh token TTL 7 días) y RBAC embebido en los claims del token, con validación de dominio `@umss.edu.bo` en el backend.**

La combinación de JWT stateless con RBAC en claims es la única alternativa que satisface simultáneamente: cero infraestructura adicional, compatibilidad nativa con la SPA React, implementación en el sprint inicial sin bloquear otros módulos, y control de acceso por `rol` y `carrera_id` embebido en cada request.

La ausencia de un IdP institucional en TI UMSS descarta Keycloak y OAuth2/OIDC en v1.0. La externalización (alternativa D) es incompatible con los requisitos de soberanía de datos institucionales. Las sesiones en servidor (alternativa B) añaden complejidad de gestión de estado que no está justificada para el volumen de usuarios esperado en el piloto (~150 usuarios concurrentes máximo).

El riesgo de tokens no revocables se mitiga con: TTL corto (24h), blocklist en BD solo para logout explícito y bloqueo por intentos fallidos (3 intentos → 15 min, implementado en BD sin Redis), y HTTPS obligatorio con TLS 1.3 (NFR-003).

---

## 4. Consecuencias

### 4.1 Positivas
- El backend es stateless: cualquier instancia del servicio puede validar cualquier JWT sin consultar un servidor de sesiones centralizado.
- El RBAC se evalúa en cada middleware de la API sin consulta adicional a BD: el token contiene `rol`, `carrera_id` y `user_id`.
- La validación de dominio `@umss.edu.bo` es una sola línea de código en el handler de login (AUTH-001).
- El bloqueo por 3 intentos fallidos se implementa con un contador en la tabla `USUARIO.intentos_fallidos` en PostgreSQL — sin Redis, sin infraestructura adicional.
- Los eventos de login/logout/bloqueo quedan en `LOG_AUDITORIA` (ADR-0002) con `ip_origen` para auditoría de seguridad.

### 4.2 Negativas / costos
- Un JWT robado es válido hasta su `exp` (24h). Mitigación: TLS 1.3 obligatorio + `HttpOnly` en el refresh token cookie + rotación del refresh token en cada uso.
- La blocklist de logout se implementa en BD (tabla `REFRESH_TOKEN_BLACKLIST`); es una consulta adicional por cada intento de renovación.
- En v2.0, si TI UMSS habilita LDAP/AD institucional, habrá que migrar la autenticación a OAuth2/OIDC. Esta ADR quedaría superada por ADR-NNNN.

### 4.3 Neutras / observables
- El access token se almacena en memoria del frontend (no en `localStorage`) para reducir el riesgo de XSS.
- El refresh token se almacena en cookie `HttpOnly; Secure; SameSite=Strict`.
- El payload del JWT: `{ user_id, rol, carrera_id, exp }` — mínimo para no exponer datos sensibles.

---

## 5. Impacto en el sistema

- **Código**: `T-02` (FSD) — implementar autenticación JWT con validación de dominio y RBAC. El middleware de autenticación se aplica a todos los endpoints privados; los endpoints del portal público [P] están excluidos (MOD-10).
- **Operaciones**: no requiere servicios adicionales. El contador de intentos fallidos usa la tabla `USUARIO` existente.
- **Seguridad**: TLS 1.3 forzado en el reverse proxy (Nginx/Caddy frente al contenedor backend). Headers de seguridad: `HSTS`, `X-Frame-Options: DENY`, `Content-Security-Policy`. OWASP ZAP en el plan de pruebas (NFR-003).
- **Equipo**: JWT es conocido por el equipo. Biblioteca recomendada: `jsonwebtoken` (Node) o `python-jose` (Python), según resultado del spike de backend.
- **Costo**: $0.

---

## 6. Plan de reversión

**Señales de que la decisión fue incorrecta:**
- TI UMSS habilita un IdP institucional (LDAP/AD) y exige SSO para todos los sistemas universitarios.
- Se detecta un incidente de seguridad relacionado con JWT robados que no puede mitigarse con TTL corto.
- El número de usuarios crece a > 500 concurrentes y el bloqueo por intentos en BD genera contención.

**Costo estimado de revertir:** medio. Migrar a OAuth2/OIDC con Keycloak requiere: configurar Keycloak, migrar usuarios, adaptar el frontend para el flujo de autorización code + PKCE, y actualizar el middleware de validación del backend. Se estima 2–3 sprints.

**Plan B:** Agregar Keycloak al `docker-compose` en v2.0 con federación al LDAP UMSS, superando esta ADR con ADR-NNNN.

---

## 7. Validación

- **TC-001** (plan de pruebas FSD §12.2): login con correo @umss.edu.bo válido redirige al dashboard correcto en < 2 s.
- **TC-002**: login con correo @gmail.com es bloqueado con mensaje claro y sin JWT generado.
- **NFR-003**: 100% de endpoints sensibles con HTTPS forzado + TLS 1.3 — verificación con OWASP ZAP.
- **NFR-006**: 0 incidentes de acceso no autorizado a información restringida — verificación con tests de autorización por rol.
- **Escenarios Gherkin**: FSD-UC-001 §4 — bloqueo por 3 intentos, refresh token expirado, usuario sin rol asignado.
- **Responsable**: @DevAgent (implementación) + @ArchAgent (revisión de seguridad).
- **Plazo**: sprint 1 del delivery track; verificación antes del piloto cerrado Q3 2026.

---

## 8. Referencias

- FSD v1.0 AcredIA/SIGESA — FSD-UC-001, PC-001, §2.3 Stack tecnológico, NFR-003, NFR-004, NFR-006, T-02.
- PRD v1.0 — PRD-REQ-001, PRD-REQ-002, PRD-NFR-005, PRD-NFR-006.
- BRD v2.0 — BR-006, RB-06.
- OWASP JWT Security Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html
- RFC 7519 (JWT): https://tools.ietf.org/html/rfc7519
- ADR relacionado: ADR-0002 (Log de auditoría append-only en PostgreSQL).

---

## 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 16/05/2026 | Equipo AcredIA | Propuesta inicial |
| 2 | 16/05/2026 | Equipo AcredIA | Aceptada — confirma la elección implícita de FSD v1.0 PC-001 con justificación formal y análisis de alternativas |