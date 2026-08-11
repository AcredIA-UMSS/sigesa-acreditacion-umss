package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.application.port.in.ListUsersUseCase;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Email;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

final class AssistantUserLookup {

    private AssistantUserLookup() {
    }

    record ResolvedUser(ListUsersUseCase.UserSummary summary) {
    }

    record LookupResult(ResolvedUser user, String errorCode, String errorMessage) {
        static LookupResult ok(ListUsersUseCase.UserSummary summary) {
            return new LookupResult(new ResolvedUser(summary), null, null);
        }

        static LookupResult error(String code, String message) {
            return new LookupResult(null, code, message);
        }

        boolean isOk() {
            return user != null;
        }
    }

    static LookupResult resolve(String identifier,
                                ListUsersUseCase listUsersUseCase,
                                UserRepositoryPort userRepository) {
        if (identifier == null || identifier.isBlank()) {
            return LookupResult.error("INVALID_ARGUMENTS", "Debe indicar el correo o nombre del usuario.");
        }

        String trimmed = identifier.trim();
        Optional<ListUsersUseCase.UserSummary> byEmail = findByEmail(trimmed, listUsersUseCase, userRepository);
        if (byEmail.isPresent()) {
            return LookupResult.ok(byEmail.get());
        }

        Optional<ListUsersUseCase.UserSummary> byListedEmail = findByListedEmail(trimmed, listUsersUseCase);
        if (byListedEmail.isPresent()) {
            return LookupResult.ok(byListedEmail.get());
        }

        List<ListUsersUseCase.UserSummary> byName = findByName(trimmed, listUsersUseCase);
        if (byName.isEmpty()) {
            return LookupResult.error("USER_NOT_FOUND", "No se encontró un usuario con ese correo o nombre.");
        }
        if (byName.size() > 1) {
            return LookupResult.error(
                    "AMBIGUOUS_USER",
                    "Hay varios usuarios que coinciden. Indique el correo institucional completo.");
        }
        return LookupResult.ok(byName.getFirst());
    }

    private static Optional<ListUsersUseCase.UserSummary> findByEmail(String identifier,
                                                                        ListUsersUseCase listUsersUseCase,
                                                                        UserRepositoryPort userRepository) {
        if (!identifier.contains("@")) {
            return Optional.empty();
        }
        try {
            Email email = Email.of(identifier);
            Optional<AppUser> user = userRepository.findByEmail(email);
            if (user.isEmpty()) {
                return Optional.empty();
            }
            return listUsersUseCase.list(null, null).stream()
                    .filter(summary -> summary.userId().equals(user.get().getId()))
                    .findFirst();
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    private static Optional<ListUsersUseCase.UserSummary> findByListedEmail(String identifier,
                                                                              ListUsersUseCase listUsersUseCase) {
        String normalized = normalize(identifier);
        return listUsersUseCase.list(null, null).stream()
                .filter(user -> normalize(user.email()).equals(normalized))
                .findFirst();
    }

    private static List<ListUsersUseCase.UserSummary> findByName(String identifier, ListUsersUseCase listUsersUseCase) {
        String normalized = normalize(identifier);
        List<ListUsersUseCase.UserSummary> matches = new ArrayList<>();
        for (ListUsersUseCase.UserSummary user : listUsersUseCase.list(null, null)) {
            if (normalize(user.fullName()).contains(normalized)
                    || normalize(user.firstName()).contains(normalized)
                    || normalize(user.lastName()).contains(normalized)) {
                matches.add(user);
            }
        }
        return matches;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
