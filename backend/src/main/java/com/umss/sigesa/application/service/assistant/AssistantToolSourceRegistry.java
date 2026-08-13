package com.umss.sigesa.application.service.assistant;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AssistantToolSourceRegistry {

    private static final Map<String, List<String>> SOURCES = Map.ofEntries(
            Map.entry(AssistantToolRegistry.LIST_USERS_ID, List.of("app_user")),
            Map.entry(AssistantToolRegistry.LIST_PROGRAMS_ID, List.of("programs")),
            Map.entry(AssistantToolRegistry.LIST_ACTIVE_PROCESSES_ID, List.of(
                    "accreditation_processes", "programs", "templates", "process_responsible_assignment", "app_user")),
            Map.entry(AssistantToolRegistry.LIST_PROCESS_PHASES_ID, List.of(
                    "phases", "subphases", "accreditation_processes", "programs")),
            Map.entry(AssistantToolRegistry.LIST_PROCESS_STRUCTURE_ID, List.of(
                    "phases", "subphases", "accreditation_processes", "programs")),
            Map.entry(AssistantToolRegistry.SET_USER_STATUS_ID, List.of("app_user")),
            Map.entry(AssistantToolRegistry.GET_USER_DETAIL_ID, List.of("app_user", "user_program_assignment")),
            Map.entry(AssistantToolRegistry.CREATE_USER_ID, List.of("app_user", "user_program_assignment")),
            Map.entry(AssistantToolRegistry.MANAGE_USER_STATUS_ID, List.of("app_user")),
            Map.entry(AssistantToolRegistry.MANAGE_USER_ASSIGNMENT_ID, List.of(
                    "user_program_assignment", "app_user", "programs")),
            Map.entry(AssistantToolRegistry.MANAGE_PROCESS_PHASE_ID, List.of(
                    "phases", "subphases", "accreditation_processes", "programs")),
            Map.entry(AssistantToolRegistry.MANAGE_PROCESS_SUBPHASE_ID, List.of(
                    "phases", "subphases", "accreditation_processes", "programs"))
    );

    private AssistantToolSourceRegistry() {
    }

    public static List<String> sourceTablesFor(String toolId) {
        if (toolId == null || toolId.isBlank()) {
            return List.of();
        }
        return SOURCES.getOrDefault(toolId, List.of());
    }

    public static Map<String, List<String>> allSources() {
        return new LinkedHashMap<>(SOURCES);
    }
}
