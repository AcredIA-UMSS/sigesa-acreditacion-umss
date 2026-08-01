package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.SendChatMessageUseCase;
import com.umss.sigesa.application.port.out.ChatCompletionPort;
import com.umss.sigesa.application.service.assistant.SendChatMessageService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AssistantProperties.class)
public class AssistantModuleConfig {

    @Bean
    SendChatMessageUseCase sendChatMessageUseCase(
            ChatCompletionPort chatCompletionPort,
            AssistantProperties assistantProperties) {
        return new SendChatMessageService(chatCompletionPort, assistantProperties.getSystemPrompt());
    }
}
