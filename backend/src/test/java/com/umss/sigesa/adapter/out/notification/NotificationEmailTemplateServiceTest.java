package com.umss.sigesa.adapter.out.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationEmailTemplateServiceTest {

    @Test
    @DisplayName("HTML template contains CTA deep link and UMSS branding")
    void rendersCtaAndBranding() {
        NotificationEmailTemplateService service =
                new NotificationEmailTemplateService("https://sigesa.umss.edu.bo");

        String html = service.renderHtml(
                "Indicador observado",
                "Revise la observación.",
                "/procesos/abc/indicadores/def");

        assertThat(html).contains("Abrir en SIGESA");
        assertThat(html).contains("https://sigesa.umss.edu.bo/procesos/abc/indicadores/def");
        assertThat(html).contains("#003770");
        assertThat(html).contains("SIGESA");
        assertThat(html).contains("no-reply@umss.edu.bo");
        assertThat(html).doesNotContain("CNACU");
    }

    @Test
    @DisplayName("From address default used by JavaMail adapter config property")
    void fromDefault() {
        JavaMailNotificationSenderAdapter adapter = new JavaMailNotificationSenderAdapter(
                null,
                new NotificationEmailTemplateService("http://localhost:5173"),
                "no-reply@umss.edu.bo");
        assertThat(adapter.fromAddress()).isEqualTo("no-reply@umss.edu.bo");
    }
}
