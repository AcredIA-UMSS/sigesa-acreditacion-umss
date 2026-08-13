package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.assistant.AssistantChatContext;
import com.umss.sigesa.application.model.assistant.AssistantToolInvocation;
import com.umss.sigesa.domain.model.ChatMessage;
import com.umss.sigesa.domain.model.ChatRole;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Catálogo de palabras clave (escenario 1 y 4). Si la pregunta coincide, se ejecuta la tool sin LLM.
 */
public class AssistantKeywordRouter {

    private static final Pattern ACTIVE_PROCESSES_PATTERN = Pattern.compile(
            "(?is).*(procesos?\\s+activos|carreras?\\s+.*proceso\\s+activo|"
                    + "qu[eé]\\s+carreras?\\s+tienen\\s+(un\\s+)?proceso|"
                    + "list(a|ar|ame)?\\s+(las\\s+)?carreras?\\s+.*proceso\\s+activo).*");

    /** Palabra catálogo «fases» — sinónimos como «etapas» NO están aquí (escenario 2 → LLM). */
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

    /** Preguntas contextuales en copiloto de fases (sin nombrar carrera). */
    private static final Pattern CONTEXTUAL_PHASES_PATTERN = Pattern.compile(
            "(?is).*(list(a|ar|ame|arme)?\\s+(todas\\s+)?(las\\s+)?fases|"
                    + "fases\\s+(del\\s+)?proceso|cu[aá]ntas\\s+fases|"
                    + "etapas\\s+(del\\s+)?proceso|list(a|ar|ame)?\\s+(las\\s+)?etapas).*");

    private static final Pattern CONTEXTUAL_STRUCTURE_PATTERN = Pattern.compile(
            "(?is).*(estructura\\s+(completa|del\\s+proceso)|"
                    + "list(a|ar|ame)?\\s+(las\\s+)?subfases|subfases\\s+(del\\s+)?proceso|"
                    + "enlaces?\\s+(de\\s+)?(las\\s+)?subfases|árbol\\s+de\\s+fases).*");

    public Optional<AssistantToolInvocation> resolve(String userMessage,
                                                     List<ChatMessage> history,
                                                     AssistantAuthContext auth) {
        return resolve(userMessage, history, auth, AssistantChatContext.general());
    }

    public Optional<AssistantToolInvocation> resolve(String userMessage,
                                                     List<ChatMessage> history,
                                                     AssistantAuthContext auth,
                                                     AssistantChatContext chatContext) {
        if (userMessage == null || userMessage.isBlank() || auth == null) {
            return Optional.empty();
        }

        String message = userMessage.trim();
        String role = auth.role() != null ? auth.role().trim().toUpperCase(Locale.ROOT) : "";

        Optional<AssistantToolInvocation> writeFlow = resolveWriteFlow(message, history, role);
        if (writeFlow.isPresent()) {
            return writeFlow;
        }

        if (message.startsWith("/buscar ") || message.startsWith("/search ")
                || message.startsWith("/search-evidence ") || message.startsWith("/search-evidences ")
                || message.startsWith("/buscar-evidencia ") || message.startsWith("/buscar-evidencias ")) {
            String query = message.substring(message.indexOf(" ") + 1).trim();
            return Optional.of(new AssistantToolInvocation("buscar_evidencias", "{\"query\":\"" + query + "\"}"));
        }

        if ("JD".equals(role) || "TD".equals(role)) {
            if (!chatContext.isPhasesAgent() && ACTIVE_PROCESSES_PATTERN.matcher(message).matches()) {
                return Optional.of(buildActiveProcessesInvocation(message));
            }
            if (PHASES_PATTERN.matcher(message).matches()) {
                return Optional.of(buildPhasesInvocation(message, chatContext));
            }
        }

        if ("JD".equals(role) || "TD".equals(role) || "CC".equals(role)) {
            if (chatContext.isPhasesAgent()
                    && chatContext.careerName() != null
                    && !chatContext.careerName().isBlank()
                    && CONTEXTUAL_STRUCTURE_PATTERN.matcher(message).matches()) {
                return Optional.of(buildStructureInvocationFromContext(chatContext));
            }
            if (chatContext.isPhasesAgent()
                    && chatContext.careerName() != null
                    && !chatContext.careerName().isBlank()
                    && CONTEXTUAL_PHASES_PATTERN.matcher(message).matches()) {
                return Optional.of(buildPhasesInvocationFromContext(chatContext));
            }
        }

        if ("JD".equals(role) && USERS_PATTERN.matcher(message).matches()) {
            return Optional.of(new AssistantToolInvocation(AssistantToolRegistry.LIST_USERS_ID, "{}"));
        }

        return Optional.empty();
    }

