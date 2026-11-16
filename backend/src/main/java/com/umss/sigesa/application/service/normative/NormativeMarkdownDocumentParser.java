package com.umss.sigesa.application.service.normative;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsea chunks Markdown con front matter YAML mínimo (title, template_type, phase_name, subphase_name).
 */
public final class NormativeMarkdownDocumentParser {

    private static final Pattern FRONT_MATTER = Pattern.compile(
            "\\A---\\r?\\n(.*?)\\r?\\n---\\r?\\n?(.*)\\z",
            Pattern.DOTALL);
    private static final Pattern YAML_LINE = Pattern.compile(
            "^([A-Za-z0-9_]+):\\s*(?:\"([^\"]*)\"|'([^']*)'|(.*))$");

    public record Document(
            String title,
            String templateType,
            String phaseName,
            String subphaseName,
            String bodyText
    ) {
    }

    public Optional<Document> parse(String rawMarkdown) {
        if (rawMarkdown == null || rawMarkdown.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = FRONT_MATTER.matcher(rawMarkdown.strip());
        if (!matcher.matches()) {
            return Optional.empty();
        }
        Map<String, String> fields = parseYaml(matcher.group(1));
        String title = blankToNull(fields.get("title"));
        String body = matcher.group(2) == null ? "" : matcher.group(2).trim();
        if (title == null || body.isBlank()) {
            return Optional.empty();
        }
        String templateType = optional(fields.get("template_type"), "ARCU-SUR");
        return Optional.of(new Document(
                title,
                templateType,
                blankToNull(fields.get("phase_name")),
                blankToNull(fields.get("subphase_name")),
                body));
    }

    private static Map<String, String> parseYaml(String block) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (String line : block.split("\\R")) {
            Matcher yaml = YAML_LINE.matcher(line.trim());
            if (!yaml.matches()) {
                continue;
            }
            String value = yaml.group(2) != null ? yaml.group(2)
                    : yaml.group(3) != null ? yaml.group(3)
                    : yaml.group(4);
            fields.put(yaml.group(1), value == null ? "" : value.trim());
        }
        return fields;
    }

    private static String optional(String value, String fallback) {
        String normalized = blankToNull(value);
        return normalized == null ? fallback : normalized;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
