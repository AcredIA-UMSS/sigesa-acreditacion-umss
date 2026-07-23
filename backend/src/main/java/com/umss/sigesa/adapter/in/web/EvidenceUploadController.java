package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.EvidenceResponse;
import com.umss.sigesa.application.port.in.UploadEvidenceUseCase;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.ForbiddenProgramScopeException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.AuthenticatedIdentity;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/indicators")
public class EvidenceUploadController {

    private final UploadEvidenceUseCase uploadEvidenceUseCase;
    private final UserRepositoryPort userRepositoryPort;
    private final UserProgramAssignmentRepositoryPort userProgramAssignmentRepositoryPort;

    public EvidenceUploadController(UploadEvidenceUseCase uploadEvidenceUseCase,
                                    UserRepositoryPort userRepositoryPort,
                                    UserProgramAssignmentRepositoryPort userProgramAssignmentRepositoryPort) {
        this.uploadEvidenceUseCase = uploadEvidenceUseCase;
        this.userRepositoryPort = userRepositoryPort;
        this.userProgramAssignmentRepositoryPort = userProgramAssignmentRepositoryPort;
    }

    @PostMapping(value = "/{indicatorId}/evidences", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvidenceResponse> uploadEvidence(
            @PathVariable UUID indicatorId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("criterionId") UUID criterionId,
            @RequestParam(value = "description", required = false) String description) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId;
        try {
            userId = UUID.fromString(auth.getPrincipal().toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AppUser user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ForbiddenProgramScopeException("Usuario no encontrado en el sistema."));

        List<UserProgramAssignment> assignments = userProgramAssignmentRepositoryPort.findActiveByUserId(userId);
        List<UUID> programScope = assignments.stream()
                .map(UserProgramAssignment::getProgramId)
                .toList();

        AuthenticatedIdentity identity = new AuthenticatedIdentity(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                programScope
        );

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el archivo cargado.", e);
        }

        EvidenceResponse response = uploadEvidenceUseCase.upload(
                indicatorId,
                criterionId,
                description,
                file.getOriginalFilename(),
                fileBytes,
                file.getContentType(),
                identity
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
