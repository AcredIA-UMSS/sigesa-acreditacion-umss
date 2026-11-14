# Contratos JSON — Assistant (Planner)

## Application Overview

`POST /api/v1/assistant/chat` — `SendChatMessageRequest` / `SendChatMessageResponse`.  
Schema: `docs/qa/schemas/send-chat-message-*.schema.json`. No asertar redacción de `reply`.

## Test Scenarios

### 1. Request

#### 1.1. chat-request-message-history-context

**File:** `backend/src/test/java/com/sigesa/app/contracts/assistant/SendChatMessageJsonContractTest.java`

**Steps:**
  1. Serializar request con agent `evidence`.
    - expect: `message`, `history[].role|content`, `context.agent` ∈ general|phases|users|evidence

### 2. Response

#### 2.1. chat-response-path-steps

**File:** `backend/src/test/java/com/sigesa/app/contracts/assistant/SendChatMessageJsonContractTest.java`

**Steps:**
  1. Serializar response KEYWORD.
    - expect: required `reply`, `path`, `llmInvoked`, `steps`; `path` ∈ KEYWORD|LLM|RAG|OUT_OF_SCOPE
    - expect: no oráculo sobre el texto de `reply`
