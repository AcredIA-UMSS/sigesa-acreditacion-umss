package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.DeactivateUserUseCase;
import com.umss.sigesa.application.port.in.RegisterUserUseCase;
import com.umss.sigesa.domain.model.AuthenticatedIdentity;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenAdapter jwtTokenAdapter;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;
    @MockitoBean
    private DeactivateUserUseCase deactivateUserUseCase;

    @Test
    void register_withoutAuthenticationReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"nuevo.cc@umss.edu.bo","role":"CC","programId":"550e8400-e29b-41d4-a716-446655440000"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void register_withCcRoleReturns403() throws Exception {
        mockMvc.perform(withBearerRole(Role.CC, post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"nuevo.cc@umss.edu.bo","role":"CC","programId":"550e8400-e29b-41d4-a716-446655440000"}
                                """)))
                .andExpect(status().isForbidden());
    }

    @Test
    void register_withJdRoleReturns201Inactive() throws Exception {
        UUID userId = UUID.randomUUID();
        when(registerUserUseCase.register(anyString(), anyString(), any(), any(char[].class)))
                .thenReturn(new RegisterUserUseCase.RegisterResult(userId, UserStatus.INACTIVE));

        mockMvc.perform(withBearerRole(Role.JD, post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"nuevo.cc@umss.edu.bo","role":"CC","programId":"550e8400-e29b-41d4-a716-446655440000"}
                                """)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void deactivate_withJdRoleReturns204() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(withBearerRole(Role.JD, patch("/api/v1/admin/users/{id}/deactivate", userId)))
                .andExpect(status().isNoContent());
    }

    @Test
    void register_duplicateEmailReturns409() throws Exception {
        when(registerUserUseCase.register(anyString(), anyString(), any(), any(char[].class)))
                .thenThrow(new com.umss.sigesa.domain.exception.DuplicateEmailException());

        mockMvc.perform(withBearerRole(Role.JD, post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"cc@umss.edu.bo","role":"CC","programId":"550e8400-e29b-41d4-a716-446655440000"}
                                """)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("EMAIL_ALREADY_REGISTERED"));
    }

    private MockHttpServletRequestBuilder withBearerRole(Role role, MockHttpServletRequestBuilder request) {
        String token = jwtTokenAdapter.issue(new AuthenticatedIdentity(
                UUID.randomUUID(),
                Email.of("test@umss.edu.bo"),
                role,
                List.of()
        )).accessToken();
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }
}
