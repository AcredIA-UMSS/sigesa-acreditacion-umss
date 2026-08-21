package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.application.model.assistant.AssistantChatResult;
import com.umss.sigesa.application.model.assistant.AssistantResolutionPath;
import com.umss.sigesa.application.model.assistant.AssistantToolStep;
import com.umss.sigesa.application.model.normative.NormativeDocumentHit;
import com.umss.sigesa.application.port.in.SearchNormativeDocumentsUseCase;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Recuperación normativa (RAG) vía FTS PostgreSQL o fallback LIKE.
 * Enriquece el prompt del LLM y responde directamente cuando hay fragmentos relevantes.
 */
public final class AssistantNormativeRagService {

    private static final Pattern NORMATIVE_QUERY_PATTERN = Pattern.compile(
            "(?is).*(normativa|ceub|arcu[-\\s]?sur|duea|acreditaci[oó]n\\s+universitaria|"
                    + "diagn[oó]stico\\s+institucional|matriz\\s+de\\s+evidencias|"
                    + "informe\\s+(preliminar|final)|cronograma|criterios?\\s+de\\s+evaluaci[oó]n|"
                    + "recolecci[oó]n\\s+documental|validaci[oó]n\\s+de\\s+criterios|"
                    + "qu[eé]\\s+dice\\s+la\\s+normativa|documentaci[oó]n\\s+normativa|"
                    + "buscar\\s+en\\s+normativa|referencia\\s+normativa|enlace\\s+normativo|"
                    + "subfase\\s+normativa|est[aá]ndares?\\s+de\\s+acreditaci[oó]n).*");

    private static final String TOOL_ID = AssistantToolRegistry.SEARCH_NORMATIVE_DOCS_ID;

    private final SearchNormativeDocumentsUseCase searchUseCase;
    private final boolean ragEnabled;
    private final int maxChunks;

    public AssistantNormativeRagService(SearchNormativeDocumentsUseCase searchUseCase,
                                        boolean ragEnabled,
                                        int maxChunks) {
        this.searchUseCase = searchUseCase;
        this.ragEnabled = ragEnabled;
        this.maxChunks = Math.clamp(maxChunks, 1, 5);
    }

    public boolean isEnabled() {
        return ragEnabled;
    }

    public boolean isNormativeQuery(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return false;
        }
        return NORMATIVE_QUERY_PATTERN.matcher(userMessage.trim()).matches();
    }

    public List<NormativeDocumentHit> retrieve(String userMessage, String templateType) {
        if (!ragEnabled || userMessage == null || userMessage.isBlank()) {
            return List.of();
        }
        return searchUseCase.search(userMessage.trim(), templateType, maxChunks);
    }

    public String buildPromptSuffix(String userMessage, String templateType) {
        List<NormativeDocumentHit> hits = retrieve(userMessage, templateType);
        if (hits.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("\n\nCONTEXTO NORMATIVO RECUPERADO (RAG):\n");
        int index = 1;
        for (NormativeDocumentHit hit : hits) {
            sb.append(index++).append(". ").append(hit.title());
            if (hit.templateType() != null && !hit.templateType().isBlank()) {
                sb.append(" [").append(hit.templateType()).append(']');
            }
            sb.append('\n');
            if (hit.snippet() != null && !hit.snippet().isBlank()) {
                sb.append("   ").append(hit.snippet()).append('\n');
            }
            if (hit.sourceUrl() != null && !hit.sourceUrl().isBlank()) {
                sb.append("   Fuente: ").append(hit.sourceUrl()).append('\n');
            }
        }
        sb.append("""
                Usa este contexto para elegir search_normative_docs cuando la pregunta sea normativa.
                No inventes requisitos fuera de estos fragmentos.
                """);
        return sb.toString();
    }

    public Optional<AssistantChatResult> tryDirectAnswer(String userMessage, String templateType) {
        if (!ragEnabled || !isNormativeQuery(userMessage)) {
            return Optional.empty();
        }
        List<NormativeDocumentHit> hits = retrieve(userMessage, templateType);
        if (hits.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(AssistantChatResult.fromSteps(
                formatHitsAsReply(userMessage, hits),
                AssistantResolutionPath.RAG,
                false,
                List.of(new AssistantToolStep(1, TOOL_ID, List.of("normative_document"), true))));
    }

    static String formatHitsAsReply(String query, List<NormativeDocumentHit> hits) {
        StringBuilder sb = new StringBuilder();
        sb.append("Encontré ").append(hits.size())
                .append(hits.size() == 1 ? " fragmento normativo" : " fragmentos normativos")
                .append(" relevante")
                .append(hits.size() == 1 ? "" : "s")
                .append(" para «")
                .append(query.trim())
                .append("»:\n\n");

        int index = 1;
        for (NormativeDocumentHit hit : hits) {
            sb.append(index++).append(". **").append(hit.title()).append("**");
            if (hit.templateType() != null && !hit.templateType().isBlank()) {
                sb.append(" (").append(hit.templateType()).append(')');
            }
            sb.append('\n');
            if (hit.phaseName() != null && hit.subphaseName() != null) {
                sb.append("   Fase: ").append(hit.phaseName())
                        .append(" → Subfase: ").append(hit.subphaseName()).append('\n');
            }
            if (hit.snippet() != null && !hit.snippet().isBlank()) {
                sb.append("   ").append(hit.snippet()).append('\n');
            }
            if (hit.sourceUrl() != null && !hit.sourceUrl().isBlank()) {
                sb.append("   Enlace normativo: ").append(hit.sourceUrl()).append('\n');
            }
            sb.append('\n');
        }
        sb.append("Esta respuesta proviene del índice normativo SIGESA (RAG).");
        return sb.toString().trim();
    }

    public static String extractQueryFromArgs(String argumentsJson) {
        if (argumentsJson == null || argumentsJson.isBlank()) {
            return "";
        }
        String lower = argumentsJson.toLowerCase(Locale.ROOT);
        int queryIndex = lower.indexOf("\"query\"");
        if (queryIndex < 0) {
            return argumentsJson.replaceAll("[{}\"]", " ").trim();
        }
        int colon = argumentsJson.indexOf(':', queryIndex);
        if (colon < 0) {
            return "";
        }
        int startQuote = argumentsJson.indexOf('"', colon + 1);
        if (startQuote < 0) {
            return "";
        }
        int endQuote = argumentsJson.indexOf('"', startQuote + 1);
        if (endQuote < 0) {
            return "";
        }
        return argumentsJson.substring(startQuote + 1, endQuote).trim();
    }
}
