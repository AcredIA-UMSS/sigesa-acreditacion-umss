package com.umss.sigesa.adapter.out.notification;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JavaMailNotificationSenderAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    @DisplayName("Sends MIME message with from, to, subject and HTML body")
    void sendsMimeMessage() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        JavaMailNotificationSenderAdapter adapter = new JavaMailNotificationSenderAdapter(
                mailSender,
                new NotificationEmailTemplateService("https://sigesa.umss.edu.bo"),
                "no-reply@umss.edu.bo");

        UUID id = UUID.randomUUID();
        adapter.send(id, "td@umss.edu.bo", "Asunto", "Cuerpo largo", "/executive/semaphore");

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        MimeMessage sent = captor.getValue();
        assertThat(sent.getSubject()).isEqualTo("Asunto");
        assertThat(sent.getFrom()[0].toString()).contains("no-reply@umss.edu.bo");
        assertThat(sent.getAllRecipients()[0].toString()).contains("td@umss.edu.bo");
        assertThat(adapter.fromAddress()).isEqualTo("no-reply@umss.edu.bo");
    }

    @Test
    @DisplayName("SMTP failure is wrapped as IllegalStateException")
    void wrapsSmtpFailure() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("relay denied")).when(mailSender).send(any(MimeMessage.class));

        JavaMailNotificationSenderAdapter adapter = new JavaMailNotificationSenderAdapter(
                mailSender,
                new NotificationEmailTemplateService("http://localhost:5173"),
                "no-reply@umss.edu.bo");

        assertThatThrownBy(() -> adapter.send(
                UUID.randomUUID(), "cc@umss.edu.bo", "x", "y", "/x"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SMTP send failed");
    }
}
