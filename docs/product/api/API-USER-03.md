---
id: API-USER-03
title: Listar usuarios administrados [JD]
method: GET
path: /api/v1/admin/users
modulo: MOD-AUTH
fsd_uc: FSD-UC-002
design_doc: DD-UC-002
implementacion: ListUsersUseCase / UserAdminController
tool_assistant: list_users
status: Implemented
ultima_actualizacion: "2026-07-31"
---

# API-USER-03 — `GET /api/v1/admin/users`

Contrato REST para **listar usuarios internos** registrados en SIGESA, con filtros opcionales por rol y estado. Exclusivo para **Jefatura DUEA [JD]**.

---

## 1. Resumen

| Campo | Valor |
|-------|-------|
| **ID contrato** | `API-USER-03` |
| **Método / Ruta** | `GET /api/v1/admin/users` |
| **Base URL** | `/api/v1` |
| **Caso de uso** | [FSD-UC-002](../uc/FSD-UC-002.md) — Gestión de usuarios [JD] |
| **Design doc** | [DD-UC-002](../../design/DD-UC-002.md) |
| **Controlador** | `UserAdminController.list()` |
| **Caso de uso app** | `ListUsersUseCase` → `ListUsersService` |
| **Side-effect** | Ninguno (solo lectura) |
| **Tool asistente** | `list_users` — [TOOL-CATALOG](../../design/assistant/TOOL-CATALOG.md) |

---

## 2. Autenticación y autorización

| Aspecto | Regla |
|---------|-------|
| **Autenticación** | JWT Bearer obligatorio (`Authorization: Bearer {accessToken}`) |
| **Roles permitidos** | **`JD` exclusivamente** |
| **Config seguridad** | `SecurityConfig`: `.requestMatchers("/api/v1/admin/users", "/api/v1/admin/users/**").hasRole("JD")` |
| **Sin token** | `401` — `{ "error": "UNAUTHORIZED", "message": "No autenticado" }` |
| **Token válido, rol CC/TD** | `403 Forbidden` (Spring Security) |
| **Datos sensibles excluidos** | `passwordHash`, `failedAttempts`, `lockedUntil` **nunca** se exponen |

---

## 3. Query parameters

Todos opcionales. Combinables.

| Parámetro | Tipo | Requerido | Valores permitidos | Descripción |
|-----------|------|-----------|-------------------|-------------|
| `role` | `string` | No | `JD`, `CC`, `TD` | Filtra por rol de usuario (case-insensitive en servidor) |
| `status` | `string` | No | `INACTIVE`, `ACTIVE`, `DEACTIVATED` | Filtra por estado de cuenta (case-insensitive en servidor) |

### Semántica de filtros

| `role` | Significado |
|--------|-------------|
| `JD` | Jefatura DUEA |
| `CC` | Coordinador de Carrera |
| `TD` | Técnico DUEA |

| `status` | Significado |
|----------|-------------|
| `INACTIVE` | Cuenta creada; pendiente de primer acceso |
| `ACTIVE` | Cuenta operativa |
| `DEACTIVATED` | Cuenta revocada (soft delete); historial conservado |

Sin parámetros → devuelve **todos** los usuarios que cumplan visibilidad admin (sin paginación en v1.0).

---

## 4. Respuesta exitosa — `200 OK`

**Content-Type:** `application/json`

Cuerpo: **array** de objetos `UserAdminSummaryResponse` (puede ser vacío `[]`).

### 4.1 Schema

```json
[
  {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "cc@umss.edu.bo",
    "role": "CC",
    "status": "ACTIVE",
    "programIds": [
      "550e8400-e29b-41d4-a716-446655440000"
    ]
  }
]
```

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `userId` | `UUID` (string) | Sí | Identificador único del usuario |
| `email` | `string` | Sí | Correo institucional `@umss.edu.bo` |
| `role` | `string` | Sí | `JD` \| `CC` \| `TD` |
| `status` | `string` | Sí | `INACTIVE` \| `ACTIVE` \| `DEACTIVATED` |
| `programIds` | `UUID[]` | Sí | IDs de programas con asignación activa (`revoked_at IS NULL`). Vacío para `JD`/`TD` |

