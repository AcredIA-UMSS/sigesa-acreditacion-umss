package com.umss.sigesa.adapter.out.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.application.port.in.NotificationModulePort;
import com.umss.sigesa.domain.exception.NotificationModuleUnavailableException;
import com.umss.sigesa.domain.model.NotificationModuleSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class HttpNotificationModuleAdapter implements NotificationModulePort {

    private static final Logger log = LoggerFactory.getLogger(HttpNotificationModuleAdapter.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final URI statusUri;
    private final Duration attemptTimeout;
    private final int maxAttempts;
    private final Duration retryBackoff;
    private final FailureCircuitBreaker circuit;

    public HttpNotificationModuleAdapter(
            HttpClient httpClient,
            ObjectMapper objectMapper,
            String baseUrl,
            Duration attemptTimeout,
            int maxAttempts,
            Duration retryBackoff,
            int circuitFailures,
            Duration circuitOpen) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        String root = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.statusUri = URI.create(root + "/internal/notifications/status");
        this.attemptTimeout = attemptTimeout;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.retryBackoff = retryBackoff;
        this.circuit = new FailureCircuitBreaker(circuitFailures, circuitOpen);
    }

    @Override
    public NotificationModuleSnapshot inspect(Duration deadline) {
        if (!circuit.allowRequest()) {
            throw new NotificationModuleUnavailableException("Circuit breaker open for notification module");
        }
        Duration budget = deadline == null ? attemptTimeout : deadline;
        Duration perAttempt = budget.compareTo(attemptTimeout) < 0 ? budget : attemptTimeout;
        RuntimeException last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                NotificationModuleSnapshot remote = fetch(perAttempt);
                circuit.onSuccess();
                return new NotificationModuleSnapshot(remote.pendingCount(), remote.totalCount(), "http");
            } catch (RuntimeException ex) {
                last = ex;
                circuit.onFailure();
                log.warn("Notification HTTP attempt {}/{} failed: {}", attempt, maxAttempts, ex.getMessage());
                if (attempt < maxAttempts) {
                    sleepBackoff(attempt);
                }
            }
        }
        throw new NotificationModuleUnavailableException(
                "Notification module unreachable after " + maxAttempts + " attempts", last);
    }

    private NotificationModuleSnapshot fetch(Duration timeout) {
        HttpRequest request = HttpRequest.newBuilder(statusUri)
                .timeout(timeout)
                .GET()
                .header("Accept", "application/json")
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 500 || response.statusCode() == 408) {
                throw new NotificationModuleUnavailableException(
                        "Notification module HTTP " + response.statusCode());
            }
            if (response.statusCode() != 200) {
                throw new NotificationModuleUnavailableException(
                        "Notification module HTTP " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), NotificationModuleSnapshot.class);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new NotificationModuleUnavailableException("Interrupted calling notification module", ex);
        } catch (IOException ex) {
            throw new NotificationModuleUnavailableException("I/O calling notification module", ex);
        }
    }

    private void sleepBackoff(int attempt) {
        long jitter = ThreadLocalRandom.current().nextLong(0, Math.max(1, retryBackoff.toMillis()));
        long sleepMs = retryBackoff.toMillis() * attempt + jitter;
        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new NotificationModuleUnavailableException("Interrupted during retry backoff", ex);
        }
    }
}
