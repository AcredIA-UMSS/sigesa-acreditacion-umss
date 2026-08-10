package com.umss.sigesa.application.service.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.assistant.ToolExecutionResult;
import com.umss.sigesa.domain.model.ChatMessage;
import com.umss.sigesa.domain.model.ChatRole;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ejecuta consultas frecuentes directamente contra tools (sin depender del tool-calling del LLM).
 * Mitiga alucinaciones de modelos pequeños como llama3.2:3b.
 */
public class AssistantDirectQueryService {

    private static final Pattern ACTIVE_PROCESSES_PATTERN = Pattern.compile(
            "(?is).*(procesos?\\s+activos|carreras?\\s+.*proceso\\s+activo|"
                    + "qu[eé]\\s+carreras?\\s+tienen\\s+(un\\s+)?proceso|"
                    + "list(a|ar|ame)?\\s+(las\\s+)?carreras?\\s+.*proceso\\s+activo).*");

    private static final Pattern PHASES_PATTERN = Pattern.compile(
            "(?is).*(list(a|ar|ame|arme)?\\s+(todas\\s+)?(las\\s+)?fases|fases\\s+(del\\s+)?proceso).*");

    private static final Pattern USERS_PATTERN = Pattern.compile(
            "(?is).*(list(a|ar|ame)?\\s+(los\\s+)?usuarios|usuarios\\s+registrados|qui[eé]n(es)?\\s+est[aá]\\s+registrad).*");

    private static final Pattern DEACTIVATE_PATTERN = Pattern.compile(
            "(?is).*(desactiva(r)?|dar\\s+de\\s+baja)\\s+(al\\s+usuario\\s+)?(?<target>.+)");
    private static final Pattern ACTIVATE_PATTERN = Pattern.compile(
            "(?is).*(activa(r)?|reactiva(r)?|dar\\s+de\\s+alta)\\s+(al\\s+usuario\\s+)?(?<target>.+)");