### 4.2 Ejemplo — listado completo

**Request:**
```http
GET /api/v1/admin/users HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Accept: application/json
```

**Response `200`:**
```json
[
  {
    "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "email": "jd@umss.edu.bo",
    "role": "JD",
    "status": "ACTIVE",
    "programIds": []
  },
  {
    "userId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "email": "cc@umss.edu.bo",
    "role": "CC",
    "status": "ACTIVE",
    "programIds": ["550e8400-e29b-41d4-a716-446655440000"]
  },
  {
    "userId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "email": "pendiente@umss.edu.bo",
    "role": "CC",
    "status": "INACTIVE",
    "programIds": ["770e8400-e29b-41d4-a716-446655440002"]
  }
]
```

### 4.3 Ejemplo — filtro por rol y estado

**Request:**
```http
GET /api/v1/admin/users?role=CC&status=ACTIVE HTTP/1.1
Authorization: Bearer {token}
```

**Response `200`:** subset con solo coordinadores activos.

---

## 5. Errores

Formato estándar MOD-AUTH:

```json
{
  "error": "ERROR_CODE",
  "message": "Descripción legible"
}
```

| HTTP | Código `error` | Condición | Ejemplo `message` |
|------|----------------|-----------|-------------------|
| **401** | `UNAUTHORIZED` | Sin Bearer token o token inválido/expirado | `"No autenticado"` |
| **403** | — | Rol distinto de `JD` (CC, TD) | Cuerpo vacío o HTML según Spring Security |
| **422** | `INVALID_ROLE` | Query `role` con valor no enum | `"Rol de filtro inválido: XX"` |
| **422** | `INVALID_FILTER` | Query `status` con valor no enum | `"Estado de filtro inválido: XX. Valores permitidos: INACTIVE, ACTIVE, DEACTIVATED."` |

### Ejemplo — filtro de rol inválido

**Request:**
```http
GET /api/v1/admin/users?role=ADMIN HTTP/1.1
Authorization: Bearer {token-jd}
```

**Response `422`:**
```json
{
  "error": "INVALID_ROLE",
  "message": "Rol de filtro inválido: ADMIN"
}
```

---

## 6. OpenAPI 3.0 (fragmento)

```yaml
paths:
  /api/v1/admin/users:
    get:
      operationId: listAdminUsers
      summary: Listar usuarios registrados [JD]
      tags: [MOD-AUTH, Admin Users]
      security:
        - bearerAuth: []
      x-allowed-roles: [JD]
      parameters:
        - name: role
          in: query
          required: false
          schema:
            type: string
            enum: [JD, CC, TD]
          description: Filtro opcional por rol
        - name: status
          in: query
          required: false
          schema:
            type: string
            enum: [INACTIVE, ACTIVE, DEACTIVATED]
          description: Filtro opcional por estado de cuenta
      responses:
        "200":
          description: Listado de usuarios
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: "#/components/schemas/UserAdminSummaryResponse"
        "401":
          description: No autenticado
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/Error"
              example:
                error: UNAUTHORIZED
                message: No autenticado
        "403":
          description: Rol insuficiente (requiere JD)
        "422":
          description: Filtro de query inválido
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/Error"

components:
  schemas:
    UserAdminSummaryResponse:
      type: object
      required: [userId, email, role, status, programIds]
      properties:
        userId:
          type: string
          format: uuid
        email:
          type: string
          format: email
          example: cc@umss.edu.bo
        role:
          type: string
          enum: [JD, CC, TD]
        status:
          type: string
          enum: [INACTIVE, ACTIVE, DEACTIVATED]
        programIds:
          type: array
          items:
            type: string
            format: uuid
          description: Programas con asignación activa (CC)
```

---

## 7. Reglas de negocio

