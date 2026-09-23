package com.sigesa.app.contracts.evidence;

import com.fasterxml.jackson.databind.JsonNode;
import com.sigesa.app.contracts.JsonContracts;
import com.umss.sigesa.adapter.in.web.dto.UploadEvidenceResponse;
import com.umss.sigesa.adapter.in.web.dto.UploadableIndicatorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Contrato JSON evidencias UC-004")
class EvidenceJsonContractTest {

    @Test
    void uploadableIndicator_exponeEstadoYCriterio() {
        UUID indicatorId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID criterionId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        JsonNode json = JsonContracts.tree(new UploadableIndicatorResponse(
                indicatorId,
                "IND-01",
                "Indicador demo",
                criterionId,
                "CR-01",
                "Criterio demo",
                "PENDIENTE"));
        JsonContracts.assertObjectFields(
                json,
                "indicatorId",
                "code",
                "title",
                "criterionId",
                "criterionCode",
                "criterionTitle",
                "currentState");
        assertThat(json.get("indicatorId").asText()).isEqualTo(indicatorId.toString());
        assertThat(json.get("currentState").asText()).isEqualTo("PENDIENTE");
        assertThat(json.get("criterionId").asText()).isEqualTo(criterionId.toString());
    }

    @Test
    void uploadEvidenceResponse_exponeIdVersionHashEventoYEstado() {
        UUID evidenceId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        JsonNode json = JsonContracts.tree(new UploadEvidenceResponse(
                evidenceId,
                1,
                "abc123hash",
                "EvidenceUploaded",
                "SUBIDO"));
        JsonContracts.assertObjectFields(json, "evidenceId", "version", "contentHash", "event", "currentState");
        assertThat(json.get("evidenceId").asText()).isEqualTo(evidenceId.toString());
        assertThat(json.get("version").asInt()).isEqualTo(1);
        assertThat(json.get("contentHash").isTextual()).isTrue();
        assertThat(json.get("event").asText()).isEqualTo("EvidenceUploaded");
        assertThat(json.get("currentState").asText()).isEqualTo("SUBIDO");
    }
}
