package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.adapter.in.web.dto.ChatMessageDto;
import com.umss.sigesa.domain.exception.AssistantInvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssistantChatInputValidatorTest {

    private AssistantChatInputValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AssistantChatInputValidator();
    }

    @Test
    void validateMessage_acceptsNaturalLanguage() {
        assertThatCode(() -> validator.validateMessage("Lista las fases de este proceso"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT * FROM users",
            "DROP TABLE app_user",
            "DELETE FROM phases",
            "INSERT INTO subphase VALUES (1)",
            "UPDATE app_user SET active=false",
            "' OR '1'='1",
            "<script>alert(1)</script>",
            "javascript:alert(1)",
            "onerror=alert(1)"
    })
    void validateMessage_rejectsBlockedPatterns(String payload) {
        assertThatThrownBy(() -> validator.validateMessage(payload))
                .isInstanceOf(AssistantInvalidInputException.class)
                .hasMessageContaining("seguridad");
    }

    @Test
    void validateMessage_rejectsOversizedInput() {
        String oversized = "a".repeat(AssistantChatInputValidator.MAX_MESSAGE_LENGTH + 1);
        assertThatThrownBy(() -> validator.validateMessage(oversized))
                .isInstanceOf(AssistantInvalidInputException.class)
                .hasMessageContaining("excede");
    }

    @Test
    void validateHistory_rejectsTooManyMessages() {
        List<ChatMessageDto> history = IntStream.range(0, AssistantChatInputValidator.MAX_HISTORY_SIZE + 1)
                .mapToObj(i -> new ChatMessageDto("user", "hola " + i))
                .toList();
        assertThatThrownBy(() -> validator.validateHistory(history))
                .isInstanceOf(AssistantInvalidInputException.class)
                .hasMessageContaining("historial");
    }

    @Test
    void validateHistory_rejectsInvalidRole() {
        assertThatThrownBy(() -> validator.validateHistory(
                List.of(new ChatMessageDto("admin", "mensaje"))))
                .isInstanceOf(AssistantInvalidInputException.class)
                .hasMessageContaining("Rol inválido");
    }
}
