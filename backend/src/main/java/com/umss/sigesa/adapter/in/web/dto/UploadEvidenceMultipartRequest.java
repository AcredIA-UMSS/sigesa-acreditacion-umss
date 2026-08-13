package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Schema(name = "UploadEvidenceMultipartRequest", description = "Parts multipart de carga de evidencia")
public class UploadEvidenceMultipartRequest {

    @Schema(type = "string", format = "binary", requiredMode = Schema.RequiredMode.REQUIRED)
    private MultipartFile file;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID criterionId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public UUID getCriterionId() {
        return criterionId;
    }

    public void setCriterionId(UUID criterionId) {
        this.criterionId = criterionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
