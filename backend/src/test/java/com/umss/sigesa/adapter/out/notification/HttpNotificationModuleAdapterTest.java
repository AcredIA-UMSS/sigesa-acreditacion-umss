package com.umss.sigesa.adapter.out.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.umss.sigesa.domain.exception.NotificationModuleUnavailableException;
import com.umss.sigesa.domain.model.NotificationModuleSnapshot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("HttpNotificationModuleAdapter — E3")
class HttpNotificationModuleAdapterTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("GET remoto 200 mapea snapshot con origin=http")
    void inspect_mapsRemotePayload() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/internal/notifications/status", exchange -> {
            byte[] body = "{\"pendingCount\":2,\"totalCount\":5,\"origin\":\"extracted\"}".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        HttpNotificationModuleAdapter adapter = adapterFor(server.getAddress().getPort(), 2, 5);
        NotificationModuleSnapshot snapshot = adapter.inspect(Duration.ofMillis(800));

        assertEquals(2L, snapshot.pendingCount());
        assertEquals(5L, snapshot.totalCount());
        assertEquals("http", snapshot.origin());
    }

    @Test
    @DisplayName("Reintenta tras 500 y luego OK")
    void inspect_retriesOnServerError() throws Exception {
        AtomicInteger hits = new AtomicInteger();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/internal/notifications/status", exchange -> {
            int n = hits.incrementAndGet();
            if (n == 1) {
                exchange.sendResponseHeaders(500, 0);
                exchange.close();
                return;
            }
            byte[] body = "{\"pendingCount\":1,\"totalCount\":1,\"origin\":\"extracted\"}".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        NotificationModuleSnapshot snapshot = adapterFor(server.getAddress().getPort(), 2, 5)
                .inspect(Duration.ofMillis(800));

        assertEquals(1L, snapshot.pendingCount());
        assertEquals(2, hits.get());
    }

    @Test
    @DisplayName("Agota reintentos y lanza NotificationModuleUnavailable")
    void inspect_failsAfterRetries() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/internal/notifications/status", exchange -> {
            exchange.sendResponseHeaders(503, 0);
            exchange.close();
        });
        server.start();

        HttpNotificationModuleAdapter adapter = adapterFor(server.getAddress().getPort(), 2, 50);
        assertThrows(NotificationModuleUnavailableException.class,
                () -> adapter.inspect(Duration.ofMillis(800)));
    }

    private static HttpNotificationModuleAdapter adapterFor(int port, int retries, int circuitFailures) {
        return new HttpNotificationModuleAdapter(
                HttpClient.newHttpClient(),
                new ObjectMapper(),
                "http://127.0.0.1:" + port,
                Duration.ofMillis(400),
                retries,
                Duration.ofMillis(5),
                circuitFailures,
                Duration.ofMillis(2_000));
    }
}
