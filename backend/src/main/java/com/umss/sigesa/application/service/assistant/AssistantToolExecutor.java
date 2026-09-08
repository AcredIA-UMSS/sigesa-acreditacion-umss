package com.umss.sigesa.application.service.assistant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.application.model.assistant.AssistantAgentProfile;
import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.assistant.AssistantToolAuditRecord;
import com.umss.sigesa.application.model.assistant.AssistantToolDefinition;
import com.umss.sigesa.application.model.assistant.ToolExecutionResult;
import com.umss.sigesa.application.model.evidence.EvidenceControlItem;
import com.umss.sigesa.application.model.normative.NormativeDocumentHit;
import com.umss.sigesa.application.model.process.EnrichedProcessDetail;
import com.umss.sigesa.application.port.in.ActivateUserUseCase;
import com.umss.sigesa.application.port.in.CheckEvidenceCompletenessUseCase;
import com.umss.sigesa.application.port.in.DeactivateUserUseCase;
import com.umss.sigesa.application.port.in.GetEvidenceDetailUseCase;
import com.umss.sigesa.application.port.in.GetProcessDetailUseCase;
import com.umss.sigesa.application.port.in.ListPendingEvidencesUseCase;
import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.application.port.in.ListProgramsUseCase;
import com.umss.sigesa.application.port.in.ListUsersUseCase;
import com.umss.sigesa.application.port.in.ManageUserProgramAssignmentUseCase;
import com.umss.sigesa.application.port.in.RegisterUserUseCase;
import com.umss.sigesa.application.port.in.SearchNormativeDocumentsUseCase;
import com.umss.sigesa.application.port.out.AssistantToolAuditPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.DuplicateEmailException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.InvalidEmailDomainException;
import com.umss.sigesa.domain.exception.InvalidFilterException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.InvalidScopeException;
import com.umss.sigesa.domain.exception.InvalidUserProfileException;
import com.umss.sigesa.domain.exception.InvalidUserStatusTransitionException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.exception.UserNotFoundException;
import com.umss.sigesa.domain.exception.WeakPasswordException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.UserStatus;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
    private final ListPendingEvidencesUseCase listPendingEvidencesUseCase;
    private final GetEvidenceDetailUseCase getEvidenceDetailUseCase;
    private final CheckEvidenceCompletenessUseCase checkEvidenceCompletenessUseCase;
    private final SearchNormativeDocumentsUseCase searchNormativeDocumentsUseCase;
    private final ObjectMapper objectMapper;
    private final AssistantToolAuditPort toolAuditPort;
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
                                 ListPendingEvidencesUseCase listPendingEvidencesUseCase,
                                 GetEvidenceDetailUseCase getEvidenceDetailUseCase,
                                 CheckEvidenceCompletenessUseCase checkEvidenceCompletenessUseCase,
                                 SearchNormativeDocumentsUseCase searchNormativeDocumentsUseCase,
                                 ObjectMapper objectMapper,
                                 AssistantToolAuditPort toolAuditPort) {
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
        this.listPendingEvidencesUseCase = listPendingEvidencesUseCase;
        this.getEvidenceDetailUseCase = getEvidenceDetailUseCase;
        this.checkEvidenceCompletenessUseCase = checkEvidenceCompletenessUseCase;
        this.searchNormativeDocumentsUseCase = searchNormativeDocumentsUseCase;
        this.objectMapper = objectMapper;
        this.toolAuditPort = toolAuditPort;
    }

    public String execute(String toolId, String argumentsJson, AssistantAuthContext auth) {
        return execute(toolId, argumentsJson, auth, AssistantAgentProfile.GENERAL);
    }

    public String execute(String toolId,
                          String argumentsJson,
                          AssistantAuthContext auth,
                          AssistantAgentProfile agentProfile) {
        AssistantToolDefinition definition = toolRegistry.findById(toolId).orElse(null);
        String sideEffect = definition != null ? definition.sideEffect() : "unknown";
        String agentId = agentProfile != null ? agentProfile.agentId() : "general";

        Optional<ToolExecutionResult> denied = AssistantToolRbacGuard.denyIfUnauthorized(
                definition, auth, agentProfile, toolRegistry, toolId);
        if (denied.isPresent()) {
            ToolExecutionResult failure = denied.get();
            auditInvocation(auth, agentId, toolId, sideEffect, failure);
            return serialize(failure);
        }

        ToolExecutionResult result = switch (toolId) {
            case AssistantToolRegistry.LIST_USERS_ID -> executeListUsers(argumentsJson);
            case AssistantToolRegistry.GET_USER_DETAIL_ID -> executeGetUserDetail(argumentsJson);
            case AssistantToolRegistry.CREATE_USER_ID -> executeCreateUser(argumentsJson);
            case AssistantToolRegistry.LIST_PROGRAMS_ID -> executeListPrograms(argumentsJson);
            case AssistantToolRegistry.LIST_ACTIVE_PROCESSES_ID -> executeListActiveProcesses(argumentsJson, auth);
            case AssistantToolRegistry.LIST_PROCESS_STRUCTURE_ID -> executeListProcessStructure(argumentsJson, auth);
            case AssistantToolRegistry.SET_USER_STATUS_ID -> executeSetUserStatus(argumentsJson, auth);
            case AssistantToolRegistry.MANAGE_USER_STATUS_ID -> executeManageUserStatus(argumentsJson, auth);
            case AssistantToolRegistry.MANAGE_USER_ASSIGNMENT_ID -> executeManageUserAssignment(argumentsJson);
            case AssistantToolRegistry.LIST_PENDING_EVIDENCES_ID -> executeListPendingEvidences(argumentsJson, auth);
            case AssistantToolRegistry.GET_EVIDENCE_DETAIL_ID -> executeGetEvidenceDetail(argumentsJson, auth);
            case AssistantToolRegistry.CHECK_EVIDENCE_COMPLETENESS_ID ->
                    executeCheckEvidenceCompleteness(argumentsJson, auth);
            case AssistantToolRegistry.SEARCH_NORMATIVE_DOCS_ID ->
                    executeSearchNormativeDocs(argumentsJson);
            default -> ToolExecutionResult.failure("TOOL_NOT_FOUND", "Tool desconocida: " + toolId);
        };

        auditInvocation(auth, agentId, toolId, sideEffect, result);
        return serialize(result);
    }

    private void auditInvocation(AssistantAuthContext auth,
                                 String agentId,
                                 String toolId,
                                 String sideEffect,
                                 ToolExecutionResult result) {
        if (toolAuditPort == null || auth == null || auth.userId() == null) {
            return;
        }
        boolean success = result != null && result.ok();
        String outcomeCode = success ? "OK" : result != null && result.error() != null
                ? result.error().code()
                : "UNKNOWN";
        toolAuditPort.logToolInvocation(new AssistantToolAuditRecord(
                auth.userId(),
                auth.role(),
                agentId,
                toolId,
                sideEffect,
                success,
                outcomeCode));
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

            List<Map<String, Object>> level1Nodes = AssistantProcessResolver.toNormativeStructurePayload(resolved.detail());
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("processId", resolved.process().processId().toString());
            data.put("careerName", resolved.process().careerName());
            data.put("careerCode", resolved.process().careerCode());
            data.put("templateType", resolved.process().templateType());
            data.put("templateName", resolved.process().templateName());
            data.put("level1Nodes", level1Nodes);
            data.put("total", level1Nodes.size());
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

    private ToolExecutionResult executeSearchNormativeDocs(String argumentsJson) {
        try {
            JsonNode args = parseArgs(argumentsJson);
            String query = requiredText(args, "query");
            String templateType = args.hasNonNull("templateType") ? args.get("templateType").asText(null) : null;
            int limit = args.hasNonNull("limit") ? Math.clamp(args.get("limit").asInt(), 1, 5) : 3;
            List<NormativeDocumentHit> hits = searchNormativeDocumentsUseCase.search(query, templateType, limit);
            List<Map<String, Object>> documents = hits.stream().map(this::toNormativeDocumentMap).toList();
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("query", query);
            data.put("documents", documents);
            data.put("total", documents.size());
            if (templateType != null && !templateType.isBlank()) {
                data.put("templateType", templateType);
            }
            return ToolExecutionResult.success(data);
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENT", ex.getMessage());
        }
    }

    private Map<String, Object> toNormativeDocumentMap(NormativeDocumentHit hit) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("title", hit.title());
        map.put("templateType", hit.templateType());
        map.put("phaseName", hit.phaseName());
        map.put("subphaseName", hit.subphaseName());
        map.put("sourceUrl", hit.sourceUrl());
        map.put("snippet", hit.snippet());
        map.put("score", hit.score());
        return map;
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

    private String serialize(ToolExecutionResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException ex) {
            return "{\"ok\":false,\"data\":null,\"error\":{\"code\":\"SERIALIZATION_ERROR\","
                    + "\"message\":\"No se pudo serializar el resultado de la tool.\"}}";
        }
    }
}
