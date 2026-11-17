package com.umss.sigesa.application.port.out;

public final class NoOpAccessSessionPort implements AccessSessionPort {

    public static final NoOpAccessSessionPort INSTANCE = new NoOpAccessSessionPort();

    private NoOpAccessSessionPort() {
    }

    @Override
    public java.util.UUID openSuccessful(java.util.UUID userId, String fullName, String email, String role) {
        return null;
    }

    @Override
    public void recordFailure(String email) {
    }

    @Override
    public void close(java.util.UUID sessionId, com.umss.sigesa.domain.model.access.AccessSessionEndReason reason) {
    }

    @Override
    public void revokeActiveByUser(java.util.UUID userId) {
    }

    @Override
    public java.util.Optional<com.umss.sigesa.domain.model.access.AccessSession> findById(java.util.UUID sessionId) {
        return java.util.Optional.empty();
    }

    @Override
    public org.springframework.data.domain.Page<com.umss.sigesa.domain.model.access.AccessSession> search(
            String email, String role, com.umss.sigesa.domain.model.access.AccessSessionStatus status, String ipAddress,
            java.time.LocalDateTime from, java.time.LocalDateTime to, org.springframework.data.domain.Pageable pageable) {
        return org.springframework.data.domain.Page.empty(pageable);
    }

    @Override
    public long countAll() {
        return 0;
    }

    @Override
    public long countByStatus(com.umss.sigesa.domain.model.access.AccessSessionStatus status) {
        return 0;
    }

    @Override
    public long countStartedSince(java.time.LocalDateTime from) {
        return 0;
    }

    @Override
    public long countDistinctActiveUsers() {
        return 0;
    }
}
