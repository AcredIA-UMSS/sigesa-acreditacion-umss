package com.umss.sigesa.adapter.in.security;

import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.CreateProcessUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateProcessUseCase createProcessUseCase;
    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    void sensitiveActionWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/processes")
                        .contentType("application/json")
                        .content("""
                                {"template_id":"550e8400-e29b-41d4-a716-446655440000","career_id":"550e8400-e29b-41d4-a716-446655440001"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }
}