| ID | Regla |
|----|-------|
| **RB-01** | Solo [JD] puede consultar el listado global de usuarios. |
| **RB-02** | `programIds` refleja únicamente asignaciones activas (`user_program_assignment.revoked_at IS NULL`). |
| **RB-03** | Usuarios `DEACTIVATED` permanecen en el listado (no se ocultan); el filtro `status=DEACTIVATED` los aísla. |
| **RB-04** | No hay paginación en v1.0; el volumen esperado es acotado (decenas de usuarios institucionales). |
| **RB-05** | El endpoint es **idempotente** y seguro para reintentos (GET). |

---

## 8. Implementación backend

| Artefacto | Ubicación |
|-----------|-----------|
| Controller | `adapter/in/web/UserAdminController.java` |
| DTO respuesta | `adapter/in/web/dto/UserAdminSummaryResponse.java` |
| Puerto | `application/port/in/ListUsersUseCase.java` |
| Servicio | `application/service/auth/ListUsersService.java` |
| Repositorio | `UserRepositoryPort.findAllFiltered(role, status)` |
| Asignaciones | `UserProgramAssignmentRepositoryPort.findActiveByUserId()` |

### Flujo interno

```mermaid
sequenceDiagram
  participant C as UserAdminController
  participant UC as ListUsersService
  participant UR as UserRepositoryPort
  participant PA as AssignmentRepositoryPort

  C->>UC: list(roleFilter, statusFilter)
  UC->>UC: parseRole / parseStatus
  UC->>UR: findAllFiltered(role, status)
  UR-->>UC: List AppUser
  loop por usuario
    UC->>PA: findActiveByUserId(userId)
    PA-->>UC: programIds
  end
  UC-->>C: List UserSummary
  C-->>C: map → UserAdminSummaryResponse[]
```

---

## 9. Consumidores

| Consumidor | Uso |
|------------|-----|
| **Frontend** | `/admin/users` — tabla de gestión ([JD]) vía Orval `user-admin-controller` |
| **Asistente virtual** | Tool `list_users` — [TOOL-CATALOG](../../design/assistant/TOOL-CATALOG.md) |
| **Tests** | `UserAdminControllerTest.list_*` |

---

## 10. Matriz de pruebas de contrato

| # | Escenario | Auth | Query | HTTP esperado |
|---|-----------|------|-------|---------------|
| T1 | Listado sin filtros | JD | — | 200 + array |
| T2 | Sin autenticación | — | — | 401 `UNAUTHORIZED` |
| T3 | Rol CC | CC | — | 403 |
| T4 | Rol TD | TD | — | 403 |
| T5 | Filtro `role=CC` | JD | `role=CC` | 200 subset |
| T6 | Filtro `status=INACTIVE` | JD | `status=INACTIVE` | 200 subset |
| T7 | `role=INVALID` | JD | `role=INVALID` | 422 `INVALID_ROLE` |
| T8 | `status=INVALID` | JD | `status=INVALID` | 422 `INVALID_FILTER` |
| T9 | Listado vacío | JD | filtros sin match | 200 `[]` |

---

## 11. Relación con otros contratos

| Contrato | Relación |
|----------|----------|
| [API-USER-01](../api_contracts.md#api-user-01--post-adminusers) | Alta de usuario; el listado muestra el resultado |
| [API-USER-02](../api_contracts.md#api-user-02--patch-adminusersiddeactivate) | Desactivación; usuarios pasan a `DEACTIVATED` visibles vía filtro |
| [API-AUTH-01](../api_contracts.md#api-auth-01--post-authlogin) | Emite JWT necesario para invocar este endpoint |
| [API-CAT-01](../api_contracts.md#api-cat-01--get-programs) | IDs en `programIds` referencian catálogo de programas |

---

## 12. Registro de cambios

| Versión | Fecha | Cambio |
|---------|-------|--------|
| 1.0 | 2026-06-26 | Implementación inicial (`PR-IMPL-006`); listado + filtros |
| 1.1 | 2026-07-31 | Contrato formal `API-USER-03`; trazabilidad tool `list_users` |
