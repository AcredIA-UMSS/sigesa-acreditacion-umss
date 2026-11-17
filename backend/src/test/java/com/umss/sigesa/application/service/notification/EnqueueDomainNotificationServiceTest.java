package com.umss.sigesa.application.service.notification;

import com.umss.sigesa.application.port.out.AccreditationProcessPort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.application.port.out.InAppNotificationPort;
import com.umss.sigesa.application.port.out.NotificationOutboxWritePort;
import com.umss.sigesa.application.port.out.NotificationRecipientResolverPort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ProcessStatus;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.notification.DomainNotificationCommand;
import com.umss.sigesa.domain.model.notification.InAppNotification;
import com.umss.sigesa.domain.model.notification.NotificationChannel;
import com.umss.sigesa.domain.model.notification.NotificationEventType;
import com.umss.sigesa.domain.model.notification.NotificationRecipient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnqueueDomainNotificationServiceTest {

    @Mock
    private NotificationRecipientResolverPort recipientResolver;
    @Mock
    private NotificationOutboxWritePort outboxWritePort;
    @Mock
    private InAppNotificationPort inAppNotificationPort;
    @Mock
    private AccreditationProcessPort processPort;
    @Mock
    private ProgramCatalogPort programCatalogPort;
    @Mock
    private TemplatePort templatePort;
    @Mock
    private NormativeHierarchyQueryPort normativeHierarchyQueryPort;

    private EnqueueDomainNotificationService service;

    @BeforeEach
    void setUp() {
        service = new EnqueueDomainNotificationService(
                recipientResolver, outboxWritePort, inAppNotificationPort, processPort);
    }

    @Test
    @DisplayName("Dual insert EMAIL + IN_APP with idempotency keys")
    void dualInsert() {
        UUID indicatorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        NotificationRecipient recipient = new NotificationRecipient(userId, "cc@umss.edu.bo", Role.CC);

        when(recipientResolver.resolveProcessResponsible(processId)).thenReturn(Optional.empty());
        when(recipientResolver.resolveForProgram(programId, Role.CC)).thenReturn(List.of(recipient));
        when(outboxWritePort.saveIfAbsent(any(), any(), eq(NotificationChannel.EMAIL), any(), any(), any(), any(), any(), any(), any(), eq("PENDING")))
                .thenReturn(Optional.of(UUID.randomUUID()));
        when(outboxWritePort.saveIfAbsent(any(), any(), eq(NotificationChannel.IN_APP), any(), any(), any(), any(), any(), any(), any(), eq("SENT")))
                .thenReturn(Optional.of(UUID.randomUUID()));

        service.enqueue(new DomainNotificationCommand(
                NotificationEventType.IndicatorApproved,
                indicatorId,
                programId,
                processId,
                Map.of("indicatorId", indicatorId.toString())));

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(outboxWritePort, times(2)).saveIfAbsent(
                keyCaptor.capture(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        assertThat(keyCaptor.getAllValues()).anyMatch(k -> k.endsWith(":EMAIL"));
        assertThat(keyCaptor.getAllValues()).anyMatch(k -> k.endsWith(":IN_APP"));
        verify(inAppNotificationPort).save(any(InAppNotification.class));
    }

    @Test
    @DisplayName("Idempotent IN_APP skip does not insert in-app twice")
    void idempotentSkip() {
        UUID indicatorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(recipientResolver.resolveProcessResponsible(processId)).thenReturn(Optional.empty());
        when(recipientResolver.resolveForProgram(programId, Role.CC))
                .thenReturn(List.of(new NotificationRecipient(userId, "cc@umss.edu.bo", Role.CC)));
        when(outboxWritePort.saveIfAbsent(any(), any(), eq(NotificationChannel.EMAIL), any(), any(), any(), any(), any(), any(), any(), eq("PENDING")))
                .thenReturn(Optional.empty());
        when(outboxWritePort.saveIfAbsent(any(), any(), eq(NotificationChannel.IN_APP), any(), any(), any(), any(), any(), any(), any(), eq("SENT")))
                .thenReturn(Optional.empty());

        service.enqueue(new DomainNotificationCommand(
                NotificationEventType.IndicatorRejected,
                indicatorId,
                programId,
                processId,
                Map.of("indicatorId", indicatorId.toString())));

        verify(inAppNotificationPort, never()).save(any());
    }

    @Test
    @DisplayName("ProcessClosed only when process status is CLOSED")
    void processClosedOnlyWhenClosed() {
        UUID processId = UUID.randomUUID();
        when(processPort.findById(processId)).thenReturn(Optional.of(AccreditationProcess.builder()
                .id(processId)
                .careerId(UUID.randomUUID())
                .status(ProcessStatus.ACTIVE.name())
                .build()));

        service.enqueue(new DomainNotificationCommand(
                NotificationEventType.ProcessClosed,
                processId,
                UUID.randomUUID(),
                processId,
                Map.of("processId", processId.toString())));

        verify(recipientResolver, never()).resolveActiveByRole(any());
        verify(outboxWritePort, never()).saveIfAbsent(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("ProcessClosed enqueues JD when already CLOSED")
    void processClosedWhenClosed() {
        UUID processId = UUID.randomUUID();
        UUID jdId = UUID.randomUUID();
        when(processPort.findById(processId)).thenReturn(Optional.of(AccreditationProcess.builder()
                .id(processId)
                .careerId(UUID.randomUUID())
                .status(ProcessStatus.CLOSED.name())
                .build()));
        when(recipientResolver.resolveActiveByRole(Role.JD))
                .thenReturn(List.of(new NotificationRecipient(jdId, "jd@umss.edu.bo", Role.JD)));
        when(outboxWritePort.saveIfAbsent(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(UUID.randomUUID()));

        service.enqueue(new DomainNotificationCommand(
                NotificationEventType.ProcessClosed,
                processId,
                UUID.randomUUID(),
                processId,
                Map.of("processId", processId.toString())));

        verify(inAppNotificationPort).save(any(InAppNotification.class));
        ArgumentCaptor<InAppNotification> captor = ArgumentCaptor.forClass(InAppNotification.class);
        verify(inAppNotificationPort).save(captor.capture());
        assertThat(captor.getValue().deepLink()).contains("/executive/semaphore");
    }

    @Test
    @DisplayName("ProcessAssigned notifies specific assignee by userId")
    void processAssignedNotifiesResponsible() {
        UUID processId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID ccId = UUID.randomUUID();
        when(recipientResolver.resolveUser(ccId))
                .thenReturn(Optional.of(new NotificationRecipient(ccId, "cc@umss.edu.bo", Role.CC)));
        when(outboxWritePort.saveIfAbsent(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(UUID.randomUUID()));

        service.enqueue(new DomainNotificationCommand(
                NotificationEventType.ProcessAssigned,
                processId,
                programId,
                processId,
                Map.of("processId", processId.toString(), "assigneeUserId", ccId.toString())));

        ArgumentCaptor<InAppNotification> captor = ArgumentCaptor.forClass(InAppNotification.class);
        verify(inAppNotificationPort).save(captor.capture());
        assertThat(captor.getValue().deepLink()).isEqualTo("/procesos/" + processId);
        assertThat(captor.getValue().userId()).isEqualTo(ccId);
        verify(recipientResolver, never()).resolveForProgram(any(), any());
        verify(recipientResolver, never()).resolveActiveByRole(any());
        verify(recipientResolver, never()).resolveProcessResponsible(any());
    }

    @Test
    @DisplayName("ProcessUnassigned notifies removed user with empty deepLink")
    void processUnassignedNotifiesRemovedWithoutDeepLink() {
        UUID processId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID removedId = UUID.randomUUID();
        when(recipientResolver.resolveUser(removedId))
                .thenReturn(Optional.of(new NotificationRecipient(removedId, "cc@umss.edu.bo", Role.CC)));
        when(outboxWritePort.saveIfAbsent(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(UUID.randomUUID()));

        service.enqueue(new DomainNotificationCommand(
                NotificationEventType.ProcessUnassigned,
                processId,
                programId,
                processId,
                Map.of("processId", processId.toString(), "removedUserId", removedId.toString())));

        ArgumentCaptor<InAppNotification> captor = ArgumentCaptor.forClass(InAppNotification.class);
        verify(inAppNotificationPort).save(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(removedId);
        assertThat(captor.getValue().deepLink() == null || captor.getValue().deepLink().isBlank()).isTrue();
        verify(recipientResolver).resolveUser(removedId);
        verify(recipientResolver, never()).resolveProcessResponsible(any());
    }


    @Test
    @DisplayName("ProcessAssigned email uses career and template business names")
    void processAssignedUsesBusinessNames() {
        UUID processId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        UUID ccId = UUID.randomUUID();

        when(recipientResolver.resolveUser(ccId))
                .thenReturn(Optional.of(new NotificationRecipient(ccId, "cc@umss.edu.bo", Role.CC)));
        when(processPort.findById(processId)).thenReturn(Optional.of(AccreditationProcess.builder()
                .id(processId)
                .careerId(programId)
                .templateId(templateId)
                .status(ProcessStatus.ACTIVE.name())
                .build()));
        when(programCatalogPort.findById(programId)).thenReturn(Optional.of(
                new ProgramCatalogPort.ProgramEntry(programId, "INF-SIS", "Ingeniería de Sistemas")));
        when(templatePort.findMetadataById(templateId)).thenReturn(Optional.of(Template.builder()
                .id(templateId)
                .name("CEUB 2026")
                .type("CEUB")
                .build()));
        when(outboxWritePort.saveIfAbsent(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(UUID.randomUUID()));

        service = new EnqueueDomainNotificationService(
                recipientResolver, outboxWritePort, inAppNotificationPort, processPort,
                programCatalogPort, templatePort, normativeHierarchyQueryPort);

        service.enqueue(new DomainNotificationCommand(
                NotificationEventType.ProcessAssigned,
                processId,
                programId,
                processId,
                Map.of(
                        "processId", processId.toString(),
                        "assigneeUserId", ccId.toString(),
                        "occurrenceId", UUID.randomUUID().toString())));

        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(outboxWritePort, times(2)).saveIfAbsent(
                any(), any(), any(), any(), any(), any(),
                subjectCaptor.capture(), bodyCaptor.capture(), any(), any(), any());

        assertThat(subjectCaptor.getAllValues())
                .anyMatch(s -> s.contains("CEUB 2026") && s.contains("Ingeniería de Sistemas"));
        assertThat(bodyCaptor.getAllValues())
                .anyMatch(b -> b.contains("CEUB 2026") && b.contains("Ingeniería de Sistemas")
                        && !b.contains(processId.toString()));
        ArgumentCaptor<InAppNotification> inAppCaptor = ArgumentCaptor.forClass(InAppNotification.class);
        verify(inAppNotificationPort).save(inAppCaptor.capture());
        assertThat(inAppCaptor.getValue().body()).contains("CEUB 2026");
        assertThat(inAppCaptor.getValue().body()).doesNotContain(processId.toString());
    }

    @Test
    @DisplayName("IndicatorApproved email uses indicator code/description instead of UUID")
    void indicatorApprovedUsesBusinessNames() {
        UUID indicatorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(recipientResolver.resolveProcessResponsible(processId)).thenReturn(Optional.empty());
        when(recipientResolver.resolveForProgram(programId, Role.CC))
                .thenReturn(List.of(new NotificationRecipient(userId, "cc@umss.edu.bo", Role.CC)));
        when(processPort.findById(processId)).thenReturn(Optional.of(AccreditationProcess.builder()
                .id(processId)
                .careerId(programId)
                .templateId(templateId)
                .status(ProcessStatus.ACTIVE.name())
                .build()));
        when(programCatalogPort.findById(programId)).thenReturn(Optional.of(
                new ProgramCatalogPort.ProgramEntry(programId, "INF-SIS", "Ingeniería de Sistemas")));
        when(templatePort.findMetadataById(templateId)).thenReturn(Optional.of(Template.builder()
                .id(templateId)
                .name("CEUB 2026")
                .type("CEUB")
                .build()));
        when(normativeHierarchyQueryPort.findIndicatorContext(indicatorId)).thenReturn(Optional.of(
                new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId,
                        processId,
                        programId,
                        UUID.randomUUID(),
                        "Dimensión 1",
                        "IND-1.1.1",
                        "Estatuto Organico de la Universidad",
                        null)));
        when(outboxWritePort.saveIfAbsent(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(UUID.randomUUID()));

        service = new EnqueueDomainNotificationService(
                recipientResolver, outboxWritePort, inAppNotificationPort, processPort,
                programCatalogPort, templatePort, normativeHierarchyQueryPort);

        service.enqueue(new DomainNotificationCommand(
                NotificationEventType.IndicatorApproved,
                indicatorId,
                programId,
                processId,
                Map.of("indicatorId", indicatorId.toString())));

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(outboxWritePort, times(2)).saveIfAbsent(
                any(), any(), any(), any(), any(), any(),
                any(), bodyCaptor.capture(), any(), any(), any());
        assertThat(bodyCaptor.getAllValues())
                .anyMatch(b -> b.contains("IND-1.1.1")
                        && b.contains("Ingeniería de Sistemas")
                        && !b.contains(indicatorId.toString()));
    }

}
