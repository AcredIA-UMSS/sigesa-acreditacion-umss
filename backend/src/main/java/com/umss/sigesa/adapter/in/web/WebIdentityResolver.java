package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.ForbiddenProgramScopeException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.AuthenticatedIdentity;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Component
public class WebIdentityResolver {

    private final UserRepositoryPort userRepositoryPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepositoryPort;

    public WebIdentityResolver(UserRepositoryPort userRepositoryPort,
                               UserProgramAssignmentRepositoryPort assignmentRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.assignmentRepositoryPort = assignmentRepositoryPort;
    }

    public UUID requireUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        try {
            return UUID.fromString(auth.getPrincipal().toString());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    public String currentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return "";
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElse("");
    }

    public AuthenticatedIdentity requireIdentity() {
        UUID userId = requireUserId();
        AppUser user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ForbiddenProgramScopeException("Usuario no encontrado en el sistema."));
        List<UUID> programScope = assignmentRepositoryPort.findActiveByUserId(userId).stream()
                .map(UserProgramAssignment::getProgramId)
                .toList();
        return new AuthenticatedIdentity(user.getId(), user.getEmail(), user.getRole(), programScope);
    }

    public List<UUID> programScopeForCurrentUser() {
        String role = currentRole();
        if ("JD".equals(role) || "TD".equals(role)) {
            return List.of();
        }
        return assignmentRepositoryPort.findActiveByUserId(requireUserId()).stream()
                .map(UserProgramAssignment::getProgramId)
                .toList();
    }
}
