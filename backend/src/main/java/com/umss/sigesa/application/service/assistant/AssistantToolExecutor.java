package com.umss.sigesa.application.service.assistant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.assistant.AssistantToolDefinition;
import com.umss.sigesa.application.model.assistant.ToolExecutionResult;
import com.umss.sigesa.application.model.process.EnrichedProcessDetail;
import com.umss.sigesa.application.model.evidence.EvidenceControlItem;
import com.umss.sigesa.application.port.in.ActivateUserUseCase;
import com.umss.sigesa.application.port.in.AddProcessPhaseUseCase;
import com.umss.sigesa.application.port.in.AddProcessSubphaseUseCase;
import com.umss.sigesa.application.port.in.CheckEvidenceCompletenessUseCase;
import com.umss.sigesa.application.port.in.DeactivateUserUseCase;
import com.umss.sigesa.application.port.in.DeleteProcessPhaseUseCase;
import com.umss.sigesa.application.port.in.DeleteProcessSubphaseUseCase;
import com.umss.sigesa.application.port.in.GetEvidenceDetailUseCase;
import com.umss.sigesa.application.port.in.GetProcessDetailUseCase;
import com.umss.sigesa.application.port.in.ListPendingEvidencesUseCase;
import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.application.port.in.ListProgramsUseCase;
import com.umss.sigesa.application.port.in.ListUsersUseCase;
import com.umss.sigesa.application.port.in.ManageUserProgramAssignmentUseCase;
import com.umss.sigesa.application.port.in.RegisterUserUseCase;
import com.umss.sigesa.application.port.in.ReorderProcessStructureUseCase;
import com.umss.sigesa.application.port.in.UpdateProcessPhaseUseCase;
import com.umss.sigesa.application.port.in.UpdateProcessSubphaseUseCase;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.DuplicateEmailException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.InvalidEmailDomainException;
import com.umss.sigesa.domain.exception.InvalidFilterException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.InvalidScopeException;
import com.umss.sigesa.domain.exception.InvalidUserProfileException;
import com.umss.sigesa.domain.exception.InvalidUserStatusTransitionException;
import com.umss.sigesa.domain.exception.ProcessNotEditableException;
import com.umss.sigesa.domain.exception.ProcessStructureOrderConflictException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.exception.SubphaseHasEvidenceException;
import com.umss.sigesa.domain.exception.UserNotFoundException;
import com.umss.sigesa.domain.exception.WeakPasswordException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Phase;
import com.umss.sigesa.domain.model.Subphase;
import com.umss.sigesa.domain.model.UserStatus;