    private Optional<AssistantToolInvocation> resolveWriteFlow(String message,
                                                                 List<ChatMessage> history,
                                                                 String role) {
        if (!"JD".equals(role)) {
            return Optional.empty();
        }

        if (CONFIRM_PATTERN.matcher(message).matches()) {
            PendingWriteAction pending = findPendingWriteAction(history);
            if (pending != null) {
                return Optional.of(buildSetUserStatusInvocation(pending.identifier(), pending.action(), true));
            }
        }

        Matcher deactivate = DEACTIVATE_PATTERN.matcher(message);
        if (deactivate.matches()) {
            String target = cleanTarget(deactivate.group("target"));
            if (!target.isBlank()) {
                return Optional.of(buildSetUserStatusInvocation(target, "DEACTIVATE", false));
            }
        }

        Matcher activate = ACTIVATE_PATTERN.matcher(message);
        if (activate.matches()) {
            String target = cleanTarget(activate.group("target"));
            if (!target.isBlank()) {
                return Optional.of(buildSetUserStatusInvocation(target, "ACTIVATE", false));
            }
        }

        return Optional.empty();
    }

    private AssistantToolInvocation buildActiveProcessesInvocation(String message) {
        AssistantProcessQueryParser.ParsedProcessQuery parsed = AssistantProcessQueryParser.parse(message);
        return new AssistantToolInvocation(
                AssistantToolRegistry.LIST_ACTIVE_PROCESSES_ID,
                buildJsonArgs(parsed.careerQuery(), parsed.templateType()));
    }

    private AssistantToolInvocation buildPhasesInvocation(String message, AssistantChatContext chatContext) {
        if (chatContext.isPhasesAgent()
                && chatContext.careerName() != null
                && !chatContext.careerName().isBlank()) {
            return buildPhasesInvocationFromContext(chatContext);
        }
        String careerQuery = extractCareerFromPhasesQuestion(message);
        if (careerQuery == null || careerQuery.isBlank()) {
            careerQuery = "Ingeniería de Sistemas";
        }
        AssistantProcessQueryParser.ParsedProcessQuery parsed = AssistantProcessQueryParser.parse(careerQuery);
        String args = buildJsonArgs(
                parsed.careerQuery() != null ? parsed.careerQuery() : careerQuery,
                parsed.templateType());
        return new AssistantToolInvocation(AssistantToolRegistry.LIST_PROCESS_PHASES_ID, args);
    }

    private AssistantToolInvocation buildPhasesInvocationFromContext(AssistantChatContext chatContext) {
        String templateType = chatContext.templateType();
        String careerQuery = chatContext.careerName();
        if (careerQuery == null || careerQuery.isBlank()) {
            careerQuery = chatContext.careerCode();
        }
        return new AssistantToolInvocation(
                AssistantToolRegistry.LIST_PROCESS_PHASES_ID,
                buildJsonArgs(careerQuery, templateType));
    }

    private AssistantToolInvocation buildStructureInvocationFromContext(AssistantChatContext chatContext) {
        String templateType = chatContext.templateType();
        String careerQuery = chatContext.careerName();
        if (careerQuery == null || careerQuery.isBlank()) {
            careerQuery = chatContext.careerCode();
        }
        return new AssistantToolInvocation(
                AssistantToolRegistry.LIST_PROCESS_STRUCTURE_ID,
                buildJsonArgs(careerQuery, templateType));
    }

    private static AssistantToolInvocation buildSetUserStatusInvocation(String identifier,
                                                                          String action,
                                                                          boolean confirmed) {
        String escapedIdentifier = identifier.replace("\\", "\\\\").replace("\"", "\\\"");
        String args = "{\"identifier\":\"" + escapedIdentifier + "\",\"action\":\"" + action
                + "\",\"confirmed\":" + confirmed + "}";
        return new AssistantToolInvocation(AssistantToolRegistry.SET_USER_STATUS_ID, args);
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
