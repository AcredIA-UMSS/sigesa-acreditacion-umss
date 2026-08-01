package com.umss.sigesa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.application.port.in.ListUsersUseCase;
import com.umss.sigesa.application.port.in.SendChatMessageUseCase;
import com.umss.sigesa.application.port.out.ChatCompletionPort;
import com.umss.sigesa.application.service.assistant.AssistantToolExecutor;
import com.umss.sigesa.application.service.assistant.AssistantToolRegistry;
import com.umss.sigesa.application.service.assistant.SendChatMessageService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AssistantProperties.class)
public class AssistantModuleConfig {

    @Bean
    AssistantToolRegistry assistantToolRegistry() {
        return new AssistantToolRegistry();
    }

    @Bean
    AssistantToolExecutor assistantToolExecutor(AssistantToolRegistry assistantToolRegistry,
                                                ListUsersUseCase listUsersUseCase) {
        return new AssistantToolExecutor(assistantToolRegistry, listUsersUseCase, new ObjectMapper());
    }

    @Bean
    SendChatMessageUseCase sendChatMessageUseCase(
            ChatCompletionPort chatCompletionPort,
            AssistantToolRegistry assistantToolRegistry,
            AssistantToolExecutor assistantToolExecutor,
            AssistantProperties assistantProperties) {
        return new SendChatMessageService(
                chatCompletionPort,
                assistantToolRegistry,
                assistantToolExecutor,
                assistantProperties.getSystemPrompt(),
                assistantProperties.getMaxToolIterations()
        );
    }
}
