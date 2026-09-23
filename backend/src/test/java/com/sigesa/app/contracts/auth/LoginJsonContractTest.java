package com.sigesa.app.contracts.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.sigesa.app.contracts.JsonContracts;
import com.umss.sigesa.adapter.in.web.dto.LoginRequest;
import com.umss.sigesa.adapter.in.web.dto.LoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Contrato JSON POST /api/v1/auth/login")
class LoginJsonContractTest {

    @Test
    void loginRequest_exponeEmailYPassword() {
        JsonNode json = JsonContracts.tree(new LoginRequest("cc@umss.edu.bo", "CoordDemo2026!"));
        JsonContracts.assertObjectFields(json, "email", "password");
        assertThat(json.get("email").asText()).isEqualTo("cc@umss.edu.bo");
        assertThat(json.get("password").isTextual()).isTrue();
    }

    @Test
    void loginResponse_exponeTokenRolYAlcance() {
        UUID programId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        JsonNode json = JsonContracts.tree(new LoginResponse("jwt-access", 86400L, "CC", List.of(programId)));
        JsonContracts.assertObjectFields(json, "accessToken", "expiresIn", "role", "programScope");
        assertThat(json.get("accessToken").isTextual()).isTrue();
        assertThat(json.get("expiresIn").isNumber()).isTrue();
        assertThat(json.get("role").asText()).isEqualTo("CC");
        assertThat(json.get("programScope").isArray()).isTrue();
        assertThat(json.get("programScope").get(0).asText()).isEqualTo(programId.toString());
    }
}
