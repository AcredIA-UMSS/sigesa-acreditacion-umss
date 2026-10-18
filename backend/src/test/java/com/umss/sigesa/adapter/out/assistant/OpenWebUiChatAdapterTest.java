package com.umss.sigesa.adapter.out.assistant;

import com.sun.net.httpserver.HttpServer;
import com.umss.sigesa.application.model.assistant.ChatCompletionRequest;
import com.umss.sigesa.config.AssistantProperties;
import com.umss.sigesa.domain.exception.AssistantGatewayTimeoutException;
import com.umss.sigesa.domain.model.ChatMessage;
import com.umss.sigesa.domain.model.ChatRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("OpenWebUiChatAdapter — PR-IMPL-042 / #52")
class OpenWebUiChatAdapterTest {

    @Test
    @DisplayName("timeout de lectura → AssistantGatewayTimeoutException")
    void complete_whenServerSlow_throwsGatewayTimeout() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
        try {
            AssistantProperties properties = new AssistantProperties();
            properties.setEnabled(true);
            properties.setApiKey("test");
            properties.setTimeoutMs(200);
            properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
            OpenWebUiChatAdapter adapter = new OpenWebUiChatAdapter(properties);
            ChatCompletionRequest request = new ChatCompletionRequest(
                    List.of(new ChatMessage(ChatRole.USER, "hola")), List.of());
            assertThrows(AssistantGatewayTimeoutException.class, () -> adapter.complete(request));
        } finally {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("respuesta 200 con content")
    void complete_returnsContent() throws Exception {
        String payload = "{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}";
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            byte[] body = payload.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            AssistantProperties properties = new AssistantProperties();
            properties.setEnabled(true);
            properties.setApiKey("test");
            properties.setTimeoutMs(5000);
            properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
            OpenWebUiChatAdapter adapter = new OpenWebUiChatAdapter(properties);
            var result = adapter.complete(new ChatCompletionRequest(
                    List.of(new ChatMessage(ChatRole.USER, "hola")), List.of()));
            assertEquals("ok", result.content());
        } finally {
            server.stop(0);
        }
    }
}
