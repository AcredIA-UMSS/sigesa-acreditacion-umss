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

    private static final Pattern PENDING_EVIDENCES_PATTERN = Pattern.compile(
            "(?is).*(evidencias?\\s+pendientes|pendientes\\s+de\\s+revisi[oó]n|"
                    + "documentaci[oó]n\\s+subida|list(a|ar|ame)?\\s+(las\\s+)?evidencias?\\s+pendientes).*");

    private static final Pattern EVIDENCE_DETAIL_PATTERN = Pattern.compile(
            "(?is).*(detalle\\s+(de\\s+)?(la\\s+)?evidencia|evidencia\\s+detalle|"
                    + "metadatos\\s+(de\\s+)?(la\\s+)?evidencia).*");

    private static final Pattern EVIDENCE_COMPLETENESS_PATTERN = Pattern.compile(
            "(?is).*(completeness|completitud|(est[aá]\\s+)?completa\\s+(la\\s+)?evidencia|"
                    + "evidencia\\s+(est[aá]\\s+)?completa|checklist\\s+(de\\s+)?evidencia).*");

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})");

    private static final Pattern NORMATIVE_PATTERN = Pattern.compile(
            "(?is).*(normativa|qu[eé]\\s+dice\\s+la\\s+normativa|documentaci[oó]n\\s+normativa|"
                    + "buscar\\s+en\\s+normativa|referencia\\s+normativa|enlace\\s+normativo|"
                    + "requisitos?\\s+normativos?|est[aá]ndares?\\s+de\\s+acreditaci[oó]n|"
                    + "diagn[oó]stico\\s+institucional|matriz\\s+de\\s+evidencias|"
                    + "informe\\s+(preliminar|final)|validaci[oó]n\\s+de\\s+criterios).*");

    private static final Pattern OPERATIONAL_QUERY_PATTERN = Pattern.compile(
            "(?is).*(list(a|ar|ame|arme)?|fases?|etapas?|usuarios?|procesos?\\s+activos?|"
                    + "evidencias?\\s+pendientes|activa(r)?|desactiva(r)?|crea(r)?|elimina(r)?).*");

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

        Optional<AssistantToolInvocation> writeFlow = resolveWriteFlow(message, history, role, chatContext);
        if (writeFlow.isPresent()) {
            return writeFlow;
        }

        Optional<AssistantToolInvocation> evidenceFlow = resolveEvidenceFlow(message, role, chatContext);
        if (evidenceFlow.isPresent()) {
            return evidenceFlow;
        }

        if ("JD".equals(role) || "TD".equals(role)) {
            if (!chatContext.isPhasesAgent() && !chatContext.isUsersAgent() && !chatContext.isEvidenceAgent()
                    && ACTIVE_PROCESSES_PATTERN.matcher(message).matches()) {
                return Optional.of(buildActiveProcessesInvocation(message));
            }
            if (!chatContext.isUsersAgent() && !chatContext.isEvidenceAgent()
                    && PHASES_PATTERN.matcher(message).matches()) {
                return Optional.of(buildPhasesInvocation(message, chatContext));
            }
        }

        if (("JD".equals(role) || "TD".equals(role) || "CC".equals(role))
                && !chatContext.isUsersAgent()
                && !chatContext.isEvidenceAgent()) {
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

        if ("JD".equals(role) && !chatContext.isEvidenceAgent()
                && USERS_PATTERN.matcher(message).matches()) {
            return Optional.of(new AssistantToolInvocation(AssistantToolRegistry.LIST_USERS_ID, "{}"));
        }

        if (isAssistantRole(role)
                && NORMATIVE_PATTERN.matcher(message).matches()
                && !OPERATIONAL_QUERY_PATTERN.matcher(message).matches()) {
            return Optional.of(buildNormativeSearchInvocation(message, chatContext));
        }

        return Optional.empty();
    }

    private static boolean isAssistantRole(String role) {
        return "JD".equals(role) || "TD".equals(role) || "CC".equals(role) || "EE".equals(role);
    }

    private static AssistantToolInvocation buildNormativeSearchInvocation(String message,
                                                                            AssistantChatContext chatContext) {
        String escaped = message.replace("\\", "\\\\").replace("\"", "\\\"");
        StringBuilder json = new StringBuilder("{\"query\":\"");
        json.append(escaped).append('"');
        if (chatContext != null && chatContext.templateType() != null && !chatContext.templateType().isBlank()) {
            json.append(",\"templateType\":\"").append(chatContext.templateType()).append('"');
        }
        json.append('}');
        return new AssistantToolInvocation(AssistantToolRegistry.SEARCH_NORMATIVE_DOCS_ID, json.toString());
    }

    private Optional<AssistantToolInvocation> resolveEvidenceFlow(String message,
                                                                    String role,
                                                                    AssistantChatContext chatContext) {
        boolean allowedRole = "JD".equals(role) || "TD".equals(role) || "CC".equals(role);
        if (!allowedRole) {
            return Optional.empty();
        }

        boolean pendingMatch = PENDING_EVIDENCES_PATTERN.matcher(message).matches();
        boolean detailMatch = EVIDENCE_DETAIL_PATTERN.matcher(message).matches();
        boolean completenessMatch = EVIDENCE_COMPLETENESS_PATTERN.matcher(message).matches();
        boolean preferEvidence = chatContext.isEvidenceAgent()
                || pendingMatch
                || detailMatch
                || completenessMatch;
        if (!preferEvidence) {
            return Optional.empty();
        }

        if (completenessMatch) {
            String indicatorId = extractUuid(message);
            if (indicatorId != null) {
                return Optional.of(new AssistantToolInvocation(
                        AssistantToolRegistry.CHECK_EVIDENCE_COMPLETENESS_ID,
                        "{\"indicatorId\":\"" + indicatorId + "\"}"));
            }
        }

        if (detailMatch) {
            String indicatorId = extractUuid(message);
            if (indicatorId != null) {
                return Optional.of(new AssistantToolInvocation(
                        AssistantToolRegistry.GET_EVIDENCE_DETAIL_ID,
                        "{\"indicatorId\":\"" + indicatorId + "\"}"));
            }
        }

        if (pendingMatch) {
            return Optional.of(buildListPendingEvidencesInvocation(chatContext));
        }

        return Optional.empty();
    }

    private static AssistantToolInvocation buildListPendingEvidencesInvocation(AssistantChatContext chatContext) {
        if (chatContext != null && chatContext.programId() != null) {
            return new AssistantToolInvocation(
                    AssistantToolRegistry.LIST_PENDING_EVIDENCES_ID,
                    "{\"programId\":\"" + chatContext.programId() + "\"}");
        }
        return new AssistantToolInvocation(AssistantToolRegistry.LIST_PENDING_EVIDENCES_ID, "{}");
    }

    private static String extractUuid(String message) {
        if (message == null) {
            return null;
        }
        Matcher matcher = UUID_PATTERN.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Optional<AssistantToolInvocation> resolveWriteFlow(String message,
                                                                 List<ChatMessage> history,
                                                                 String role,
                                                                 AssistantChatContext chatContext) {
        if (!"JD".equals(role)) {
            return Optional.empty();
        }

        boolean usersAgent = chatContext != null && chatContext.isUsersAgent();
        String statusToolId = usersAgent
                ? AssistantToolRegistry.MANAGE_USER_STATUS_ID
                : AssistantToolRegistry.SET_USER_STATUS_ID;

        if (CONFIRM_PATTERN.matcher(message).matches()) {
            PendingWriteAction pending = findPendingWriteAction(history);
            if (pending != null) {
                return Optional.of(buildUserStatusInvocation(
                        statusToolId, pending.identifier(), pending.action(), true));
            }
        }

        Matcher deactivate = DEACTIVATE_PATTERN.matcher(message);
        if (deactivate.matches()) {
            String target = cleanTarget(deactivate.group("target"));
            if (!target.isBlank()) {
                return Optional.of(buildUserStatusInvocation(statusToolId, target, "DEACTIVATE", false));
            }
        }

        Matcher activate = ACTIVATE_PATTERN.matcher(message);
        if (activate.matches()) {
            String target = cleanTarget(activate.group("target"));
            if (!target.isBlank()) {
                return Optional.of(buildUserStatusInvocation(statusToolId, target, "ACTIVATE", false));
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

    private static AssistantToolInvocation buildUserStatusInvocation(String toolId,
                                                                       String identifier,
                                                                       String action,
                                                                       boolean confirmed) {
        String escapedIdentifier = identifier.replace("\\", "\\\\").replace("\"", "\\\"");
        String args = "{\"identifier\":\"" + escapedIdentifier + "\",\"action\":\"" + action
                + "\",\"confirmed\":" + confirmed + "}";
        return new AssistantToolInvocation(toolId, args);
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