import com.umss.sigesa.application.port.in.SearchEvidenceUseCase;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AssistantToolExecutor {

    private final AssistantToolRegistry toolRegistry;
    private final ListUsersUseCase listUsersUseCase;
    private final ActivateUserUseCase activateUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final ManageUserProgramAssignmentUseCase manageUserProgramAssignmentUseCase;
    private final UserRepositoryPort userRepositoryPort;
    private final ListProgramsUseCase listProgramsUseCase;
    private final ListProcessesUseCase listProcessesUseCase;
    private final GetProcessDetailUseCase getProcessDetailUseCase;
    private final AddProcessPhaseUseCase addProcessPhaseUseCase;
    private final UpdateProcessPhaseUseCase updateProcessPhaseUseCase;
    private final DeleteProcessPhaseUseCase deleteProcessPhaseUseCase;
    private final AddProcessSubphaseUseCase addProcessSubphaseUseCase;
    private final UpdateProcessSubphaseUseCase updateProcessSubphaseUseCase;
    private final DeleteProcessSubphaseUseCase deleteProcessSubphaseUseCase;
    private final ReorderProcessStructureUseCase reorderProcessStructureUseCase;
    private final SearchEvidenceUseCase searchEvidenceUseCase;
    private final ListPendingEvidencesUseCase listPendingEvidencesUseCase;
    private final GetEvidenceDetailUseCase getEvidenceDetailUseCase;
    private final CheckEvidenceCompletenessUseCase checkEvidenceCompletenessUseCase;
    private final com.umss.sigesa.application.port.in.ApproveIndicatorUseCase approveIndicatorUseCase;
    private final com.umss.sigesa.application.port.in.RejectIndicatorUseCase rejectIndicatorUseCase;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    public AssistantToolExecutor(AssistantToolRegistry toolRegistry,
                                 ListUsersUseCase listUsersUseCase,
                                 ActivateUserUseCase activateUserUseCase,
                                 DeactivateUserUseCase deactivateUserUseCase,
                                 RegisterUserUseCase registerUserUseCase,
                                 ManageUserProgramAssignmentUseCase manageUserProgramAssignmentUseCase,
                                 UserRepositoryPort userRepositoryPort,
                                 ListProgramsUseCase listProgramsUseCase,
                                 ListProcessesUseCase listProcessesUseCase,
                                 GetProcessDetailUseCase getProcessDetailUseCase,
                                 AddProcessPhaseUseCase addProcessPhaseUseCase,
                                 UpdateProcessPhaseUseCase updateProcessPhaseUseCase,
                                 DeleteProcessPhaseUseCase deleteProcessPhaseUseCase,
                                 AddProcessSubphaseUseCase addProcessSubphaseUseCase,
                                 UpdateProcessSubphaseUseCase updateProcessSubphaseUseCase,
                                 DeleteProcessSubphaseUseCase deleteProcessSubphaseUseCase,
                                 ReorderProcessStructureUseCase reorderProcessStructureUseCase,
                                 SearchEvidenceUseCase searchEvidenceUseCase,
                                 ListPendingEvidencesUseCase listPendingEvidencesUseCase,
                                 GetEvidenceDetailUseCase getEvidenceDetailUseCase,
                                 CheckEvidenceCompletenessUseCase checkEvidenceCompletenessUseCase,
                                 com.umss.sigesa.application.port.in.ApproveIndicatorUseCase approveIndicatorUseCase,
                                 com.umss.sigesa.application.port.in.RejectIndicatorUseCase rejectIndicatorUseCase,
                                 ObjectMapper objectMapper) {
        this.toolRegistry = toolRegistry;
        this.listUsersUseCase = listUsersUseCase;
        this.activateUserUseCase = activateUserUseCase;
        this.deactivateUserUseCase = deactivateUserUseCase;
        this.registerUserUseCase = registerUserUseCase;
        this.manageUserProgramAssignmentUseCase = manageUserProgramAssignmentUseCase;
        this.userRepositoryPort = userRepositoryPort;
        this.listProgramsUseCase = listProgramsUseCase;
        this.listProcessesUseCase = listProcessesUseCase;
        this.getProcessDetailUseCase = getProcessDetailUseCase;
        this.addProcessPhaseUseCase = addProcessPhaseUseCase;
        this.updateProcessPhaseUseCase = updateProcessPhaseUseCase;
        this.deleteProcessPhaseUseCase = deleteProcessPhaseUseCase;
        this.addProcessSubphaseUseCase = addProcessSubphaseUseCase;
        this.updateProcessSubphaseUseCase = updateProcessSubphaseUseCase;
        this.deleteProcessSubphaseUseCase = deleteProcessSubphaseUseCase;
        this.reorderProcessStructureUseCase = reorderProcessStructureUseCase;
        this.searchEvidenceUseCase = searchEvidenceUseCase;
        this.listPendingEvidencesUseCase = listPendingEvidencesUseCase;
        this.getEvidenceDetailUseCase = getEvidenceDetailUseCase;
        this.checkEvidenceCompletenessUseCase = checkEvidenceCompletenessUseCase;
        this.approveIndicatorUseCase = approveIndicatorUseCase;
        this.rejectIndicatorUseCase = rejectIndicatorUseCase;
        this.objectMapper = objectMapper;
    }

    public String execute(String toolId, String argumentsJson, AssistantAuthContext auth) {
        AssistantToolDefinition definition = toolRegistry.findById(toolId).orElse(null);
        if (definition == null) {
            return serialize(ToolExecutionResult.failure("TOOL_NOT_FOUND", "Tool desconocida: " + toolId));
        }

        if (auth == null || auth.role() == null || !definition.allowedRoles().contains(auth.role().trim().toUpperCase())) {
            return serialize(ToolExecutionResult.failure(
                    "ACCESS_DENIED",
                    "No tiene permisos para ejecutar la tool '" + toolId + "'."));
        }

        ToolExecutionResult result = switch (toolId) {
            case AssistantToolRegistry.LIST_USERS_ID -> executeListUsers(argumentsJson);
            case AssistantToolRegistry.GET_USER_DETAIL_ID -> executeGetUserDetail(argumentsJson);
            case AssistantToolRegistry.CREATE_USER_ID -> executeCreateUser(argumentsJson);
            case AssistantToolRegistry.LIST_PROGRAMS_ID -> executeListPrograms(argumentsJson);
            case AssistantToolRegistry.LIST_ACTIVE_PROCESSES_ID -> executeListActiveProcesses(argumentsJson, auth);
            case AssistantToolRegistry.LIST_PROCESS_PHASES_ID -> executeListProcessPhases(argumentsJson, auth);
            case AssistantToolRegistry.LIST_PROCESS_STRUCTURE_ID -> executeListProcessStructure(argumentsJson, auth);
            case AssistantToolRegistry.SET_USER_STATUS_ID -> executeSetUserStatus(argumentsJson, auth);
            case AssistantToolRegistry.MANAGE_USER_STATUS_ID -> executeManageUserStatus(argumentsJson, auth);
            case AssistantToolRegistry.MANAGE_USER_ASSIGNMENT_ID -> executeManageUserAssignment(argumentsJson);
            case AssistantToolRegistry.MANAGE_PROCESS_PHASE_ID -> executeManageProcessPhase(argumentsJson, auth);
            case AssistantToolRegistry.MANAGE_PROCESS_SUBPHASE_ID -> executeManageProcessSubphase(argumentsJson, auth);
            case AssistantToolRegistry.BUSCAR_EVIDENCIAS_ID -> executeBuscarEvidencias(argumentsJson, auth);
            case AssistantToolRegistry.LIST_PENDING_EVIDENCES_ID -> executeListPendingEvidences(argumentsJson, auth);
            case AssistantToolRegistry.GET_EVIDENCE_DETAIL_ID -> executeGetEvidenceDetail(argumentsJson, auth);
            case AssistantToolRegistry.CHECK_EVIDENCE_COMPLETENESS_ID ->
                    executeCheckEvidenceCompleteness(argumentsJson, auth);
            case AssistantToolRegistry.APPROVE_INDICATOR_ID -> executeApproveIndicator(argumentsJson, auth);
            case AssistantToolRegistry.REJECT_INDICATOR_ID -> executeRejectIndicator(argumentsJson, auth);
            default -> ToolExecutionResult.failure("TOOL_NOT_FOUND", "Tool desconocida: " + toolId);
        };

        return serialize(result);
    }

    private ToolExecutionResult executeBuscarEvidencias(String argumentsJson, AssistantAuthContext auth) {
        try {
            String query = "";
            if (argumentsJson != null && !argumentsJson.isBlank()) {
                JsonNode args = objectMapper.readTree(argumentsJson);
                if (args.hasNonNull("query")) {
                    query = args.get("query").asText();
                }
            }

            var searchResult = searchEvidenceUseCase.search(
                    query,
                    true,
                    auth.userId(),
                    auth.role(),
                    auth.programScope()
            );
            List<com.umss.sigesa.adapter.in.web.dto.EvidenceSearchDetailDto> allResults = searchResult.subsets().stream()
                    .flatMap(s -> s.results().stream())
                    .toList();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("evidences", allResults);
            data.put("total", allResults.size());
            return ToolExecutionResult.success(data);
        } catch (Exception ex) {
            return ToolExecutionResult.failure("SEARCH_FAILED", "Falla al ejecutar búsqueda: " + ex.getMessage());
        }
    }

    private ToolExecutionResult executeListUsers(String argumentsJson) {
        try {
            String roleFilter = null;
            String statusFilter = null;
            UUID programFilter = null;

            if (argumentsJson != null && !argumentsJson.isBlank()) {
                JsonNode args = objectMapper.readTree(argumentsJson);
                if (args.hasNonNull("role")) {
                    roleFilter = args.get("role").asText();
                }
                if (args.hasNonNull("status")) {
                    statusFilter = args.get("status").asText();
                }
                if (args.hasNonNull("programId") && !args.get("programId").asText().isBlank()) {
                    programFilter = UUID.fromString(args.get("programId").asText().trim());
                }
            }

            List<ListUsersUseCase.UserSummary> users = listUsersUseCase.list(roleFilter, statusFilter);
            if (programFilter != null) {
                UUID programId = programFilter;
                users = users.stream()
                        .filter(user -> user.programIds() != null && user.programIds().contains(programId))
                        .toList();
            }
            List<Map<String, Object>> userPayload = users.stream().map(this::toUserMap).toList();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("users", userPayload);
            data.put("total", userPayload.size());
            return ToolExecutionResult.success(data);
        } catch (IllegalArgumentException ex) {
            return ToolExecutionResult.failure("INVALID_FILTER", "programId inválido.");
        } catch (InvalidRoleException ex) {
            return ToolExecutionResult.failure("INVALID_ROLE", ex.getMessage());
        } catch (InvalidFilterException ex) {
            return ToolExecutionResult.failure("INVALID_FILTER", ex.getMessage());
        } catch (JsonProcessingException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", "No se pudieron interpretar los argumentos de la tool.");
        }
    }

    private ToolExecutionResult executeGetUserDetail(String argumentsJson) {
        try {
            JsonNode args = parseArgs(argumentsJson);
            String identifier = requiredText(args, "identifier");
            AssistantUserLookup.LookupResult lookup = AssistantUserLookup.resolve(
                    identifier, listUsersUseCase, userRepositoryPort);
            if (!lookup.isOk()) {
                return ToolExecutionResult.failure(lookup.errorCode(), lookup.errorMessage());
            }

            ListUsersUseCase.UserSummary summary = lookup.user().summary();
            AppUser domainUser = userRepositoryPort.findById(summary.userId()).orElse(null);

            Map<String, Object> data = toUserMap(summary);
            if (domainUser != null) {
                data.put("createdAt", domainUser.getCreatedAt() != null ? domainUser.getCreatedAt().toString() : null);
                data.put("updatedAt", domainUser.getUpdatedAt() != null ? domainUser.getUpdatedAt().toString() : null);
                data.put("lastAccess", null);
                data.put("lastAccessNote", "No se registra lastAccess en v1; updatedAt refleja el último cambio de estado.");
            }

            List<Map<String, Object>> programs = new ArrayList<>();
            if (summary.programIds() != null) {
                for (UUID programId : summary.programIds()) {
                    listProgramsUseCase.list(null).stream()
                            .filter(p -> p.id().equals(programId))
                            .findFirst()
                            .ifPresent(p -> {
                                Map<String, Object> program = new LinkedHashMap<>();
                                program.put("programId", p.id().toString());
                                program.put("code", p.code());
                                program.put("name", p.name());
                                programs.add(program);
                            });
                }
            }
            data.put("programs", programs);
            return ToolExecutionResult.success(data);
        } catch (IllegalArgumentException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", ex.getMessage());
        } catch (JsonProcessingException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", "No se pudieron interpretar los argumentos de la tool.");
        }
    }

    private ToolExecutionResult executeCreateUser(String argumentsJson) {
        try {
            JsonNode args = parseArgs(argumentsJson);
            String email = requiredText(args, "email");
            String firstName = requiredText(args, "firstName");
            String lastName = requiredText(args, "lastName");
            String phoneNumber = requiredText(args, "phoneNumber");
            String role = requiredText(args, "role").toUpperCase(Locale.ROOT);
            boolean confirmed = AssistantConfirmationSupport.isConfirmed(args);

            UUID programId = resolveProgramId(args);
            boolean requiresProgram = "CC".equals(role) || "EE".equals(role);
            if (requiresProgram && programId == null) {
                return ToolExecutionResult.failure(
                        "INVALID_SCOPE",
                        "El rol " + role + " requiere programId o programQuery.");
            }
            if (!requiresProgram) {
                programId = null;
            }

            String tempPassword = generateTemporaryPassword();
            Map<String, Object> preview = new LinkedHashMap<>();
            preview.put("email", email.trim().toLowerCase(Locale.ROOT));
            preview.put("firstName", firstName);
            preview.put("lastName", lastName);
            preview.put("phoneNumber", phoneNumber);
            preview.put("role", role);
            preview.put("programId", programId != null ? programId.toString() : null);
            preview.put("initialStatus", UserStatus.INACTIVE.name());

            AssistantUserActionPlan plan = AssistantUserActionPlan.of(
                    "CREATE_USER",
                    "Alta de " + firstName + " " + lastName + " (" + email + ") como " + role
                            + " — cuenta INACTIVE hasta primer acceso.",
                    preview);

            if (!confirmed) {
                return AssistantConfirmationSupport.confirmationRequired(
                        "CREATE",
                        plan.preview(),
                        plan.summary() + " Responda «confirmo» para crear la cuenta.");
            }

            RegisterUserUseCase.RegisterResult registered = registerUserUseCase.register(
                    new RegisterUserUseCase.RegisterUserCommand(
                            email,
                            role,
                            programId,
                            firstName,
                            lastName,
                            phoneNumber,
                            tempPassword.toCharArray()
                    ));

            Map<String, Object> result = new LinkedHashMap<>(plan.preview());
            result.put("userId", registered.userId().toString());
            result.put("status", registered.status().name());
            result.put("temporaryPassword", tempPassword);
            result.put("deliveryNote", "Entregue la contraseña temporal al usuario por canal offline.");

            return AssistantConfirmationSupport.executed(
                    "CREATE",
                    result,
                    "Usuario creado en estado INACTIVE. Contraseña temporal: " + tempPassword);
        } catch (InvalidEmailDomainException ex) {
            return ToolExecutionResult.failure("INVALID_EMAIL_DOMAIN", ex.getMessage());
        } catch (DuplicateEmailException ex) {
            return ToolExecutionResult.failure("EMAIL_ALREADY_REGISTERED", DuplicateEmailException.MESSAGE);
        } catch (InvalidScopeException | InvalidRoleException | InvalidUserProfileException | WeakPasswordException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", ex.getMessage());
        } catch (JsonProcessingException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", "No se pudieron interpretar los argumentos de la tool.");
        } catch (RuntimeException ex) {
            return ToolExecutionResult.failure("ASSISTANT_TOOL_FAILED", ex.getMessage());
        }
    }

    private ToolExecutionResult executeManageUserStatus(String argumentsJson, AssistantAuthContext auth) {
        try {
            JsonNode args = parseArgs(argumentsJson);
            String identifier = requiredText(args, "identifier");
            String action = requiredText(args, "action").toUpperCase(Locale.ROOT);
            boolean confirmed = AssistantConfirmationSupport.isConfirmed(args);

            if ("REACTIVATE".equals(action)) {
                action = "ACTIVATE";
            }
            if (!"ACTIVATE".equals(action) && !"DEACTIVATE".equals(action)) {
                return ToolExecutionResult.failure(
                        "INVALID_ACTION",
                        "La acción debe ser ACTIVATE, DEACTIVATE o REACTIVATE.");
            }

            // Reuse set_user_status semantics with UserActionPlan summary.
            String argsForStatus = "{\"identifier\":\"" + escapeJson(identifier)
                    + "\",\"action\":\"" + action + "\",\"confirmed\":" + confirmed + "}";
            ToolExecutionResult raw = executeSetUserStatus(argsForStatus, auth);
            if (!raw.ok() || !(raw.data() instanceof Map<?, ?> dataMap)) {
                return raw;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) dataMap;
            if (Boolean.TRUE.equals(data.get("confirmationRequired"))
                    && data.get("preview") instanceof Map<?, ?> previewRaw) {
                @SuppressWarnings("unchecked")
                Map<String, Object> preview = new LinkedHashMap<>((Map<String, Object>) previewRaw);
                String verb = "DEACTIVATE".equals(action) ? "desactivar" : "activar/reactivar";
                AssistantUserActionPlan plan = AssistantUserActionPlan.of(
                        action,
                        "Se va a " + verb + " a " + preview.get("fullName")
                                + " (" + preview.get("email") + "). Estado actual: "
                                + preview.get("currentStatus") + ".",
                        preview);
                return AssistantConfirmationSupport.confirmationRequired(
                        action,
                        plan.preview(),
                        plan.summary() + " Responda «confirmo» para proceder.");
            }
            return raw;
        } catch (IllegalArgumentException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", ex.getMessage());
        } catch (JsonProcessingException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", "No se pudieron interpretar los argumentos de la tool.");
        }
    }

    private ToolExecutionResult executeManageUserAssignment(String argumentsJson) {
        try {
            JsonNode args = parseArgs(argumentsJson);
            String identifier = requiredText(args, "identifier");
            String action = requiredText(args, "action").toUpperCase(Locale.ROOT);
            boolean confirmed = AssistantConfirmationSupport.isConfirmed(args);

            if (!"CREATE".equals(action) && !"UPDATE".equals(action)) {
                return ToolExecutionResult.failure("INVALID_ACTION", "La acción debe ser CREATE o UPDATE.");
            }

            UUID programId = resolveProgramId(args);
            if (programId == null) {
                return ToolExecutionResult.failure(
                        "INVALID_ARGUMENTS",
                        "Debe indicar programId o programQuery.");
            }

            AssistantUserLookup.LookupResult lookup = AssistantUserLookup.resolve(
                    identifier, listUsersUseCase, userRepositoryPort);
            if (!lookup.isOk()) {
                return ToolExecutionResult.failure(lookup.errorCode(), lookup.errorMessage());
            }

            ListUsersUseCase.UserSummary user = lookup.user().summary();
            String programLabel = listProgramsUseCase.list(null).stream()
                    .filter(p -> p.id().equals(programId))
                    .findFirst()
                    .map(p -> p.code() + " — " + p.name())
                    .orElse(programId.toString());

            Map<String, Object> preview = new LinkedHashMap<>();
            preview.put("userId", user.userId().toString());
            preview.put("email", user.email());
            preview.put("fullName", user.fullName());
            preview.put("role", user.role());
            preview.put("programId", programId.toString());
            preview.put("programLabel", programLabel);
            preview.put("currentProgramIds", user.programIds().stream().map(Object::toString).toList());

            AssistantUserActionPlan plan = AssistantUserActionPlan.of(
                    action + "_ASSIGNMENT",
                    action + " asignación de " + user.fullName() + " → " + programLabel
                            + " (mínimo privilegio: una carrera activa).",
                    preview);

            if (!confirmed) {
                return AssistantConfirmationSupport.confirmationRequired(
                        action,
                        plan.preview(),
                        plan.summary() + " Responda «confirmo» para aplicar.");
            }

            ManageUserProgramAssignmentUseCase.AssignmentResult assigned =
                    manageUserProgramAssignmentUseCase.assign(
                            new ManageUserProgramAssignmentUseCase.AssignCommand(
                                    user.userId(), programId, action));

            Map<String, Object> result = new LinkedHashMap<>(plan.preview());
            result.put("revokedCount", assigned.revokedCount());
            return AssistantConfirmationSupport.executed(
                    action,
                    result,
                    "Asignación actualizada. Carreras previas revocadas: " + assigned.revokedCount() + ".");
        } catch (InvalidScopeException ex) {
            return ToolExecutionResult.failure("INVALID_SCOPE", ex.getMessage());
        } catch (UserNotFoundException ex) {
            return ToolExecutionResult.failure("USER_NOT_FOUND", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", ex.getMessage());
        } catch (JsonProcessingException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", "No se pudieron interpretar los argumentos de la tool.");
        } catch (RuntimeException ex) {
            return ToolExecutionResult.failure("ASSISTANT_TOOL_FAILED", ex.getMessage());
        }
    }

    private UUID resolveProgramId(JsonNode args) {
        if (args.hasNonNull("programId") && !args.get("programId").asText().isBlank()) {
            return UUID.fromString(args.get("programId").asText().trim());
        }
        if (args.hasNonNull("programQuery") && !args.get("programQuery").asText().isBlank()) {
            String query = args.get("programQuery").asText().trim();
            List<ListProgramsUseCase.ProgramSummary> matches = listProgramsUseCase.list(query);
            if (matches.isEmpty()) {
                throw new IllegalArgumentException("No se encontró carrera con: " + query);
            }
            if (matches.size() > 1) {
                throw new IllegalArgumentException(
                        "Hay varias carreras que coinciden. Indique programId UUID.");
            }
            return matches.getFirst().id();
        }
        return null;
    }

    private String generateTemporaryPassword() {
        final String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 10; i++) {
            sb.append(alphabet.charAt(secureRandom.nextInt(alphabet.length())));
        }
        sb.append('A').append('1');
        return sb.toString();
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private ToolExecutionResult executeListPrograms(String argumentsJson) {
        try {
            String query = null;
            if (argumentsJson != null && !argumentsJson.isBlank()) {
                JsonNode args = objectMapper.readTree(argumentsJson);
                if (args.hasNonNull("query")) {
                    query = args.get("query").asText();
                }
            }

            List<Map<String, Object>> programs = listProgramsUseCase.list(query).stream()
                    .map(program -> {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("programId", program.id().toString());
                        map.put("code", program.code());
                        map.put("name", program.name());
                        return map;
                    })
                    .toList();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("programs", programs);
            data.put("total", programs.size());
            return ToolExecutionResult.success(data);
        } catch (JsonProcessingException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", "No se pudieron interpretar los argumentos de la tool.");
        }
    }

    private ToolExecutionResult executeListActiveProcesses(String argumentsJson, AssistantAuthContext auth) {
        try {
            JsonNode args = parseArgs(argumentsJson);
            String careerQuery = args.hasNonNull("careerQuery") ? args.get("careerQuery").asText(null) : null;
            String templateType = args.hasNonNull("templateType") ? args.get("templateType").asText(null) : null;

            List<Map<String, Object>> processes = AssistantProcessResolver.listActiveProcessPayload(
                    careerQuery, templateType, auth, listProcessesUseCase);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("processes", processes);
            data.put("total", processes.size());
            return ToolExecutionResult.success(data);
        } catch (JsonProcessingException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", "No se pudieron interpretar los argumentos de la tool.");
        } catch (RuntimeException ex) {
            return ToolExecutionResult.failure("ASSISTANT_TOOL_FAILED", ex.getMessage());
        }
    }

    private ToolExecutionResult executeListProcessPhases(String argumentsJson, AssistantAuthContext auth) {
        try {
            JsonNode args = parseArgs(argumentsJson);
            String careerQuery = requiredText(args, "careerQuery");
            String templateType = args.hasNonNull("templateType") ? args.get("templateType").asText(null) : null;

            AssistantProcessResolver.ResolveResult resolved = AssistantProcessResolver.resolveActiveProcess(
                    careerQuery,
                    templateType,
                    auth,
                    listProgramsUseCase,
                    listProcessesUseCase,
                    getProcessDetailUseCase
            );
            if (!resolved.isOk()) {
                return ToolExecutionResult.failure(resolved.errorCode(), resolved.errorMessage());
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("processId", resolved.process().processId().toString());
            data.put("careerName", resolved.process().careerName());
            data.put("careerCode", resolved.process().careerCode());
            data.put("templateType", resolved.process().templateType());
            data.put("templateName", resolved.process().templateName());
            data.put("phases", AssistantProcessResolver.toPhasePayload(resolved.detail()));
            data.put("total", resolved.detail().phases().size());
            return ToolExecutionResult.success(data);
        } catch (IllegalArgumentException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", ex.getMessage());
        } catch (JsonProcessingException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", "No se pudieron interpretar los argumentos de la tool.");
        } catch (RuntimeException ex) {
            return ToolExecutionResult.failure("ASSISTANT_TOOL_FAILED", ex.getMessage());
        }
    }

    private ToolExecutionResult executeListProcessStructure(String argumentsJson, AssistantAuthContext auth) {
        try {
            JsonNode args = parseArgs(argumentsJson);
            String careerQuery = requiredText(args, "careerQuery");
            String templateType = args.hasNonNull("templateType") ? args.get("templateType").asText(null) : null;

            AssistantProcessResolver.ResolveResult resolved = AssistantProcessResolver.resolveActiveProcess(
                    careerQuery,
                    templateType,
                    auth,
                    listProgramsUseCase,
                    listProcessesUseCase,
                    getProcessDetailUseCase
            );
            if (!resolved.isOk()) {
                return ToolExecutionResult.failure(resolved.errorCode(), resolved.errorMessage());
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("processId", resolved.process().processId().toString());
            data.put("careerName", resolved.process().careerName());
            data.put("careerCode", resolved.process().careerCode());
            data.put("templateType", resolved.process().templateType());
            data.put("templateName", resolved.process().templateName());
            data.put("phases", AssistantProcessResolver.toStructurePayload(resolved.detail()));
            data.put("total", resolved.detail().phases().size());
            data.put("includesSubphases", true);
            return ToolExecutionResult.success(data);
        } catch (IllegalArgumentException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", ex.getMessage());
        } catch (JsonProcessingException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", "No se pudieron interpretar los argumentos de la tool.");
        } catch (RuntimeException ex) {
            return ToolExecutionResult.failure("ASSISTANT_TOOL_FAILED", ex.getMessage());
        }
    }

    private ToolExecutionResult executeSetUserStatus(String argumentsJson, AssistantAuthContext auth) {
        try {
            JsonNode args = parseArgs(argumentsJson);
            String identifier = requiredText(args, "identifier");
            String action = requiredText(args, "action").toUpperCase(Locale.ROOT);
            boolean confirmed = AssistantConfirmationSupport.isConfirmed(args);

            if (!"ACTIVATE".equals(action) && !"DEACTIVATE".equals(action)) {
                return ToolExecutionResult.failure("INVALID_ACTION", "La acción debe ser ACTIVATE o DEACTIVATE.");
            }

            AssistantUserLookup.LookupResult lookup = AssistantUserLookup.resolve(
                    identifier, listUsersUseCase, userRepositoryPort);
            if (!lookup.isOk()) {
                return ToolExecutionResult.failure(lookup.errorCode(), lookup.errorMessage());
            }

            ListUsersUseCase.UserSummary user = lookup.user().summary();
            if (auth.userId().equals(user.userId())) {
                return ToolExecutionResult.failure(
                        "SELF_MODIFICATION_DENIED",
                        "No puede activar ni desactivar su propia cuenta.");
            }

            Map<String, Object> preview = new LinkedHashMap<>();
            preview.put("userId", user.userId().toString());
            preview.put("email", user.email());
            preview.put("fullName", user.fullName());
            preview.put("role", user.role());
            preview.put("currentStatus", user.status());
            preview.put("requestedAction", action);

            if ("DEACTIVATE".equals(action) && UserStatus.DEACTIVATED.name().equals(user.status())) {
                return ToolExecutionResult.failure("INVALID_STATUS", "El usuario ya está desactivado.");
            }
            if ("ACTIVATE".equals(action) && UserStatus.ACTIVE.name().equals(user.status())) {
                return ToolExecutionResult.failure("INVALID_STATUS", "El usuario ya está activo.");
            }

            if (!confirmed) {
                String verb = "DEACTIVATE".equals(action) ? "desactivar" : "activar";
                return AssistantConfirmationSupport.confirmationRequired(
                        action,
                        preview,
                        "Confirme que desea " + verb + " a " + user.fullName()
                                + " (" + user.email() + "). Responda explícitamente que confirma para proceder.");
            }

            if ("DEACTIVATE".equals(action)) {
                deactivateUserUseCase.deactivate(user.userId());
            } else {
                activateUserUseCase.activate(user.userId());
            }

            Map<String, Object> result = new LinkedHashMap<>(preview);
            result.put("newStatus", "DEACTIVATE".equals(action)
                    ? UserStatus.DEACTIVATED.name()
                    : UserStatus.ACTIVE.name());

            String message = "DEACTIVATE".equals(action)
                    ? "Usuario desactivado correctamente."
                    : "Usuario activado correctamente.";
            return AssistantConfirmationSupport.executed(action, result, message);
        } catch (UserNotFoundException ex) {
            return ToolExecutionResult.failure("USER_NOT_FOUND", ex.getMessage());
        } catch (InvalidUserStatusTransitionException ex) {
            return ToolExecutionResult.failure("INVALID_STATUS", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", ex.getMessage());
        } catch (JsonProcessingException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", "No se pudieron interpretar los argumentos de la tool.");
        } catch (RuntimeException ex) {
            return ToolExecutionResult.failure("ASSISTANT_TOOL_FAILED", ex.getMessage());
        }
    }

    private ToolExecutionResult executeManageProcessPhase(String argumentsJson, AssistantAuthContext auth) {
        try {
            JsonNode args = parseArgs(argumentsJson);
            String action = requiredText(args, "action").toUpperCase(Locale.ROOT);
            String careerQuery = requiredText(args, "careerQuery");
            boolean confirmed = AssistantConfirmationSupport.isConfirmed(args);

            AssistantProcessResolver.ResolveResult resolved = AssistantProcessResolver.resolveActiveProcess(
                    careerQuery,
                    null,
                    auth,
                    listProgramsUseCase,
                    listProcessesUseCase,
                    getProcessDetailUseCase
            );
            if (!resolved.isOk()) {
                return ToolExecutionResult.failure(resolved.errorCode(), resolved.errorMessage());
            }

            UUID processId = resolved.process().processId();
            EnrichedProcessDetail detail = resolved.detail();

            return switch (action) {
                case "CREATE" -> manageCreatePhase(args, confirmed, processId, resolved, detail);
                case "UPDATE" -> manageUpdatePhase(args, confirmed, processId, resolved, detail);
                case "DELETE" -> manageDeletePhase(args, confirmed, processId, resolved, detail);
                case "REORDER" -> manageReorderPhases(args, confirmed, processId, resolved, detail, auth);
                default -> ToolExecutionResult.failure(
                        "INVALID_ACTION",
                        "La acción debe ser CREATE, UPDATE, DELETE o REORDER.");
            };
        } catch (IllegalArgumentException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", ex.getMessage());
        } catch (JsonProcessingException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", "No se pudieron interpretar los argumentos de la tool.");
        } catch (ProcessNotEditableException | ProcessStructureOrderConflictException | SubphaseHasEvidenceException ex) {
            return ToolExecutionResult.failure("BUSINESS_RULE_VIOLATION", ex.getMessage());
        } catch (RuntimeException ex) {
            return ToolExecutionResult.failure("ASSISTANT_TOOL_FAILED", ex.getMessage());
        }
    }

    private ToolExecutionResult manageCreatePhase(JsonNode args,
                                                  boolean confirmed,
                                                  UUID processId,
                                                  AssistantProcessResolver.ResolveResult resolved,
                                                  EnrichedProcessDetail detail) {
        String name = requiredText(args, "name");
        Integer order = args.hasNonNull("order") ? args.get("order").asInt() : nextPhaseOrder(detail);
        String description = args.hasNonNull("description") ? args.get("description").asText(null) : null;

        Map<String, Object> preview = basePhasePreview(resolved, "CREATE");
        preview.put("name", name);
        preview.put("order", order);
        preview.put("description", description);

        if (!confirmed) {
            return AssistantConfirmationSupport.confirmationRequired(
                    "CREATE",
                    preview,
                    "Confirme la creación de la fase \"" + name + "\" en "
                            + resolved.process().careerName() + " con orden " + order + ".");
        }

        Phase created = addProcessPhaseUseCase.execute(processId, name, order, description);
        Map<String, Object> result = new LinkedHashMap<>(preview);
        result.put("phaseId", created.getId().toString());
        return AssistantConfirmationSupport.executed("CREATE", result, "Fase creada correctamente.");
    }

    private ToolExecutionResult manageUpdatePhase(JsonNode args,
                                                  boolean confirmed,
                                                  UUID processId,
                                                  AssistantProcessResolver.ResolveResult resolved,
                                                  EnrichedProcessDetail detail) {
        Phase existing = AssistantStructureLookup.findPhase(args, detail);
        String name = args.hasNonNull("name") ? args.get("name").asText(null) : existing.getName();
        Integer order = args.hasNonNull("order") ? args.get("order").asInt() : existing.getOrder();
        String description = args.hasNonNull("description") ? args.get("description").asText(null) : existing.getDescription();

        Map<String, Object> preview = basePhasePreview(resolved, "UPDATE");
        preview.put("phaseId", existing.getId().toString());
        preview.put("currentName", existing.getName());
        preview.put("currentOrder", existing.getOrder());
        preview.put("newName", name);
        preview.put("newOrder", order);
        preview.put("newDescription", description);

        if (!confirmed) {
            return AssistantConfirmationSupport.confirmationRequired(
                    "UPDATE",
                    preview,
                    "Confirme la edición de la fase \"" + existing.getName() + "\" en "
                            + resolved.process().careerName() + ".");
        }

        Phase updated = updateProcessPhaseUseCase.execute(processId, existing.getId(), name, order, description);
        Map<String, Object> result = new LinkedHashMap<>(preview);
        result.put("phaseId", updated.getId().toString());
        return AssistantConfirmationSupport.executed("UPDATE", result, "Fase actualizada correctamente.");
    }

    private ToolExecutionResult manageDeletePhase(JsonNode args,
                                                  boolean confirmed,
                                                  UUID processId,
                                                  AssistantProcessResolver.ResolveResult resolved,
                                                  EnrichedProcessDetail detail) {
        Phase existing = AssistantStructureLookup.findPhase(args, detail);

        Map<String, Object> preview = basePhasePreview(resolved, "DELETE");
        preview.put("phaseId", existing.getId().toString());
        preview.put("name", existing.getName());
        preview.put("order", existing.getOrder());
        preview.put("subphaseCount", existing.getSubphases() == null ? 0 : existing.getSubphases().size());

        if (!confirmed) {
            return AssistantConfirmationSupport.confirmationRequired(
                    "DELETE",
                    preview,
                    "Confirme la eliminación de la fase \"" + existing.getName() + "\" en "
                            + resolved.process().careerName()
                            + ". Esta acción no se puede deshacer.");
        }

        deleteProcessPhaseUseCase.execute(processId, existing.getId());
        return AssistantConfirmationSupport.executed("DELETE", preview, "Fase eliminada correctamente.");
    }

    private ToolExecutionResult manageReorderPhases(JsonNode args,
                                                    boolean confirmed,
                                                    UUID processId,
                                                    AssistantProcessResolver.ResolveResult resolved,
                                                    EnrichedProcessDetail detail,
                                                    AssistantAuthContext auth) {
        if (!args.hasNonNull("phaseIds") || !args.get("phaseIds").isArray() || args.get("phaseIds").isEmpty()) {
            return ToolExecutionResult.failure(
                    "INVALID_ARGUMENTS",
                    "Debe indicar phaseIds con el orden deseado de las fases.");
        }

        List<UUID> requestedOrder = new ArrayList<>();
        for (JsonNode node : args.get("phaseIds")) {
            requestedOrder.add(UUID.fromString(node.asText()));
        }

        if (requestedOrder.size() != detail.phases().size()) {
            return ToolExecutionResult.failure(
                    "INVALID_ARGUMENTS",
                    "Debe incluir todas las fases del proceso en phaseIds.");
        }

        List<UUID> existingIds = detail.phases().stream().map(Phase::getId).toList();
        if (!existingIds.containsAll(requestedOrder) || !requestedOrder.containsAll(existingIds)) {
            return ToolExecutionResult.failure(
                    "INVALID_ARGUMENTS",
                    "phaseIds debe contener exactamente las fases del proceso activo.");
        }

        Map<String, Object> preview = basePhasePreview(resolved, "REORDER");
        preview.put("requestedPhaseIds", requestedOrder.stream().map(UUID::toString).toList());
        preview.put("currentPhases", AssistantProcessResolver.toPhasePayload(detail));

        if (!confirmed) {
            return AssistantConfirmationSupport.confirmationRequired(
                    "REORDER",
                    preview,
                    "Confirme el nuevo orden de fases para " + resolved.process().careerName() + ".");
        }

        reorderProcessStructureUseCase.execute(processId, requestedOrder, Map.of());
        EnrichedProcessDetail updated = getProcessDetailUseCase.getDetail(
                processId,
                new com.umss.sigesa.application.model.process.ProcessQueryContext(auth.role(), auth.programScope())
        );

        Map<String, Object> result = new LinkedHashMap<>(preview);
        result.put("phases", AssistantProcessResolver.toPhasePayload(updated));
        return AssistantConfirmationSupport.executed("REORDER", result, "Orden de fases actualizado correctamente.");
    }

    private ToolExecutionResult executeManageProcessSubphase(String argumentsJson, AssistantAuthContext auth) {
        try {
            JsonNode args = parseArgs(argumentsJson);
            String action = requiredText(args, "action").toUpperCase(Locale.ROOT);
            String careerQuery = requiredText(args, "careerQuery");
            boolean confirmed = AssistantConfirmationSupport.isConfirmed(args);

            AssistantProcessResolver.ResolveResult resolved = AssistantProcessResolver.resolveActiveProcess(
                    careerQuery,
                    null,
                    auth,
                    listProgramsUseCase,
                    listProcessesUseCase,
                    getProcessDetailUseCase
            );
            if (!resolved.isOk()) {
                return ToolExecutionResult.failure(resolved.errorCode(), resolved.errorMessage());
            }

            UUID processId = resolved.process().processId();
            EnrichedProcessDetail detail = resolved.detail();

            return switch (action) {
                case "CREATE" -> manageCreateSubphase(args, confirmed, processId, resolved, detail);
                case "UPDATE" -> manageUpdateSubphase(args, confirmed, processId, resolved, detail);
                case "DELETE" -> manageDeleteSubphase(args, confirmed, processId, resolved, detail);
                default -> ToolExecutionResult.failure(
                        "INVALID_ACTION",
                        "La acción debe ser CREATE, UPDATE o DELETE.");
            };
        } catch (IllegalArgumentException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", ex.getMessage());
        } catch (JsonProcessingException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", "No se pudieron interpretar los argumentos de la tool.");
        } catch (ProcessNotEditableException | ProcessStructureOrderConflictException | SubphaseHasEvidenceException ex) {
            return ToolExecutionResult.failure("BUSINESS_RULE_VIOLATION", ex.getMessage());
        } catch (RuntimeException ex) {
            return ToolExecutionResult.failure("ASSISTANT_TOOL_FAILED", ex.getMessage());
        }
    }

    private ToolExecutionResult manageCreateSubphase(JsonNode args,
                                                     boolean confirmed,
                                                     UUID processId,
                                                     AssistantProcessResolver.ResolveResult resolved,
                                                     EnrichedProcessDetail detail) {
        Phase phase = AssistantStructureLookup.findPhase(args, detail);
        String name = requiredText(args, "name");
        String referenceUrl = requiredText(args, "referenceUrl");
        Integer llmOrder = args.hasNonNull("order") ? args.get("order").asInt() : null;
        AssistantStructureLookup.SubphaseOrderPlan orderPlan =
                AssistantStructureLookup.planCreateSubphaseOrder(phase, llmOrder);
        int order = orderPlan.assignedOrder();
        String description = args.hasNonNull("description") ? args.get("description").asText(null) : null;

        Map<String, Object> preview = basePhasePreview(resolved, "CREATE_SUBPHASE");
        preview.put("phaseId", phase.getId().toString());
        preview.put("phaseName", phase.getName());
        preview.put("name", name);
        preview.put("order", order);
        preview.put("existingSubphaseCount", orderPlan.existingCount());
        preview.put("maxExistingOrder", orderPlan.maxExistingOrder());
        preview.put("assignedOrder", orderPlan.assignedOrder());
        preview.put("referenceUrl", referenceUrl);
        preview.put("description", description);

        if (!confirmed) {
            return AssistantConfirmationSupport.confirmationRequired(
                    "CREATE",
                    preview,
                    orderPlan.confirmationMessage(phase.getName(), name, referenceUrl));
        }

        Subphase created = addProcessSubphaseUseCase.execute(
                processId, phase.getId(), name, order, referenceUrl, description);
        Map<String, Object> result = new LinkedHashMap<>(preview);
        result.put("subphaseId", created.getId().toString());
        return AssistantConfirmationSupport.executed(
                "CREATE",
                result,
                "Subfase «" + name + "» creada en «" + phase.getName()
                        + "» con orden " + order + ".");
    }

    private ToolExecutionResult manageUpdateSubphase(JsonNode args,
                                                     boolean confirmed,
                                                     UUID processId,
                                                     AssistantProcessResolver.ResolveResult resolved,
                                                     EnrichedProcessDetail detail) {
        Phase phase = AssistantStructureLookup.findPhase(args, detail);
        Subphase existing = AssistantStructureLookup.findSubphase(args, phase);

        String name = args.hasNonNull("name") ? args.get("name").asText(null) : existing.getName();
        Integer order = args.hasNonNull("order") ? args.get("order").asInt() : existing.getOrder();
        String referenceUrl = args.hasNonNull("referenceUrl")
                ? args.get("referenceUrl").asText(null)
                : existing.getReferenceUrl();
        String description = args.hasNonNull("description")
                ? args.get("description").asText(null)
                : existing.getDescription();

        Map<String, Object> preview = basePhasePreview(resolved, "UPDATE_SUBPHASE");
        preview.put("phaseId", phase.getId().toString());
        preview.put("subphaseId", existing.getId().toString());
        preview.put("currentName", existing.getName());
        preview.put("newName", name);
        preview.put("newOrder", order);
        preview.put("newReferenceUrl", referenceUrl);

        if (!confirmed) {
            return AssistantConfirmationSupport.confirmationRequired(
                    "UPDATE",
                    preview,
                    "Confirme la edición de la subfase \"" + existing.getName() + "\".");
        }

        Subphase updated = updateProcessSubphaseUseCase.execute(
                processId, phase.getId(), existing.getId(), name, order, referenceUrl, description);
        Map<String, Object> result = new LinkedHashMap<>(preview);
        result.put("subphaseId", updated.getId().toString());
        return AssistantConfirmationSupport.executed("UPDATE", result, "Subfase actualizada correctamente.");
    }

    private ToolExecutionResult manageDeleteSubphase(JsonNode args,
                                                     boolean confirmed,
                                                     UUID processId,
                                                     AssistantProcessResolver.ResolveResult resolved,
                                                     EnrichedProcessDetail detail) {
        Phase phase = AssistantStructureLookup.findPhase(args, detail);
        Subphase existing = AssistantStructureLookup.findSubphase(args, phase);

        Map<String, Object> preview = basePhasePreview(resolved, "DELETE_SUBPHASE");
        preview.put("phaseId", phase.getId().toString());
        preview.put("subphaseId", existing.getId().toString());
        preview.put("name", existing.getName());
        preview.put("referenceUrl", existing.getReferenceUrl());

        if (!confirmed) {
            return AssistantConfirmationSupport.confirmationRequired(
                    "DELETE",
                    preview,
                    "Confirme la eliminación de la subfase \"" + existing.getName() + "\".");
        }

        deleteProcessSubphaseUseCase.execute(processId, phase.getId(), existing.getId());
        return AssistantConfirmationSupport.executed("DELETE", preview, "Subfase eliminada correctamente.");
    }

    private ToolExecutionResult executeListPendingEvidences(String argumentsJson, AssistantAuthContext auth) {
        try {
            UUID programId = null;
            if (argumentsJson != null && !argumentsJson.isBlank()) {
                JsonNode args = objectMapper.readTree(argumentsJson);
                if (args.hasNonNull("programId") && !args.get("programId").asText().isBlank()) {
                    programId = UUID.fromString(args.get("programId").asText().trim());
                }
            }
            List<EvidenceControlItem> items = listPendingEvidencesUseCase.list(auth, programId);
            List<Map<String, Object>> payload = items.stream().map(this::toEvidenceControlMap).toList();
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("evidences", payload);
            data.put("total", payload.size());
            data.put("stateFilter", "SUBIDO");
            return ToolExecutionResult.success(data);
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENT", "programId inválido.");
        } catch (ProgramScopeDeniedException ex) {
            return ToolExecutionResult.failure("ACCESS_DENIED", "No tiene acceso a la carrera solicitada.");
        }
    }

    private ToolExecutionResult executeGetEvidenceDetail(String argumentsJson, AssistantAuthContext auth) {
        try {
            JsonNode args = parseArgs(argumentsJson);
            UUID indicatorId = UUID.fromString(requiredText(args, "indicatorId"));
            var itemOpt = getEvidenceDetailUseCase.get(auth, indicatorId);
            if (itemOpt.isEmpty() || itemOpt.get().evidenceId() == null) {
                return ToolExecutionResult.failure(
                        "EVIDENCE_NOT_FOUND",
                        "No se encontró el indicador o su evidencia.");
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("evidence", toEvidenceControlMap(itemOpt.get()));
            return ToolExecutionResult.success(data);
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENT", ex.getMessage());
        } catch (ProgramScopeDeniedException ex) {
            return ToolExecutionResult.failure("ACCESS_DENIED", "No tiene acceso a la evidencia solicitada.");
        }
    }

    private ToolExecutionResult executeCheckEvidenceCompleteness(String argumentsJson, AssistantAuthContext auth) {
        try {
            JsonNode args = parseArgs(argumentsJson);
            UUID indicatorId = UUID.fromString(requiredText(args, "indicatorId"));
            CheckEvidenceCompletenessUseCase.CompletenessChecklist checklist =
                    checkEvidenceCompletenessUseCase.check(auth, indicatorId);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("indicatorId", checklist.indicatorId().toString());
            data.put("hasEvidence", checklist.hasEvidence());
            data.put("hasDescription", checklist.hasDescription());
            data.put("hasCriterion", checklist.hasCriterion());
            data.put("hasContentHash", checklist.hasContentHash());
            data.put("currentState", checklist.currentState() == null ? null : checklist.currentState().name());
            data.put("complete", checklist.complete());
            return ToolExecutionResult.success(data);
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENT", ex.getMessage());
        } catch (IndicatorNotFoundException ex) {
            return ToolExecutionResult.failure("EVIDENCE_NOT_FOUND", ex.getMessage());
        } catch (ProgramScopeDeniedException ex) {
            return ToolExecutionResult.failure("ACCESS_DENIED", "No tiene acceso a la evidencia solicitada.");
        }
    }

    private Map<String, Object> toEvidenceControlMap(EvidenceControlItem item) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("indicatorId", item.indicatorId() == null ? null : item.indicatorId().toString());
        map.put("programId", item.programId() == null ? null : item.programId().toString());
        map.put("criterionId", item.criterionId() == null ? null : item.criterionId().toString());
        map.put("phaseId", item.phaseId() == null ? null : item.phaseId().toString());
        map.put("currentState", item.currentState() == null ? null : item.currentState().name());
        map.put("evidenceId", item.evidenceId() == null ? null : item.evidenceId().toString());
        map.put("versionNumber", item.versionNumber());
        map.put("contentHash", item.contentHash());
        map.put("description", item.description());
        map.put("createdAt", item.createdAt() == null ? null : item.createdAt().toString());
        return map;
    }

    private static Map<String, Object> basePhasePreview(AssistantProcessResolver.ResolveResult resolved, String action) {
        Map<String, Object> preview = new LinkedHashMap<>();
        preview.put("processId", resolved.process().processId().toString());
        preview.put("careerName", resolved.process().careerName());
        preview.put("careerCode", resolved.process().careerCode());
        preview.put("requestedAction", action);
        return preview;
    }

    private static int nextPhaseOrder(EnrichedProcessDetail detail) {
        return detail.phases().stream()
                .map(Phase::getOrder)
                .filter(order -> order != null)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }

    private JsonNode parseArgs(String argumentsJson) throws JsonProcessingException {
        if (argumentsJson == null || argumentsJson.isBlank()) {
            return objectMapper.createObjectNode();
        }
        return objectMapper.readTree(argumentsJson);
    }

    private static String requiredText(JsonNode args, String field) {
        if (args == null || !args.hasNonNull(field) || args.get(field).asText().isBlank()) {
            throw new IllegalArgumentException("El campo '" + field + "' es obligatorio.");
        }
        return args.get(field).asText().trim();
    }

    private Map<String, Object> toUserMap(ListUsersUseCase.UserSummary user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userId", user.userId().toString());
        map.put("email", user.email());
        map.put("role", user.role());
        map.put("status", user.status());
        map.put("programIds", user.programIds().stream().map(Object::toString).toList());
        map.put("fullName", user.fullName());
        map.put("phoneNumber", user.phoneNumber());
        return map;
    }

    private ToolExecutionResult executeApproveIndicator(String argumentsJson, AssistantAuthContext auth) {
        try {
            JsonNode args = parseArgs(argumentsJson);
            String indicatorIdStr = requiredText(args, "indicatorId");
            UUID indicatorId = UUID.fromString(indicatorIdStr);
            boolean confirmed = args.hasNonNull("confirmed") && args.get("confirmed").asBoolean();

            if (!confirmed) {
                Map<String, Object> previewData = new LinkedHashMap<>();
                previewData.put("indicatorId", indicatorIdStr);
                previewData.put("action", "APPROVE");
                previewData.put("confirmed", false);
                previewData.put("message", "Confirmar aprobación del indicador " + indicatorIdStr + ". Responder 'sí' para ejecutar.");
                return ToolExecutionResult.success(previewData);
            }

            var roleEnum = com.umss.sigesa.domain.model.Role.valueOf(auth.role().trim().toUpperCase());
            var result = approveIndicatorUseCase.approve(indicatorId, auth.userId(), roleEnum);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("indicatorId", indicatorIdStr);
            data.put("newState", result.newState().name());
            data.put("stateHistoryId", result.stateHistoryId().toString());
            return ToolExecutionResult.success(data);
        } catch (IllegalArgumentException ex) {
            return ToolExecutionResult.failure("INVALID_INPUT", ex.getMessage());
        } catch (IndicatorNotFoundException ex) {
            return ToolExecutionResult.failure("INDICATOR_NOT_FOUND", ex.getMessage());
        } catch (Exception ex) {
            return ToolExecutionResult.failure("EXECUTION_ERROR", "Error al aprobar indicador: " + ex.getMessage());
        }
    }

    private ToolExecutionResult executeRejectIndicator(String argumentsJson, AssistantAuthContext auth) {
        try {
            JsonNode args = parseArgs(argumentsJson);
            String indicatorIdStr = requiredText(args, "indicatorId");
            String justification = requiredText(args, "justification");
            UUID indicatorId = UUID.fromString(indicatorIdStr);
            boolean confirmed = args.hasNonNull("confirmed") && args.get("confirmed").asBoolean();

            if (!confirmed) {
                Map<String, Object> previewData = new LinkedHashMap<>();
                previewData.put("indicatorId", indicatorIdStr);
                previewData.put("justification", justification);
                previewData.put("action", "REJECT");
                previewData.put("confirmed", false);
                previewData.put("message", "Confirmar rechazo/observación del indicador " + indicatorIdStr + " con motivo: '" + justification + "'. Responder 'sí' para ejecutar.");
                return ToolExecutionResult.success(previewData);
            }

            var roleEnum = com.umss.sigesa.domain.model.Role.valueOf(auth.role().trim().toUpperCase());
            var result = rejectIndicatorUseCase.reject(indicatorId, justification, auth.userId(), roleEnum);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("indicatorId", indicatorIdStr);
            data.put("observationId", result.observationId());
            data.put("newState", result.newState().name());
            return ToolExecutionResult.success(data);
        } catch (IllegalArgumentException ex) {
            return ToolExecutionResult.failure("INVALID_INPUT", ex.getMessage());
        } catch (IndicatorNotFoundException ex) {
            return ToolExecutionResult.failure("INDICATOR_NOT_FOUND", ex.getMessage());
        } catch (Exception ex) {
            return ToolExecutionResult.failure("EXECUTION_ERROR", "Error al rechazar indicador: " + ex.getMessage());
        }
    }

    private String serialize(ToolExecutionResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException ex) {
            return "{\"ok\":false,\"data\":null,\"error\":{\"code\":\"SERIALIZATION_ERROR\","
                    + "\"message\":\"No se pudo serializar el resultado de la tool.\"}}";
        }
    }
}
