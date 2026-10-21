# Code Generation Plan: Backend Notification System

- **Intent:** 261020-notifications-backend
- **Scope Profile:** Express
- **Target Module:** `MOD-NOTIFY`

## Generation Phases & Tasks

### Phase 1: Database Migration
- [x] Create Flyway migration script `V24__create_notification_outbox.sql` for transactional outbox persistence.
- [x] Create Flyway migration script `V26__notification_read_status.sql` adding `read_at` column and performance indexes.

### Phase 2: Domain & Persistence Layers
- [x] Define domain entity `NotificationItem` and status enumerations.
- [x] Implement JPA entity `NotificationOutboxJpaEntity` with audit columns and retry counters.
- [x] Implement Spring Data JPA repository `NotificationOutboxJpaRepository` with custom `@Query` for scoped pagination and `SKIP LOCKED`.

### Phase 3: Application Ports & Services
- [x] Define incoming port `GetNotificationsUseCase` for inbox queries and read-status mutations.
- [x] Define outgoing port `NotificationQueryPort` and `NotificationSenderPort`.
- [x] Implement application service `GetNotificationsService`.

### Phase 4: Web Adapters & DTOs
- [x] Implement REST controller `NotificationController` mapping `/api/v1/notifications/*`.
- [x] Create response DTOs: `NotificationResponseDto`, `NotificationUnreadCountDto`.
- [x] Wire Spring configuration bean in `NotificationModuleConfig`.

### Phase 5: Asynchronous Worker
- [x] Implement `NotificationOutboxWorker` with `@Scheduled` batch processing.
- [x] Integrate exponential backoff + jitter retry strategy.

### Phase 6: Automated Verification & Unit Tests
- [x] Unit test web controller `NotificationControllerTest`.
- [x] Unit test application service `GetNotificationsServiceTest`.
- [x] Unit test outbox persistence adapter `JpaNotificationOutboxAdapterTest`.
- [x] Unit test worker resilience `NotificationOutboxWorkerTest`.
