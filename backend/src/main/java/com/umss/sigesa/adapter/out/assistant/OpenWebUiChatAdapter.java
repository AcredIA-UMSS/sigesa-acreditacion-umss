package com.umss.sigesa.adapter.out.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.umss.sigesa.application.port.out.ChatCompletionPort;
import com.umss.sigesa.config.AssistantProperties;
import com.umss.sigesa.domain.exception.AssistantCompletionException;
import com.umss.sigesa.domain.exception.AssistantUnavailableException;
import com.umss.sigesa.domain.model.ChatMessage;
import com.umss.sigesa.domain.model.ChatRole;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Component
public class OpenWebUiChatAdapter implements ChatCompletionPort {

    private static final Duration TIMEOUT = Duration.ofSeconds(120);

    private final AssistantProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    public OpenWebUiChatAdapter(AssistantProperties properties) {
        this.properties = properties;
        // Open WebUI (uvicorn) rejects Java's default HTTP/2 requests with "Invalid HTTP request received."
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(TIMEOUT)
                .build();
    }

    @Override
    public String complete(List<ChatMessage> messages) {
        if (!properties.isEnabled()) {
            throw new AssistantUnavailableException("El asistente está deshabilitado.");
        }
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new AssistantUnavailableException(
                    "El asistente no está configurado. Defina SIGESA_ASSISTANT_API_KEY.");
        }

        try {
            String requestBody = buildRequestBody(messages);
            String endpoint = normalizeBaseUrl(properties.getBaseUrl()) + "/v1/chat/completions";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 401 || response.statusCode() == 403) {
                throw new AssistantUnavailableException(
                        "API key de Open WebUI inválida. Genere una nueva en Open WebUI y actualice SIGESA_ASSISTANT_API_KEY.");
            }

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new AssistantCompletionException(
                        "Open WebUI respondió con HTTP " + response.statusCode() + ": " + truncate(response.body()));
            }

            return extractAssistantReply(response.body());
        } catch (AssistantUnavailableException | AssistantCompletionException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AssistantCompletionException("La solicitud al asistente fue interrumpida.", ex);
        } catch (Exception ex) {
            throw new AssistantCompletionException(
                    "No se pudo conectar con Open WebUI. Verifique que el servicio esté activo.", ex);
        }
    }

    private String buildRequestBody(List<ChatMessage> messages) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", properties.getModel());
        root.put("stream", false);

        ArrayNode messagesNode = root.putArray("messages");
        for (ChatMessage message : messages) {
            ObjectNode messageNode = messagesNode.addObject();
            messageNode.put("role", message.role().name().toLowerCase());
            messageNode.put("content", message.content());
        }

        return objectMapper.writeValueAsString(root);
    }

    private String extractAssistantReply(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode content = root.path("choices").path(0).path("message").path("content");

        if (content.isMissingNode() || content.asText().isBlank()) {
            throw new AssistantCompletionException("Open WebUI no devolvió contenido en la respuesta.");
        }

        return content.asText();
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private static String truncate(String value) {
        if (value == null) {
            return "";
        }
        return value.length() <= 200 ? value : value.substring(0, 200) + "…";
    }
}
