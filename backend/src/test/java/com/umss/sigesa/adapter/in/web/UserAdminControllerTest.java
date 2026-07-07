package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.AuthExceptionHandler;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.DeactivateUserUseCase;
import com.umss.sigesa.application.port.in.ListUsersUseCase;
import com.umss.sigesa.application.port.in.RegisterUserUseCase;
import com.umss.sigesa.domain.model.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserAdminController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, RestAuthenticationEntryPoint.class, AuthExceptionHandler.class})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;
    @MockitoBean
    private DeactivateUserUseCase deactivateUserUseCase;
    @MockitoBean
    private ListUsersUseCase listUsersUseCase;
    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

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
    @WithMockUser(roles = "CC")
    void register_withCcRoleReturns403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users")
                        .with(user("testcc").roles("CC"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"nuevo.cc@umss.edu.bo","role":"CC","programId":"550e8400-e29b-41d4-a716-446655440000"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "JD")
    void register_withJdRoleReturns201Inactive() throws Exception {
        UUID userId = UUID.randomUUID();
        when(registerUserUseCase.register(anyString(), anyString(), any(), any(char[].class)))
                .thenReturn(new RegisterUserUseCase.RegisterResult(userId, UserStatus.INACTIVE));

        mockMvc.perform(post("/api/v1/admin/users")
                        .with(user("testjd").roles("JD"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"nuevo.cc@umss.edu.bo","role":"CC","programId":"550e8400-e29b-41d4-a716-446655440000"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @WithMockUser(roles = "JD")
    void deactivate_withJdRoleReturns204() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/users/{id}/deactivate", userId)
                        .with(user("testjd").roles("JD")))
                .andExpect(status().isNoContent());
    }

    @Test
    void list_withoutAuthenticationReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @WithMockUser(roles = "CC")
    void list_withCcRoleReturns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .with(user("testcc").roles("CC")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "JD")
    void list_withJdRoleReturns200() throws Exception {
        UUID userId = UUID.randomUUID();
        when(listUsersUseCase.list(null, null)).thenReturn(List.of(
                new ListUsersUseCase.UserSummary(userId, "cc@umss.edu.bo", "CC", "ACTIVE", List.of())
        ));

        mockMvc.perform(get("/api/v1/admin/users")
                        .with(user("testjd").roles("JD")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].email").value("cc@umss.edu.bo"))
                .andExpect(jsonPath("$[0].role").value("CC"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "JD")
    void register_duplicateEmailReturns409() throws Exception {
        when(registerUserUseCase.register(anyString(), anyString(), any(), any(char[].class)))
                .thenThrow(new com.umss.sigesa.domain.exception.DuplicateEmailException());

        mockMvc.perform(post("/api/v1/admin/users")
                        .with(user("testjd").roles("JD"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"cc@umss.edu.bo","role":"CC","programId":"550e8400-e29b-41d4-a716-446655440000"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("EMAIL_ALREADY_REGISTERED"));
    }
}
