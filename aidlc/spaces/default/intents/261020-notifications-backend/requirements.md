# Requirements Specification: Backend Notification & Outbox System

- **Intent:** 261020-notifications-backend
- **Scope:** Express (Lightweight)
- **Module:** `MOD-NOTIFY`
- **Traceability:** FSD-UC-015 · PRD-US-017, PRD-US-018, PRD-US-019 · FEAT-05

## 1. Executive Summary

Implement an asynchronous transactional notification and outbox system for the accreditation platform (SIGESA-UMSS). This feature allows the system to emit alerts upon evidence observation, approval, or deadline proximity, persisting them atomically with business transactions and exposing REST query and mark-as-read endpoints for the career coordinator (`[CC]`).

## 2. Functional Requirements (FR)

- **FR-01: Transactional Outbox Persistence.** System must atomically persist notification records to `notification_outbox` during domain state changes (e.g., evidence observation, phase rejection, technical review).
- **FR-02: Authenticated Notifications Query.** Expose `GET /api/v1/notifications/me` with pagination (`Pageable`), program-scope filtering, and optional `unreadOnly=true` filter.
- **FR-03: Unread Notifications Count.** Expose `GET /api/v1/notifications/unread-count` returning `{ count: N }` for UI badges.
- **FR-04: Mark Notifications as Read.** Expose `PATCH /api/v1/notifications/{id}/read` to set `read_at = NOW()` and `POST /api/v1/notifications/mark-all-read` for bulk updates.
- **FR-05: Asynchronous Delivery Worker.** Scheduled background worker polling pending notifications using `SELECT ... FOR UPDATE SKIP LOCKED` and dispatching them through `NotificationSenderPort` with exponential backoff and jitter.

## 3. Non-Functional Requirements (NFR)

- **NFR-01: Concurrency & I/O Decoupling.** SMTP / remote delivery must never run within the primary JPA business transaction ($W_{db} < 5$ ms). HikariCP connections must be released immediately upon transaction commit.
- **NFR-02: Stateless & Multi-Instance Safety.** Worker must support multiple backend instances concurrently without duplicate deliveries or deadlocks via PostgreSQL row-level locks (`SKIP LOCKED`).
- **NFR-03: Retry Policy & Resilience.** Maximum 5 retry attempts with exponential backoff and randomized jitter on delivery failure before marking status as `FAILED`.
- **NFR-04: Strict Hexagonal Architecture & Code Quality.** No JPA annotations in domain/use case layers; explicit mapping between JPA entities and domain models; test coverage > 90%.
