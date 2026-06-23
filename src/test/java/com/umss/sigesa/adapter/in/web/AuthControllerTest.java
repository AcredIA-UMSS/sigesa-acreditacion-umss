package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.advice.AuthExceptionHandler;
import com.umss.sigesa.application.port.in.AuthenticateUseCase;
import com.umss.sigesa.application.port.out.IssuedToken;
import com.umss.sigesa.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AuthExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticateUseCase authenticateUseCase;

    @Test
    void login_returnsJwtOnSuccess() throws Exception {
        UUID programId = UUID.randomUUID();
        when(authenticateUseCase.authenticate(anyString(), anyString()))
                .thenReturn(new AuthenticateUseCase.LoginResult(
                        new IssuedToken("jwt-token", 3600L),
                        Role.CC,
                        List.of(programId)
                ));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"cc@umss.edu.bo","password":"secret"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.role").value("CC"));
    }
}
