package com.umss.sigesa.adapter.in.web.advice;

import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ErrorContractProbeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    void validationErrorIncludesFieldDetailsWithoutStack() throws Exception {
        mockMvc.perform(post("/api/v1/_error-probe/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("El nombre es obligatorio"))
                .andExpect(jsonPath("$.details.fields[0].field").value("name"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }

    @Test
    void malformedJsonDoesNotExposeParserInternals() throws Exception {
        mockMvc.perform(post("/api/v1/_error-probe/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MALFORMED_JSON"))
                .andExpect(jsonPath("$.message").value("El cuerpo de la solicitud no es JSON válido."))
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.not(containsString("JsonParse"))));
    }

    @Test
    void unhandledExceptionReturnsGeneric500() throws Exception {
        mockMvc.perform(get("/api/v1/_error-probe/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value(GlobalExceptionHandler.INTERNAL_CLIENT_MESSAGE))
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.not(containsString("password"))))
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.not(containsString("SELECT"))))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void typeMismatchReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/_error-probe/typed").param("id", "no-es-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.parameter").value("id"));
    }

    @Test
    void successStillReturnsPayload() throws Exception {
        mockMvc.perform(post("/api/v1/_error-probe/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"ok\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ok"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }
}
