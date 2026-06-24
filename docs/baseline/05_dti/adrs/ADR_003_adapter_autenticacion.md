# ADR_003: Patrón Adapter para autenticación (local v1.0 → LDAP v1.1)

| Campo | Valor |
|-------|-------|
| **Canónico** | [ADR-0003](../../adr/ADR-0003-authentication-adapter.md) |
| **Estado** | **Aceptada** |
| **Fecha** | 2026-05-17 |
| **Alcance** | MOD-AUTH · FSD-UC-001 |
| **Trazabilidad** | BRD Q-02 · PRD-REQ-001 · FSD-UC-001 |

## Contexto

El piloto SIGESA debe validar flujos de acreditación con usuarios reales de la DUEA y carreras piloto antes de que TI UMSS entregue integración SSO/LDAP institucional. El BRD documenta la pregunta abierta Q-02: credenciales locales en fase piloto frente a directorio corporativo. Retrasar el MVP hasta disponibilidad de IdP centralizado bloquearía FSD-UC-002 en adelante (carga de Evidencia, workflow del [TD]) y contradice el calendario Q3–Q4 2026.

Simultáneamente, acoplar la lógica de dominio (`AuthenticateUserUseCase`, asignación de roles [CC]/[TD]/[JD]) a un proveedor concreto (tabla local vs. bind LDAP) generaría deuda: un cambio de TI UMSS en v1.1 forzaría reescribir controladores y tests. El patrón **puerto/adaptador** aísla la verificación de credenciales detrás de `AuthPort` / `AuthenticationProvider`, mientras la sesión API y el RBAC permanecen en la capa descrita en [ADR_007](ADR_007_jwt_rbac.md).

Restricciones de negocio: solo correos `@umss.edu.bo` (FSD, PRD); dominio validado en adaptador y en registro de usuarios.

## Alternativas consideradas

| Alternativa | Pros | Contras | Veredicto |
|-------------|------|---------|-----------|
| **A. `LocalAuthAdapter` v1.0 + `LdapAuthAdapter` v1.1** | Piloto no bloqueado; migración = nuevo adaptador | Doble suite de pruebas temporal | **Elegida** |
| **B. Esperar LDAP para v1.0** | Una sola implementación auth | Riesgo calendario alto | Rechazada |
| **C. Auth embebida sin interfaz en controladores** | Menos archivos al inicio | Refactor costoso en v1.1 | Rechazada |
| **D. Keycloak self-hosted desde v1.0** | OIDC estándar | +400 MB RAM, semanas de config (ver ADR_007) | Rechazada para v1.0 |

## Decisión

1. **v1.0 — `LocalAuthAdapter`:** autenticación contra `app_user` en PostgreSQL; contraseña con **Argon2id**; solo emails `@umss.edu.bo`.
2. **v1.1 — `LdapAuthAdapter`:** mismo contrato `AuthPort`; atributos LDAP mapean a `role` y `programScope` sin cambiar casos de uso de Evidencia ni workflow.
3. Tras autenticación exitosa, emisión de JWT y aplicación de RBAC según ADR_007 (no responsabilidad del adaptador LDAP).
4. Variables de entorno seleccionan adaptador: `AUTH_PROVIDER=local|ldap` sin recompilar.

## Consecuencias

### Positivas

- El equipo puede desarrollar MOD-EVIDENCE y MOD-WORKFLOW en paralelo al trámite LDAP con TI.
- Pruebas de integración usan usuarios seed locales reproducibles en Docker Compose.

### Negativas

- Política de contraseñas locales puede diferir de UMSS hasta v1.1; comunicar al [JD] en capacitación piloto.
- Riesgo de cuentas locales huérfanas tras migración LDAP: plan de migración debe incluir desactivación (`PATCH deactivate`) conservando historial.

## Referencias

- [`docs/04_fsd/api_contracts.md`](../../04_fsd/api_contracts.md) — API-AUTH-01
- [`docs/05_dti/adrs/ADR_007_jwt_rbac.md`](ADR_007_jwt_rbac.md)
- Equipo: `team/aylenGonzales/09_dti/DTI_v1.md` §8 (Identidad)
