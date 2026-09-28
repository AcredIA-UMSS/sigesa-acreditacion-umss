# Contratos JSON — Auth (Planner)

## Application Overview

`POST /api/v1/auth/login` serializa `LoginRequest` / `LoginResponse` en camelCase.

## Test Scenarios

### 1. Login

#### 1.1. login-request-email-password

**File:** `backend/src/test/java/com/sigesa/app/contracts/auth/LoginJsonContractTest.java`

**Steps:**
  1. Serializar `LoginRequest`.
    - expect: campos `email`, `password`

#### 1.2. login-response-token-role-scope

**File:** `backend/src/test/java/com/sigesa/app/contracts/auth/LoginJsonContractTest.java`

**Steps:**
  1. Serializar `LoginResponse`.
    - expect: `accessToken`, `expiresIn` number, `role`, `programScope` array UUID string
