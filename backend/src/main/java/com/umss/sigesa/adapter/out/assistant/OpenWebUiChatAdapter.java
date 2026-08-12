package com.umss.sigesa.adapter.out.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.umss.sigesa.application.model.assistant.AssistantToolDefinition;
import com.umss.sigesa.application.model.assistant.ChatCompletionRequest;
import com.umss.sigesa.application.model.assistant.ChatCompletionResult;
import com.umss.sigesa.application.model.assistant.ToolCall;
import com.umss.sigesa.application.port.out.ChatCompletionPort;
import com.umss.sigesa.config.AssistantProperties;
import com.umss.sigesa.domain.exception.AssistantCompletionException;
import com.umss.sigesa.domain.exception.AssistantUnavailableException;
import com.umss.sigesa.domain.model.ChatMessage;
import com.umss.sigesa.domain.model.ChatRole;
import com.umss.sigesa.domain.model.ChatToolCall;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class OpenWebUiChatAdapter implements ChatCompletionPort {

    private static final Duration TIMEOUT = Duration.ofSeconds(120);

    private final AssistantProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    public OpenWebUiChatAdapter(AssistantProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(TIMEOUT)
                .build();
    }

    @Override
    public ChatCompletionResult complete(ChatCompletionRequest request) {
        if (!properties.isEnabled()) {
            throw new AssistantUnavailableException("El asistente está deshabilitado.");
        }
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new AssistantUnavailableException(
                    "El asistente no está configurado. Defina SIGESA_ASSISTANT_API_KEY.");
        }

        try {
            String requestBody = buildRequestBody(request);
            String endpoint = normalizeBaseUrl(properties.getBaseUrl()) + "/v1/chat/completions";

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 401 || response.statusCode() == 403) {
                throw new AssistantUnavailableException(
                        "API key de Open WebUI inválida. Genere una nueva en Open WebUI y actualice SIGESA_ASSISTANT_API_KEY.");
            }

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new AssistantCompletionException(
                        "Open WebUI respondió con HTTP " + response.statusCode() + ": " + truncate(response.body()));
            }

            return extractCompletionResult(response.body());
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

    private String buildRequestBody(ChatCompletionRequest request) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", properties.getModel());
        root.put("stream", false);

        ArrayNode messagesNode = root.putArray("messages");
        for (ChatMessage message : request.messages()) {
            appendMessageNode(messagesNode, message);
        }

        List<AssistantToolDefinition> tools = request.tools();
        if (tools != null && !tools.isEmpty()) {
            ArrayNode toolsNode = root.putArray("tools");
            for (AssistantToolDefinition tool : tools) {
                ObjectNode toolNode = toolsNode.addObject();
                toolNode.put("type", "function");
                ObjectNode functionNode = toolNode.putObject("function");
                functionNode.put("name", tool.id());
                functionNode.put("description", tool.description());
                functionNode.set("parameters", objectMapper.valueToTree(tool.parameterSchema()));
            }
        }

        return objectMapper.writeValueAsString(root);
    }

    private void appendMessageNode(ArrayNode messagesNode, ChatMessage message) {
        ObjectNode messageNode = messagesNode.addObject();
        messageNode.put("role", message.role().name().toLowerCase());

        if (message.role() == ChatRole.TOOL) {
            messageNode.put("tool_call_id", message.toolCallId());
            messageNode.put("content", message.content());
            return;
        }

        if (message.role() == ChatRole.ASSISTANT
                && message.toolCalls() != null
                && !message.toolCalls().isEmpty()) {
            if (message.content() != null) {
                messageNode.put("content", message.content());
            } else {
                messageNode.putNull("content");
            }
            ArrayNode toolCallsNode = messageNode.putArray("tool_calls");
            for (ChatToolCall toolCall : message.toolCalls()) {
                ObjectNode toolCallNode = toolCallsNode.addObject();
                toolCallNode.put("id", toolCall.id());
                toolCallNode.put("type", "function");
                ObjectNode functionNode = toolCallNode.putObject("function");
                functionNode.put("name", toolCall.name());
                functionNode.put("arguments", toolCall.argumentsJson());
            }
            return;
        }

        messageNode.put("content", message.content() != null ? message.content() : "");
    }

    private ChatCompletionResult extractCompletionResult(String responseBody) throws Exception {
        JsonNode message = objectMapper.readTree(responseBody).path("choices").path(0).path("message");
        JsonNode toolCallsNode = message.path("tool_calls");

        if (toolCallsNode.isArray() && !toolCallsNode.isEmpty()) {
            List<ToolCall> toolCalls = new ArrayList<>();
            for (JsonNode toolCallNode : toolCallsNode) {
                toolCalls.add(new ToolCall(
                        toolCallNode.path("id").asText(),
                        toolCallNode.path("function").path("name").asText(),
                        toolCallNode.path("function").path("arguments").asText()
                ));
            }
            return new ChatCompletionResult(null, toolCalls);
        }

        JsonNode content = message.path("content");
        if (content.isMissingNode() || content.isNull() || content.asText().isBlank()) {
            throw new AssistantCompletionException("Open WebUI no devolvió contenido en la respuesta.");
        }

        return new ChatCompletionResult(content.asText(), List.of());
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