    private static final Pattern CONFIRM_PATTERN = Pattern.compile(
            "(?is)^(confirmo|s[ií]\\s*,?\\s*procede|confirmar|procede)\\b.*");

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "([a-z0-9._%+-]+@umss\\.edu\\.bo)", Pattern.CASE_INSENSITIVE);

    private final AssistantToolExecutor toolExecutor;
    private final ObjectMapper objectMapper;

    public AssistantDirectQueryService(AssistantToolExecutor toolExecutor, ObjectMapper objectMapper) {
        this.toolExecutor = toolExecutor;
        this.objectMapper = objectMapper;
    }

    public Optional<String> tryHandle(String userMessage, List<ChatMessage> history, AssistantAuthContext auth) {
        if (userMessage == null || userMessage.isBlank() || auth == null) {
            return Optional.empty();
        }

        String message = userMessage.trim();
        String role = auth.role() != null ? auth.role().trim().toUpperCase(Locale.ROOT) : "";

        Optional<String> writeFlow = tryHandleWriteFlow(message, history, auth, role);
        if (writeFlow.isPresent()) {
            return writeFlow;
        }

        if ("JD".equals(role) || "TD".equals(role)) {
            if (ACTIVE_PROCESSES_PATTERN.matcher(message).matches()) {
                return Optional.of(handleListActiveProcesses(message, auth));
            }
            if (PHASES_PATTERN.matcher(message).matches()) {
                return Optional.of(handleListPhases(message, auth));
            }
        }

        if ("JD".equals(role) && USERS_PATTERN.matcher(message).matches()) {
            return Optional.of(handleListUsers(auth));
        }

        return Optional.empty();
    }

    private Optional<String> tryHandleWriteFlow(String message,
                                                List<ChatMessage> history,
                                                AssistantAuthContext auth,
                                                String role) {
        if (!"JD".equals(role)) {
            return Optional.empty();
        }

        if (CONFIRM_PATTERN.matcher(message).matches()) {
            PendingWriteAction pending = findPendingWriteAction(history);
            if (pending != null) {
                return Optional.of(executeSetUserStatus(pending.identifier(), pending.action(), true, auth));
            }
        }

        Matcher deactivate = DEACTIVATE_PATTERN.matcher(message);
        if (deactivate.matches()) {
            String target = cleanTarget(deactivate.group("target"));
            if (!target.isBlank()) {
                return Optional.of(executeSetUserStatus(target, "DEACTIVATE", false, auth));
            }
        }

        Matcher activate = ACTIVATE_PATTERN.matcher(message);
        if (activate.matches()) {
            String target = cleanTarget(activate.group("target"));
            if (!target.isBlank()) {
                return Optional.of(executeSetUserStatus(target, "ACTIVATE", false, auth));
            }
        }

        return Optional.empty();
    }

    private String handleListActiveProcesses(String message, AssistantAuthContext auth) {
        AssistantProcessQueryParser.ParsedProcessQuery parsed = AssistantProcessQueryParser.parse(message);
        String args = buildJsonArgs(parsed.careerQuery(), parsed.templateType());
        return formatTool(AssistantToolRegistry.LIST_ACTIVE_PROCESSES_ID, args, auth);
    }

    private String handleListPhases(String message, AssistantAuthContext auth) {
        String careerQuery = extractCareerFromPhasesQuestion(message);
        if (careerQuery == null || careerQuery.isBlank()) {
            return "Indique la carrera (por ejemplo: «lista las fases de Ingeniería de Sistemas CEUB»).";
        }
        AssistantProcessQueryParser.ParsedProcessQuery parsed = AssistantProcessQueryParser.parse(careerQuery);
        String args = buildJsonArgs(
                parsed.careerQuery() != null ? parsed.careerQuery() : careerQuery,
                parsed.templateType());
        return formatTool(AssistantToolRegistry.LIST_PROCESS_PHASES_ID, args, auth);
    }

    private String handleListUsers(AssistantAuthContext auth) {
        return formatTool(AssistantToolRegistry.LIST_USERS_ID, "{}", auth);
    }

    private String executeSetUserStatus(String identifier, String action, boolean confirmed, AssistantAuthContext auth) {
        String escapedIdentifier = identifier.replace("\\", "\\\\").replace("\"", "\\\"");
        String args = "{\"identifier\":\"" + escapedIdentifier + "\",\"action\":\"" + action
                + "\",\"confirmed\":" + confirmed + "}";
        return formatTool(AssistantToolRegistry.SET_USER_STATUS_ID, args, auth);
    }

    private String formatTool(String toolId, String args, AssistantAuthContext auth) {
        try {
            String json = toolExecutor.execute(toolId, args, auth);
            ToolExecutionResult result = AssistantResponseFormatter.parseToolJson(json, objectMapper);
            return AssistantResponseFormatter.format(result);
        } catch (Exception ex) {
            return "No pude completar la consulta: " + ex.getMessage();
        }
    }

    private static String buildJsonArgs(String careerQuery, String templateType) {
        StringBuilder sb = new StringBuilder("{");
        if (careerQuery != null && !careerQuery.isBlank()) {
            sb.append("\"careerQuery\":\"").append(escapeJson(careerQuery)).append("\"");
        }
        if (templateType != null && !templateType.isBlank()) {
            if (sb.length() > 1) {
                sb.append(',');
            }
            sb.append("\"templateType\":\"").append(escapeJson(templateType)).append("\"");
        }
        sb.append('}');
        return sb.toString();
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String extractCareerFromPhasesQuestion(String message) {
        Pattern afterDe = Pattern.compile("(?is)(?:fases\\s+(?:del\\s+proceso\\s+)?de\\s+|proceso\\s+de\\s+)(.+)");
        Matcher matcher = afterDe.matcher(message);
        if (matcher.find()) {
            return cleanTarget(matcher.group(1));
        }

        Pattern trailing = Pattern.compile("(?is)(?:ingenier[ií]a\\s+de\\s+sistemas|inf-?sis|[\\p{L}\\s-]{3,})(?:\\s+(?:arcu-?sur|arcusur|ceub))?");
        Matcher trailingMatcher = trailing.matcher(message);
        if (trailingMatcher.find()) {
            String candidate = trailingMatcher.group().trim();
            if (!candidate.toLowerCase(Locale.ROOT).contains("fases")
                    && !candidate.toLowerCase(Locale.ROOT).contains("list")) {
                return candidate;
            }
        }
        return null;
    }

    private static String cleanTarget(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replaceAll("[?.!]+$", "").trim();
    }

    private PendingWriteAction findPendingWriteAction(List<ChatMessage> history) {
        if (history == null || history.isEmpty()) {
            return null;
        }

        for (int i = history.size() - 1; i >= 0; i--) {
            ChatMessage message = history.get(i);
            if (message.role() != ChatRole.ASSISTANT || message.content() == null) {
                continue;
            }
            String content = message.content();
            if (!content.contains("Vista previa:") && !content.contains("Confirme")) {
                continue;
            }

            Matcher emailMatcher = EMAIL_PATTERN.matcher(content);
            if (!emailMatcher.find()) {
                continue;
            }
            String email = emailMatcher.group(1);
            String action = content.toLowerCase(Locale.ROOT).contains("activar") ? "ACTIVATE" : "DEACTIVATE";
            return new PendingWriteAction(email, action);
        }
        return null;
    }

    private record PendingWriteAction(String identifier, String action) {
    }
}
