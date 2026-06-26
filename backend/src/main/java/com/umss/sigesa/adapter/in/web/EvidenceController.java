package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.UploadEvidenceResponse;
import com.umss.sigesa.application.port.in.UploadEvidenceUseCase;
import com.umss.sigesa.domain.model.EvidenceUploadCommand;
import com.umss.sigesa.domain.model.EvidenceUploadResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/indicators/{indicatorId}/evidences")
public class EvidenceController {

    private final UploadEvidenceUseCase uploadEvidenceUseCase;

    public EvidenceController(UploadEvidenceUseCase uploadEvidenceUseCase) {
        this.uploadEvidenceUseCase = uploadEvidenceUseCase;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadEvidenceResponse> upload(
            @PathVariable UUID indicatorId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("criterionId") UUID criterionId,
            @RequestParam("description") String description,
            Authentication authentication) throws IOException {

        UUID uploadedBy = (UUID) authentication.getPrincipal();
        EvidenceUploadCommand command = new EvidenceUploadCommand(
                indicatorId,
                criterionId,
                description,
                file.getBytes(),
                file.getContentType(),
                file.getOriginalFilename(),
                uploadedBy
        );

        EvidenceUploadResult result = uploadEvidenceUseCase.upload(command);
        UploadEvidenceResponse response = new UploadEvidenceResponse(
                result.evidenceId(),
                result.version(),
                result.contentHash(),
                result.event(),
                result.currentState().name()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
