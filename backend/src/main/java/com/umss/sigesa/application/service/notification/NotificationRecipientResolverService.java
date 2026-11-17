package com.umss.sigesa.application.service.notification;

import com.umss.sigesa.application.port.out.NotificationRecipientResolverPort;
import com.umss.sigesa.application.port.out.ProcessResponsiblePort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.InvalidEmailDomainException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import com.umss.sigesa.domain.model.UserStatus;
import com.umss.sigesa.domain.model.notification.NotificationRecipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class NotificationRecipientResolverService implements NotificationRecipientResolverPort {

    private static final Logger log = LoggerFactory.getLogger(NotificationRecipientResolverService.class);

    private final UserRepositoryPort userRepository;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;
    private final ProcessResponsiblePort processResponsiblePort;

    public NotificationRecipientResolverService(
            UserRepositoryPort userRepository,
            UserProgramAssignmentRepositoryPort assignmentRepository,
            ProcessResponsiblePort processResponsiblePort) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.processResponsiblePort = processResponsiblePort;
    }

    @Override
    public List<NotificationRecipient> resolveForProgram(UUID programId, Role role) {
        if (programId == null || role == null) {
            return List.of();
        }

        if (role == Role.TD) {
            return resolveTdForProgram(programId);
        }
        if (role == Role.CC) {
            return resolveCcForProgram(programId, null);
        }

        return filterActiveValid(userRepository.findAllFiltered(role, UserStatus.ACTIVE).stream()
                .filter(user -> hasActiveAssignment(user.getId(), programId))
                .toList());
    }

    /**
     * CC resolution: program assignees + optional process responsible (RR-03).
     */
    public List<NotificationRecipient> resolveCcForProgram(UUID programId, UUID processId) {
        Map<UUID, NotificationRecipient> byUser = new LinkedHashMap<>();

        if (processId != null) {
            resolveProcessResponsible(processId).ifPresent(r -> byUser.put(r.userId(), r));
        }

        List<UserProgramAssignment> assignments = assignmentRepository.findActiveByProgramId(programId);
        for (UserProgramAssignment assignment : assignments) {
            toRecipientIfEligible(assignment.getUserId(), Role.CC)
                    .ifPresent(r -> byUser.putIfAbsent(r.userId(), r));
        }

        return List.copyOf(byUser.values());
    }

    @Override
    public List<NotificationRecipient> resolveActiveByRole(Role role) {
        if (role == null) {
            return List.of();
        }
        return filterActiveValid(userRepository.findAllFiltered(role, UserStatus.ACTIVE));
    }

    @Override
    public Optional<NotificationRecipient> resolveProcessResponsible(UUID processId) {
        if (processId == null) {
            return Optional.empty();
        }
        return processResponsiblePort.findActiveByProcessId(processId)
                .flatMap(assignment -> toRecipientIfEligible(assignment.getUserId(), Role.CC));
    }

    @Override
    public Optional<NotificationRecipient> resolveUser(UUID userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return userRepository.findById(userId)
                .flatMap(user -> toRecipient(user, user.getRole()));
    }

    private List<NotificationRecipient> resolveTdForProgram(UUID programId) {
        List<UserProgramAssignment> assignments = assignmentRepository.findActiveByProgramId(programId);
        List<NotificationRecipient> scoped = new ArrayList<>();
        for (UserProgramAssignment assignment : assignments) {
            toRecipientIfEligible(assignment.getUserId(), Role.TD).ifPresent(scoped::add);
        }
        if (!scoped.isEmpty()) {
            return List.copyOf(dedupe(scoped));
        }

        log.warn("NOTIFY_TD_SCOPE_FALLBACK programId={} — no TD assignment; using all ACTIVE TD", programId);
        return filterActiveValid(userRepository.findAllFiltered(Role.TD, UserStatus.ACTIVE));
    }

    private boolean hasActiveAssignment(UUID userId, UUID programId) {
        return assignmentRepository.findActiveByUserId(userId).stream()
                .anyMatch(a -> programId.equals(a.getProgramId()) && a.getRevokedAt() == null);
    }

    private Optional<NotificationRecipient> toRecipientIfEligible(UUID userId, Role expectedRole) {
        return userRepository.findById(userId)
                .flatMap(user -> toRecipient(user, expectedRole));
    }

    private List<NotificationRecipient> filterActiveValid(List<AppUser> users) {
        List<NotificationRecipient> result = new ArrayList<>();
        for (AppUser user : users) {
            toRecipient(user, user.getRole()).ifPresent(result::add);
        }
        return List.copyOf(dedupe(result));
    }

    private Optional<NotificationRecipient> toRecipient(AppUser user, Role expectedRole) {
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            return Optional.empty();
        }
        if (expectedRole != null && user.getRole() != expectedRole) {
            return Optional.empty();
        }
        String rawEmail = user.getEmail() != null ? user.getEmail().value() : null;
        try {
            Email email = Email.of(rawEmail);
            return Optional.of(new NotificationRecipient(user.getId(), email.value(), user.getRole()));
        } catch (InvalidEmailDomainException | IllegalArgumentException ex) {
            log.warn("NOTIFICATION_RECIPIENT_INVALID userId={} email={} reason={}",
                    user.getId(), rawEmail, ex.getMessage());
            return Optional.empty();
        }
    }

    private static List<NotificationRecipient> dedupe(List<NotificationRecipient> recipients) {
        Map<UUID, NotificationRecipient> byUser = new LinkedHashMap<>();
        for (NotificationRecipient recipient : recipients) {
            byUser.putIfAbsent(recipient.userId(), recipient);
        }
        return List.copyOf(byUser.values());
    }
}
