package com.umss.sigesa.adapter.out.notification;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Breaker mínimo (cerrado / abierto / medio-abierto) para el cliente HTTP del E3.
 * Sin Resilience4j: el experimento pide un mecanismo, no una librería nueva.
 */
final class FailureCircuitBreaker {

    enum State {
        CLOSED, OPEN, HALF_OPEN
    }

    private final int failureThreshold;
    private final Duration openDuration;
    private final AtomicInteger consecutiveFailures = new AtomicInteger();
    private final AtomicLong openedAtNanos = new AtomicLong();
    private volatile State state = State.CLOSED;

    FailureCircuitBreaker(int failureThreshold, Duration openDuration) {
        this.failureThreshold = Math.max(1, failureThreshold);
        this.openDuration = openDuration;
    }

    synchronized boolean allowRequest() {
        if (state == State.CLOSED) {
            return true;
        }
        if (state == State.OPEN) {
            if (System.nanoTime() - openedAtNanos.get() >= openDuration.toNanos()) {
                state = State.HALF_OPEN;
                return true;
            }
            return false;
        }
        return true;
    }

    synchronized void onSuccess() {
        consecutiveFailures.set(0);
        state = State.CLOSED;
    }

    synchronized void onFailure() {
        int failures = consecutiveFailures.incrementAndGet();
        if (failures >= failureThreshold || state == State.HALF_OPEN) {
            state = State.OPEN;
            openedAtNanos.set(System.nanoTime());
        }
    }

    State state() {
        return state;
    }
}
