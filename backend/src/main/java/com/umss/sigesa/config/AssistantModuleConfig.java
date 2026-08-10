package com.umss.sigesa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.application.port.in.ActivateUserUseCase;
import com.umss.sigesa.application.port.in.AddProcessPhaseUseCase;
import com.umss.sigesa.application.port.in.DeactivateUserUseCase;
import com.umss.sigesa.application.port.in.DeleteProcessPhaseUseCase;
import com.umss.sigesa.application.port.in.GetProcessDetailUseCase;
import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.application.port.in.ListProgramsUseCase;
import com.umss.sigesa.application.port.in.ListUsersUseCase;
import com.umss.sigesa.application.port.in.ReorderProcessStructureUseCase;
import com.umss.sigesa.application.port.in.SendChatMessageUseCase;
import com.umss.sigesa.application.port.in.UpdateProcessPhaseUseCase;
import com.umss.sigesa.application.port.out.ChatCompletionPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.application.service.assistant.AssistantDirectQueryService;
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
                                                ListUsersUseCase listUsersUseCase,
                                                ActivateUserUseCase activateUserUseCase,
                                                DeactivateUserUseCase deactivateUserUseCase,
                                                UserRepositoryPort userRepositoryPort,
                                                ListProgramsUseCase listProgramsUseCase,
                                                ListProcessesUseCase listProcessesUseCase,
                                                GetProcessDetailUseCase getProcessDetailUseCase,
                                                AddProcessPhaseUseCase addProcessPhaseUseCase,
                                                UpdateProcessPhaseUseCase updateProcessPhaseUseCase,
                                                DeleteProcessPhaseUseCase deleteProcessPhaseUseCase,
                                                ReorderProcessStructureUseCase reorderProcessStructureUseCase) {
        return new AssistantToolExecutor(
                assistantToolRegistry,
                listUsersUseCase,
                activateUserUseCase,
                deactivateUserUseCase,
                userRepositoryPort,
                listProgramsUseCase,
                listProcessesUseCase,
                getProcessDetailUseCase,
                addProcessPhaseUseCase,
                updateProcessPhaseUseCase,
                deleteProcessPhaseUseCase,
                reorderProcessStructureUseCase,
                new ObjectMapper()
        );
    }

    @Bean
    AssistantDirectQueryService assistantDirectQueryService(AssistantToolExecutor assistantToolExecutor) {
        return new AssistantDirectQueryService(assistantToolExecutor, new ObjectMapper());
    }

    @Bean
    SendChatMessageUseCase sendChatMessageUseCase(
            ChatCompletionPort chatCompletionPort,
            AssistantToolRegistry assistantToolRegistry,
            AssistantToolExecutor assistantToolExecutor,
            AssistantDirectQueryService assistantDirectQueryService,
            AssistantProperties assistantProperties) {
        return new SendChatMessageService(
                chatCompletionPort,
                assistantToolRegistry,
                assistantToolExecutor,
                assistantDirectQueryService,
                assistantProperties.getSystemPrompt(),
                assistantProperties.getMaxToolIterations()
        );
    }
}
