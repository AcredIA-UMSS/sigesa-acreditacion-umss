package com.umss.sigesa.adapter.out.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class NotificationEmailTemplateService {

    private final String publicBaseUrl;
    private final String templateHtml;

    public NotificationEmailTemplateService(
            @Value("${sigesa.public.base-url:http://localhost:5173}") String publicBaseUrl) {
        this.publicBaseUrl = trimTrailingSlash(publicBaseUrl);
        this.templateHtml = loadTemplate();
    }

    public String renderHtml(String subject, String body, String deepLink) {
        String ctaUrl = toAbsoluteUrl(deepLink);
        return templateHtml
                .replace("{{subject}}", escapeHtml(subject))
                .replace("{{body}}", escapeHtml(body).replace("\n", "<br/>"))
                .replace("{{ctaUrl}}", escapeHtml(ctaUrl));
    }

    public String toAbsoluteUrl(String deepLink) {
        if (deepLink == null || deepLink.isBlank()) {
            return publicBaseUrl;
        }
        if (deepLink.startsWith("http://") || deepLink.startsWith("https://")) {
            return deepLink;
        }
        return publicBaseUrl + (deepLink.startsWith("/") ? deepLink : "/" + deepLink);
    }

    private static String loadTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("templates/mail/notification.html");
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return """
                    <html><body style="font-family:Inter,Arial,sans-serif;color:#191C1D;">
                    <h1 style="color:#003770;">{{subject}}</h1>
                    <p>{{body}}</p>
                    <p><a href="{{ctaUrl}}">Abrir en SIGESA</a></p>
                    </body></html>
                    """;
        }
    }

    private static String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:5173";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private static String escapeHtml(String raw) {
        if (raw == null) {
            return "";
        }
        return raw
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
