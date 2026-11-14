# Contratos JSON — Evidence (Planner)

## Application Overview

`GET /api/v1/indicators/uploadable` → `UploadableIndicatorResponse`.  
`POST /api/v1/indicators/{id}/evidences` → `UploadEvidenceResponse` (FSD-UC-004).

## Test Scenarios

### 1. Indicadores cargables

#### 1.1. uploadable-indicator-estado-criterio

**File:** `backend/src/test/java/com/sigesa/app/contracts/evidence/EvidenceJsonContractTest.java`

**Steps:**
  1. Serializar indicador uploadable.
    - expect: `indicatorId`, `currentState`, `criterionId` (fuente de data, no copy UI)

### 2. Carga

#### 2.1. upload-evidence-response-v1

**File:** `backend/src/test/java/com/sigesa/app/contracts/evidence/EvidenceJsonContractTest.java`

**Steps:**
  1. Serializar respuesta de carga.
    - expect: `evidenceId`, `version`, `contentHash`, `event`, `currentState`
